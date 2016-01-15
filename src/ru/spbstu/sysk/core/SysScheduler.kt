package ru.spbstu.sysk.core

import ru.spbstu.sysk.connectors.SysBus
import ru.spbstu.sysk.data.SysPort
import ru.spbstu.sysk.data.SysSignal
import java.util.*

class SysScheduler {

    var currentTime: SysWait.Time = SysWait.Time(0)
        private set

    private val events: MutableMap<SysWait.Event, SysWait.Time> = LinkedHashMap()

    private val signals: MutableSet<SysSignal<*>> = LinkedHashSet()

    private val buses: MutableSet<SysBus<*>> = LinkedHashSet()

    private val ports: MutableSet<SysPort<*>> = LinkedHashSet()

    private val functions: MutableMap<SysFunction, SysWait> = LinkedHashMap()

    private val newFunctions: MutableMap<SysFunction, SysWait> = LinkedHashMap()

    internal fun register(event: SysWait.Event) {
        events.put(event, SysWait.Time.INFINITY)
    }

    internal fun register(signal: SysSignal<*>) {
        signals.add(signal)
    }

    internal fun register(bus: SysBus<*>) {
        buses.add(bus)
    }

    internal fun register(port: SysPort<*>) {
        ports.add(port)
    }

    internal fun register(function: SysFunction) {
        newFunctions[function] = convert(function.sensitivities)
    }

    internal fun happens(event: SysWait.Event, delay: SysWait.Time) {
        events[event] = if (delay.femtoSeconds < 0) SysWait.Time(0) else delay + currentTime
    }

    private fun resetEvents() {
        val reset = hashMapOf<SysWait.Event, SysWait.Time>()
        for ((event, time) in events) {
            if (time <= currentTime) reset[event] = SysWait.Time.INFINITY
        }
        events += reset
    }

    private fun update() {
        signals.forEach { it.update() }
        buses.forEach { it.update() }
    }

    private fun convertToList(sensitivities: List<SysWait>): List<SysWait> {
        val result = ArrayList<SysWait>()
        var time: SysWait.Time? = null
        for (s in sensitivities) {
            when (s) {
                is SysWait.Initialize -> result.add(s)
                is SysWait.Event -> result.add(s)
                is SysWait.Finder -> result.add(s)
                is SysWait.Time -> if (time == null || time > s + currentTime) time = s + currentTime
                is SysWait.OneOf -> {
                    val innerResult = convertToList(s.elements)
                    val last = innerResult.lastOrNull()
                    if (last is SysWait.Time) {
                        result.addAll(innerResult.subList(0, innerResult.size - 1))
                        if (time == null || time > last + currentTime) time = last + currentTime
                    }
                    else {
                        result.addAll(innerResult)
                    }
                }
            }
        }
        time?.let { result.add(it) }
        return result
    }

    private fun convert(wait: SysWait) = convert(listOf(wait))

    private fun convert(sensitivities: List<SysWait>): SysWait = SysWait.reduce(convertToList(sensitivities))

    private fun triggeredEvent(wait: SysWait, events: Set<SysWait?>): SysWait? =
        when (wait) {
            is SysWait.Initialize, is SysWait.Event ->
                if (wait in events) wait else null
            // Temporary (wait) (here and below in time): see KT-10483
            is SysWait.Finder -> (wait)().let { if (it in events) it else null }
            is SysWait.Time -> if (wait <= currentTime) wait else null
            is SysWait.OneOf -> {
                for (element in wait.elements) {
                    triggeredEvent(element, events)?.let { return it }
                }
                null
            }
            is SysWait.Never -> null
        }

    private fun happenTime(event: SysWait.Event) = events[event] ?: SysWait.Time.INFINITY

    private fun time(wait: SysWait): SysWait.Time =
        when (wait) {
            is SysWait.Event -> happenTime(wait).let { if (it <= currentTime) currentTime else it }
            is SysWait.Finder -> (wait)()?.let { if (happenTime(it) <= currentTime) currentTime else null} ?: SysWait.Time.INFINITY
            is SysWait.Time -> wait
            is SysWait.OneOf -> wait.elements.map { time(it) }.min() ?: SysWait.Time.INFINITY
            else -> SysWait.Time.INFINITY
        }

    var stopRequested = true
        private set

    fun start(endTime: SysWait.Time = SysWait.Time.INFINITY) {
        stopRequested = false
        ports.forEach { assert(it.isBound || it.sealed) { "Port $it: is not bounded and not sealed." } }
        if (currentTime >= endTime) return
        var happenedEvents: Set<SysWait> = if (currentTime.femtoSeconds == 0L) setOf(SysWait.Initialize) else setOf()
        var initialize = true
        while (currentTime < endTime && !stopRequested) {
            var globalClosestTime = SysWait.Time.INFINITY
            var functionActivated = false
            var sensitivities: SysWait
            var neverCalledFunctions: MutableList<SysFunction> = ArrayList()
            if (!initialize) resetEvents()
            else initialize = false
            functions += newFunctions
            newFunctions.clear()
            for ((function, wait) in functions) {
                val triggered = triggeredEvent(wait, happenedEvents)
                if (triggered != null) {
                    functionActivated = true
                    sensitivities = convert(function.run(triggered))
                    if (sensitivities == SysWait.Never) neverCalledFunctions.add(function)
                    else functions[function] = sensitivities
                }
                else if (!functionActivated) {
                    val closestTime = time(wait)
                    if (closestTime > currentTime && closestTime < globalClosestTime) {
                        globalClosestTime = closestTime
                    }
                }
            }
            if (functionActivated) {
                functions -= neverCalledFunctions
                update()
                // Proceed to next delta-cycle
            }
            else {
                currentTime = globalClosestTime
                // Proceed to next time moment
            }
            happenedEvents = events.entries.filter { it.value <= currentTime }.map { it.key }.toSet()
        }
    }

    fun stop() {
        stopRequested = true
    }
}
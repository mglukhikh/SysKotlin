package sysk

import java.util.*

class SysScheduler {

    var currentTime: SysWait.Time = SysWait.Time(0)
        private set

    private val events: MutableMap<SysWait.Event, SysWait.Time> = LinkedHashMap()

    private val signals: MutableSet<SysSignal<*>> = LinkedHashSet()

    private val buses: MutableSet<SysBus<*>> = LinkedHashSet()

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

    internal fun register(function: SysFunction) {
        newFunctions[function] = convert(function.sensitivities)
    }

    internal fun happens(event: SysWait.Event, delay: Long) {
        events[event] = SysWait.Time(delay)
    }

    private fun resetEvents() {
        events.keys.forEach {
            if (events[it]!! <= currentTime)
                events[it] = SysWait.Time.INFINITY
        }
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

    private fun happened(wait: SysWait, events: Set<SysWait?>): Boolean =
        when (wait) {
            is SysWait.Initialize -> wait in events
            is SysWait.Event -> wait in events
            is SysWait.Finder -> wait() in events
            is SysWait.Time -> wait <= currentTime
            is SysWait.OneOf -> wait.elements.any { happened(it, events) }
            else -> false
        }

    private fun time(wait: SysWait): SysWait.Time =
        when (wait) {
            is SysWait.Event -> if (events[wait]!! <= currentTime) currentTime else SysWait.Time.INFINITY
            is SysWait.Finder -> wait()?.let { if (events[it]!! <= currentTime) currentTime else null} ?: SysWait.Time.INFINITY
            is SysWait.Time -> wait
            is SysWait.OneOf -> wait.elements.map { time(it) }.min() ?: SysWait.Time.INFINITY
            else -> SysWait.Time.INFINITY
        }

    private var stopRequested = false

    fun start(endTime: SysWait.Time = SysWait.Time.INFINITY) {
        stopRequested = false
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
                if (happened(wait, happenedEvents)) {
                    functionActivated = true
                    sensitivities = convert(function.run(wait))
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
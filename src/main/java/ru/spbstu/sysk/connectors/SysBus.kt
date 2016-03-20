package ru.spbstu.sysk.connectors

import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.*
import java.util.*

abstract class SysBus<T : SysData> internal constructor(
        name: String, private val scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    init {
        scheduler.register(this)
    }

    protected val signals: MutableList<SysSignal<T>> = ArrayList()

    protected final val changeEvent = SysWait.Event("changeEvent", scheduler)

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
    }

    protected open fun addWire(startValue: T) {
        assert(scheduler.stopRequested) { "Impossible to add the wire in bus while running the scheduler" }
        signals.add(SysSignal((signals.size).toString(), startValue, scheduler))
    }

    abstract fun set(value: T, index: Int, port: SysPort<*>)

    internal operator fun get(index: Int): T {
        if (index >= signals.size || index < 0) {
            throw IllegalArgumentException(
                    "SysBus $name: Wire with index $index is not found.")
        }
        return signals[index].value
    }

    internal abstract fun update()
}

open class SysBitBus internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysBit>(name, scheduler, parent) {

    private val ports: MutableMap<SysPort<*>, MutableList<SysBit>> = HashMap()

    var changed = false
        private set

    override fun register(port: SysPort<*>) {
        val list = ArrayList<SysBit>()
        for (i in signals.indices) list.add(SysBit.Z)
        ports.put(port, list)
        for (i in signals.indices) update(i)
    }

    fun addWire() {
        super.addWire(SysBit.X)
        ports.forEach { it.value.add(SysBit.Z) }
        if (!ports.isEmpty()) update(signals.size - 1)
    }

    override fun set(value: SysBit, index: Int, port: SysPort<*>) {
        ports[port]!![index] = value
        update(index)
    }

    private fun update(index: Int) {
        var value: SysBit = SysBit.Z
        ports.forEach { value = value.wiredAnd(it.value[index]) }
        signals[index].value = value;
        if (signals[index].changed) changed = true
    }

    override fun update() {
        signals.forEach { it.update() }
        if (changed) changeEvent.happens()
        changed = false
    }
}

open class SysPriorityBus<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysPriorityValue<T>>(name, scheduler, parent) {

    private val priority: MutableList<Int> = ArrayList()

    var changed = false
        private set

    override public fun addWire(startValue: SysPriorityValue<T>) {
        super.addWire(startValue)
        priority.add(startValue.priority)
    }

    override fun set(value: SysPriorityValue<T>, index: Int, port: SysPort<*>) {
        if (this.priority[index] < value.priority) {
            this.priority[index] = value.priority
            signals[index].value = value
            if (signals[index].changed) changed = true
        }
    }

    override fun update() {
        for (i in priority.indices) {
            priority[i] = 0
            signals[i].update()
        }
        if (changed) changeEvent.happens()
        changed = false
    }
}

class SysPriorityValue<T : SysData>(val priority: Int, val value: T) : SysData {
    operator fun invoke() = value
}

open class SysFifoBus<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<T>(name, scheduler, parent) {

    private val fifo: MutableList<Queue<T>> = ArrayList()

    override public fun addWire(startValue: T) {
        super.addWire(startValue)
        fifo.add(LinkedList())
    }

    override fun set(value: T, index: Int, port: SysPort<*>) {
        fifo[index].add(value);
    }

    override fun update() {
        for (i in signals.indices)
            if (!fifo[i].isEmpty()) {
                signals[i].value = fifo[i].element()
                fifo[i].remove()
                changeEvent.happens()
            }
        signals.forEach { it.update() }
    }
}
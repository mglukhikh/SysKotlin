package ru.spbstu.sysk.connectors

import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.*
import java.util.*

abstract class SysBus<T : SysData> internal constructor(
        val capacity: Int, startValues: Array<T>, name: String, private val scheduler: SysScheduler,
        parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    protected val signals: MutableList<SysSignal<T>> = ArrayList()

    protected final val changeEvent = SysWait.Event("changeEvent", scheduler)

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
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

    init {
        if (capacity != startValues.size)
            throw IllegalArgumentException("The number of start values must be equal to the capacity of the bus $name")
        scheduler.register(this)
        for (i in 0..capacity - 1)
            signals.add(SysSignal(i.toString(), startValues[i], scheduler))
    }
}

open class SysBitBus internal constructor(
        capacity: Int, name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysBit>(capacity, Array(capacity, { SysBit.X }), name, scheduler, parent) {

    private val ports: MutableMap<SysPort<*>, MutableList<SysBit>> = HashMap()

    var changed = false
        private set

    override fun register(port: SysPort<*>) {
        val list = ArrayList<SysBit>()
        for (i in signals.indices) list.add(SysBit.Z)
        ports.put(port, list)
        for (i in signals.indices) update(i)
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
        capacity: Int, startValues: Array<SysPriorityValue<T>>, name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysPriorityValue<T>>(capacity, startValues, name, scheduler, parent) {

    private val priority: MutableList<Int> = ArrayList()

    var changed = false
        private set

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

    init {
        for (i in 0..capacity - 1) {
            priority.add(startValues[i].priority)
        }
    }
}

class SysPriorityValue<T : SysData>(val priority: Int, val value: T) : SysData {
    operator fun invoke() = value
}

open class SysFifoBus<T : SysData> internal constructor(
        capacity: Int, startValues: Array<T>, name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<T>(capacity, startValues, name, scheduler, parent) {

    private val fifo: MutableList<Queue<T>> = ArrayList()

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

    init {
        for (i in 0..capacity - 1) fifo.add(LinkedList())
    }
}
package sysk

import java.util.*

/** ToDo: impossibility a create objects of this class. */
open class SysBus<T> constructor(
        name: String, private val scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    init {
        scheduler.register(this)
    }

    protected val signal: ArrayList<SysSignal<T>> = ArrayList()

    protected final val changeEvent = SysWait.Event("changeEvent", scheduler, this)

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
    }

    open fun addWire(startValue: T) {
        signal.add(SysSignal<T>((signal.size).toString(), startValue, scheduler, this))
    }

    open fun set(value: T, index: Int) {
        throw UnsupportedOperationException(
                "SysBus $name: set(index: Int) is not supported for internal SysBus")
    }

    internal operator fun get(index: Int): T {
        if (index >= signal.size || index < 0) {
            throw IllegalArgumentException(
                    "SysBus $name: Wire with index $index is not found.")
        }
        return signal[index].value
    }

    internal open fun update() {
        throw UnsupportedOperationException(
                "SysBus $name: Update is not supported for internal SysBus")
    }
}

open class SysWireBus constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysWireState>(name, scheduler, parent) {

    private val change: MutableList<Boolean> = ArrayList()

    override fun addWire(startValue: SysWireState) {
        super.addWire(startValue)
        change.add(false)
    }

    override fun set(value: SysWireState, index: Int) {
        if (!change[index]) {
            signal[index].value = value
            change[index] = true
        } else if (value != signal[index].value) {
            signal[index].value = SysWireState.X
        }
    }

    override fun update() {
        for (i in change.indices) change[i] = false
        signal.forEach { it.update() }
    }
}

open class SysPriorityBus<T> constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysPriorityValue<T>>(name, scheduler, parent) {

    private val priority: MutableList<Int> = ArrayList()

    override fun addWire(startValue: SysPriorityValue<T>) {
        super.addWire(startValue)
        priority.add(startValue.priority)
    }

    override fun set(value: SysPriorityValue<T>, index: Int) {
        if (this.priority[index] < value.priority) {
            this.priority[index] = value.priority
            signal[index].value = value
        }
    }

    override fun update() {
        for (i in priority.indices) {
            priority[i] = 0
            signal[i].update()
        }
    }
}

class SysPriorityValue<T> constructor(final val priority: Int, final val value: T) {}

open class SysFifoBus<T> constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<T>(name, scheduler, parent) {

    private val fifo: MutableList<Queue<T>> = ArrayList()

    override fun addWire(startValue: T) {
        super.addWire(startValue)
        fifo.add(LinkedList())
    }

    override fun set(value: T, index: Int) {
        fifo[index].add(value);
    }

    override fun update() {
        for (i in signal.indices)
            if (!fifo[i].isEmpty()) {
                signal[i].value = fifo[i].element()
                fifo[i].remove()
            }
        signal.forEach { it.update() }
    }
}
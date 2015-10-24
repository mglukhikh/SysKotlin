package sysk

import java.util.*

/** ToDo: impossibility a create objects of this class. */
open class SysBus<T> constructor(
        name: String, private val scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    init {
        scheduler.register(this)
    }

    protected val signals: ArrayList<SysSignal<T>> = ArrayList()

    protected final val changeEvent = SysWait.Event("changeEvent", scheduler, this)

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
    }

    open fun addWire(startValue: T) {
        signals.add(SysSignal<T>((signals.size).toString(), startValue, scheduler, this))
    }

    open fun set(value: T, index: Int) {
        throw UnsupportedOperationException(
                "SysBus $name: set(index: Int) is not supported for internal SysBus")
    }

    internal operator fun get(index: Int): T {
        if (index >= signals.size || index < 0) {
            throw IllegalArgumentException(
                    "SysBus $name: Wire with index $index is not found.")
        }
        return signals[index].value
    }

    internal open fun update() {
        throw UnsupportedOperationException(
                "SysBus $name: Update is not supported for internal SysBus")
    }
}

open class SysWireBus constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysWireState>(name, scheduler, parent) {

    private val change: ArrayList<Boolean> = ArrayList()

    override fun addWire(startValue: SysWireState) {
        super.addWire(startValue)
        change.add(false)
    }

    override fun set(value: SysWireState, index: Int) {
        if (!change[index]) {
            signals[index].value = value
            change[index] = true
            changeEvent.happens()
        } else if (value != signals[index].value) {
            signals[index].value = SysWireState.X
            changeEvent.happens()
        }
    }

    override fun update() {
        for (i in change.indices) change[i] = false
        signals.forEach { it.update() }
    }
}

open class SysPriorityBus<T> constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<T>(name, scheduler, parent) {

    override fun set(value: T, index: Int) {

    }

    override fun update() {

    }
}

open class SysFifoBus<T> constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<T>(name, scheduler, parent) {

    override fun set(value: T, index: Int) {

    }

    override fun update() {

    }
}
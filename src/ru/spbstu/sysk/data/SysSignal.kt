package ru.spbstu.sysk.data

import ru.spbstu.sysk.core.SysFunction
import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysWait

open class SysSignal<T : SysData> internal constructor(
        name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null
): SysSignalRead<T>, SysSignalWrite<T>, SysObject(name, parent) {

    /** Current signal value (backing field) */
    private var storedValue = startValue

    /** Current signal value */
    override var value: T
        get() = storedValue
        set(value) = write(value)

    /** Signal value for the next delta-cycle */
    protected var nextValue = startValue

    /** Is value going to change or not */
    var changed: Boolean = false
        private set

    private val changeEvent: SysWait.Event = SysWait.Event("changeEvent", scheduler)

    init {
        scheduler.register(this)
    }

    override fun read() = value

    override fun write(value: T) {
        nextValue = value
        changed = (value != this.value)
    }

    override fun register(port: SysPort<*>) {

    }

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun toString(): String {
        return if (changed) "$value($nextValue)" else "$value"
    }

    /** Proceed to the next delta cycle */
    internal open fun update() {
        if (changed) {
            changeEvent.happens()
        }
        storedValue = nextValue
        changed = false
    }
}

open class SysBitSignal internal constructor(
        name: String, scheduler: SysScheduler, startValue: SysBit = SysBit.X, parent: SysObject? = null
): SysSignal<SysBit>(name, startValue, scheduler, parent), SysBitRead {

    override val posEdgeEvent = SysWait.Event("posEdgeEvent", scheduler)

    override val negEdgeEvent = SysWait.Event("negEdgeEvent", scheduler)

    val zero: Boolean
        get() = value.zero

    val one: Boolean
        get() = value.one

    val x: Boolean
        get() = value.x

    override fun update() {
        if (changed) {
            if (value.one && nextValue.zero) {
                negEdgeEvent.happens()
            }
            else if (value.zero && nextValue.one){
                posEdgeEvent.happens()
            }
        }
        super.update()
    }
}

class SysClockedSignal internal constructor(
        name: String,
        public val period: SysWait.Time,
        private val scheduler: SysScheduler,
        startValue: SysBit = SysBit.ZERO,
        parent: SysObject? = null
): SysBitSignal(name, scheduler, startValue, parent) {

    protected inner class SysClockedSignalFunction :
            SysFunction(period / 2, initialize = false) {

        override fun run(event: SysWait): SysWait {
            value = !value
            return wait()
        }
    }

    init {
        scheduler.register(SysClockedSignalFunction())
    }
}
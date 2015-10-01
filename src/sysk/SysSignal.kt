package sysk

open class SysSignal<T> internal constructor(
        name: String, startValue: T, parent: SysObject? = null
): SysSignalRead<T>, SysSignalWrite<T>, SysObject(name, parent) {

    /** Current signal value */
    override var value: T = startValue
        set(value) = write(value)

    /** Signal value for the next delta-cycle */
    protected var nextValue: T = startValue

    /** Is value going to change or not */
    var changed: Boolean = false
        private set

    private val changeEvent: SysWait.Event = SysWait.Event("changeEvent", this)

    init {
        SysScheduler.register(this)
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

    /** Compare value with another signal (only current value is important here) */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? SysSignal<*>)?.let { value == it.value } ?: return false
    }

    override fun toString(): String {
        return if (changed) "$value($nextValue)" else "$value"
    }

    /** Proceed to the next delta cycle */
    internal open fun update() {
        if (changed) {
            changeEvent.happens()
        }
        $value = nextValue
        changed = false
    }
}

open class SysWireSignal internal constructor(
        name: String, startValue: SysWireState = SysWireState.X, parent: SysObject? = null
): SysSignal<SysWireState>(name, startValue, parent), SysWireRead {

    override val posEdgeEvent = SysWait.Event("posEdgeEvent", this)

    override val negEdgeEvent = SysWait.Event("negEdgeEvent", this)

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
        startValue: SysWireState = SysWireState.ZERO,
        parent: SysObject? = null
): SysWireSignal(name, startValue, parent) {

    init {
        object: SysFunction(SysWait.Time(period.femtoSeconds / 2), initialize = false) {
            override fun run(initialization: Boolean): SysWait {
                value = !value
                return wait()
            }
        }
    }
}
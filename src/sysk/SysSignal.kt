package sysk

internal open class SysSignal<T>(name: String, startValue: T, parent: SysObject? = null):
        SysSignalRead<T>, SysSignalWrite<T>, SysObject(name, parent) {

    /** Current signal value */
    override public var value: T = startValue
        set(value) = write(value)

    /** Signal value for the next delta-cycle */
    private var nextValue: T = startValue

    /** Is value going to change or not */
    public var changed: Boolean = false
        private set

    private val changeEvent: SysWait.Event = SysWait.Event("changeEvent", this)

    init {
        SysScheduler.register(this)
    }

    override public fun read() = value

    override public fun write(value: T) {
        nextValue = value
        changed = (value != this.value)
    }

    override public fun register(port: SysPort<*>) {

    }

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    /** Compare value with another signal (only current value is important here) */
    override public fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? SysSignal<*>)?.let { value == it.value } ?: return false
    }

    override public fun toString(): String {
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

internal open class SysBooleanSignal(name: String, startValue: Boolean, parent: SysObject? = null):
        SysSignal<Boolean>(name, startValue, parent), SysBooleanRead {

    override val posEdgeEvent = SysWait.Event("posEdgeEvent", this)

    override val negEdgeEvent = SysWait.Event("negEdgeEvent", this)

    override fun update() {
        if (changed) {
            if (value) {
                negEdgeEvent.happens()
            }
            else {
                posEdgeEvent.happens()
            }
        }
        super<SysSignal>.update()
    }
}

internal class SysClockedSignal(name: String, startValue: Boolean, public val period: SysWait.Time, parent: SysObject? = null):
        SysBooleanSignal(name, startValue, parent) {

    init {
        object: SysFunction(SysWait.Time(period.femtoSeconds / 2), initialize = false) {
            override fun run(initialization: Boolean): SysWait {
                value = !value
                return wait()
            }
        }
    }
}
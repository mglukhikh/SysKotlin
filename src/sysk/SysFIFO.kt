package sysk;
import java.util.*

open class SysFIFO<T>(open val capacity: Int, name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null):
        SysSignalRead<T>, SysObject(name, parent) {

    private val changeEvent = SysWait.Event("changeEvent", scheduler, this)

    protected var fifo: Queue<T> = LinkedList()

    /** Head of queue */
    override var value = startValue // Excess
        private set

    /** Interface*/
    // TODO: rename to output?
    var input: T
        get() = throw UnsupportedOperationException("Input property is write-only for FIFO")
        set(value) = push(value)

    // TODO: rename to input?
    val output: T
        get() = value

    val size: Int
        get() = fifo.size

    val empty: Boolean
        get() = fifo.isEmpty()

    val full: Boolean
        get() = fifo.size == capacity

    open fun pop() { // How to convert in the class field?
        if (!fifo.isEmpty()) fifo.remove()
        if (!fifo.isEmpty() && value != fifo.element()) {
            value = fifo.element()
            changeEvent.happens()
        }
    }

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    fun push(value: T) {
        if (!full) fifo.add(value)
        this.value = fifo.element()
    }

    override fun read() = value // Excess

    override fun register(port: SysPort<*>) { }

    override fun toString() = fifo.toString()
}

open class SysWireFIFO(capacity: Int, name: String, scheduler: SysScheduler, parent: SysObject? = null)
: SysFIFO<SysWireState>(capacity, name, SysWireState.X, scheduler, parent), SysWireRead {

    override val posEdgeEvent = SysWait.Event("posEdgeEvent", scheduler, this)

    override val negEdgeEvent = SysWait.Event("negEdgeEvent", scheduler, this)

    val zero: Boolean
        get() = value.zero

    val one: Boolean
        get() = value.one

    val x: Boolean
        get() = value.x

    override fun pop() {
        val prevValue = this.value;
        super.pop();
        if (prevValue.one && value.zero) {
            negEdgeEvent.happens()
        }
        else if (prevValue.zero && value.one){
            posEdgeEvent.happens()
        }
    }
}

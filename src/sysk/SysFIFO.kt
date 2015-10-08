package sysk;
import java.util.*

open class SysFIFO<T>(open val capacity: Int, name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null):
        SysSignalRead<T>, SysObject(name, parent) {

    private val changeEvent = SysWait.Event("changeEvent", scheduler, this);

    protected var fifo: Queue<T> = LinkedList();

    /** Head of queue */
    override var value = startValue; // Excess
        private set;

    /** Interface*/
    public var input: T = startValue
        set(value) = push(value); // How make a private get?

    public val output: T
        get() = value;

    public val size: Int
        get() = fifo.size;

    public val empty: Boolean
        get() = (fifo.isEmpty());

    public val full: Boolean
        get() = (fifo.size == capacity);

    open fun pop() { // How to convert in the class field?
        if (!fifo.isEmpty()) fifo.remove();
        if (!fifo.isEmpty()
                && (value != fifo.element())) {
            value = fifo.element();
            changeEvent.happens();
        }
    }

    override val defaultEvent: SysWait.Event
        get() = changeEvent;

    public fun push(value: T) {
        if (!full) fifo.add(value);
        this.value = fifo.element();
    }

    override fun read() = value; // Excess

    override fun register(port: SysPort<*>) { }

    override fun toString() = fifo.toString();
}

open class SysWireFIFO(capacity: Int, name: String, scheduler: SysScheduler, parent: SysObject? = null)
: SysFIFO<SysWireState>(capacity, name, SysWireState.X, scheduler, parent), SysWireRead {

    override val posEdgeEvent = SysWait.Event("posEdgeEvent", scheduler, this)

    override val negEdgeEvent = SysWait.Event("negEdgeEvent", scheduler, this)

    val zero: Boolean
        get() = value.zero;

    val one: Boolean
        get() = value.one;

    val x: Boolean
        get() = value.x;

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

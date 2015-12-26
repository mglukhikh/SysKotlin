package sysk

import java.util.*

open class SysFifo<T : SysData> internal constructor(
        val capacity: Int, name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    private var counterSysFifoInputs: Int = 0

    protected val changeEvent = SysWait.Event("changeEvent", scheduler)

    protected var fifo: Queue<T> = LinkedList()

    var input: T = startValue
        set(value) {
            field = value;
        }

    var output: T = startValue
        private set

    val size: Int
        get() = fifo.size

    val empty: Boolean
        get() = fifo.isEmpty()

    val full: Boolean
        get() = fifo.size == capacity

    open var push = SysBit.X
        set(value) = push()

    open var pop = SysBit.X
        set(value) = pop()

    internal open fun pop() {
        if (!fifo.isEmpty()) fifo.remove()
        if (!fifo.isEmpty() && output != fifo.element()) {
            this.output = fifo.element()
            changeEvent.happens()
        }
    }

    internal open fun push() {
        if (!full) fifo.add(input)
        if ((size - 1) == 0) {
            this.output = fifo.element()
            changeEvent.happens()
        }
    }

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
        if (port is SysFifoOutput<*>) ++counterSysFifoInputs
        if (counterSysFifoInputs > 1)
            throw IllegalStateException("SysFifo $name may have only one output port.")
    }

    override fun toString() = fifo.toString()
}

open class SysWireFifo internal constructor(
        capacity: Int, name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysFifo<SysBit>(capacity, name, SysBit.X, scheduler, parent) {

    val posEdgeEvent = SysWait.Event("posEdgeEvent", scheduler)

    val negEdgeEvent = SysWait.Event("negEdgeEvent", scheduler)

    val zero: Boolean
        get() = output.zero

    val one: Boolean
        get() = output.one

    val x: Boolean
        get() = output.x

    override fun pop() {
        val prevValue = this.output
        super.pop()
        if (prevValue.one && output.zero) {
            negEdgeEvent.happens()
        } else if (prevValue.zero && output.one) {
            posEdgeEvent.happens()
        }
    }
}

open class SysAsynchronousFifo<T : SysData> internal constructor(
        capacity: Int, name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null
) : SysFifo<T>(capacity, name, startValue, scheduler, parent) {

    override var push = SysBit.X
        get() = throw UnsupportedOperationException(
                "SysAsynchronousFifo $name: Read is not supported for push port.")
        set(value) {
            if (field.zero && value.one) push()
            field = value;
        }

    override var pop = SysBit.X
        get() = throw UnsupportedOperationException(
                "SysAsynchronousFifo $name: Read is not supported for pop port.")
        set(value) {
            if (field.zero && value.one) pop()
            field = value;
        }
}


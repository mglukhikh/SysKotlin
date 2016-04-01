package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.data.*
import java.util.*

open class SysFifo<T : SysData> internal constructor(
        val capacity: Int, name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    private var counterSysFifoInputs: Int = 0
    private var counterSysFifoOutputs: Int = 0

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

    open var push = SysBit.undefined
        set(value) = push()

    open var pop = SysBit.undefined
        set(value) = pop()

    protected open fun pop() {
        if (!fifo.isEmpty()) fifo.remove()
        if (!fifo.isEmpty() && output != fifo.element()) {
            this.output = fifo.element()
            changeEvent.happens()
        }
    }

    protected open fun push() {
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
        if (port is SysFifoInput<*>) ++counterSysFifoOutputs
        if (counterSysFifoOutputs > 1)
            throw IllegalStateException("SysFifo $name may have only one input port.")
    }

    override fun toString() = fifo.toString()
}

open class SysBitFifo internal constructor(
        capacity: Int, name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysFifo<SysBit>(capacity, name, SysBit.undefined, scheduler, parent), SysEdged {

    override val posEdgeEvent = SysWait.Event("posEdgeEvent", scheduler)

    override val negEdgeEvent = SysWait.Event("negEdgeEvent", scheduler)

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

open class SysSynchronousFifo<T : SysData> internal constructor(
        capacity: Int, name: String, startValue: T, positive: Boolean, private val scheduler: SysScheduler, parent: SysObject? = null
) : SysFifo<T>(capacity, name, startValue, scheduler, parent) {

    val clk = SysBitInput(name, scheduler, this)

    override var push = SysBit.undefined
        set(value) {
            field = value
        }

    override var pop = SysBit.undefined
        set(value) {
            field = value
        }

    internal val update: (SysWait) -> Any = {
        if (push.one) push()
        if (pop.one) pop()
    }

    private class function(
            private val f: (SysWait) -> Any,
            sensitivities: SysWait,
            initialize: Boolean
    ) : SysFunction(sensitivities, initialize = initialize) {
        override fun run(event: SysWait): SysWait = (f(event) as? SysWait)?.let { it } ?: wait()
    }

    init {
        scheduler.register(function(update, if (positive) clk.posEdgeEvent else clk.negEdgeEvent, initialize = false))
    }
}

open class SysSynchronousBitFifo internal constructor(
        capacity: Int, name: String, positive: Boolean, startValue: SysBit, scheduler: SysScheduler, parent: SysObject? = null
) : SysSynchronousFifo<SysBit>(capacity, name, startValue, positive, scheduler, parent), SysEdged {

    override val posEdgeEvent = SysWait.Event("posEdgeEvent", scheduler)

    override val negEdgeEvent = SysWait.Event("negEdgeEvent", scheduler)

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

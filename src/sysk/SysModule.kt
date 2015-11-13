package sysk

import java.util.*

open class SysModule internal constructor(
        name: String, val scheduler: SysScheduler, parent: SysModule? = null
) : SysObject(name, parent) {

    constructor(name: String, parent: SysModule) : this(name, parent.scheduler, parent)

    private val functions: MutableList<SysFunction> = ArrayList()

    // A set of helper functions

    protected val currentTime: SysWait.Time
        get() = scheduler.currentTime

    protected class SysModuleFunction(
            private val f: (SysWait) -> SysWait,
            sensitivities: SysWait,
            initialize: Boolean
    ) : SysFunction(sensitivities, initialize = initialize) {
        override fun run(event: SysWait): SysWait = f(event)
    }

    protected fun function(run: (SysWait) -> SysWait,
                           sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysFunction {
        return SysModuleFunction(run, sensitivities, initialize).register()
    }

    protected class SysModuleTriggeredFunction(
            private val f: (SysWait) -> SysWait,
            trigger: SysWait,
            sensitivities: SysWait,
            initialize: Boolean
    ) : SysTriggeredFunction(trigger, listOf(sensitivities), initialize) {

        constructor(
                f: (SysWait) -> SysWait,
                clock: SysClockedSignal,
                positive: Boolean,
                sensitivities: SysWait,
                initialize: Boolean
        ) : this(f, if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

        constructor(
                f: (SysWait) -> SysWait,
                clock: SysWireInput,
                positive: Boolean,
                sensitivities: SysWait,
                initialize: Boolean
        ) : this(f, if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

        override fun run(event: SysWait) = f(event)
    }

    protected fun triggeredFunction(run: (SysWait) -> SysWait,
                                    clock: SysClockedSignal, positive: Boolean = true,
                                    sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysTriggeredFunction {
        return SysModuleTriggeredFunction(run, clock, positive, sensitivities, initialize).register()
    }

    protected fun triggeredFunction(run: (SysWait) -> SysWait,
                                    clock: SysWireInput, positive: Boolean = true,
                                    sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysTriggeredFunction {
        return SysModuleTriggeredFunction(run, clock, positive, sensitivities, initialize).register()
    }

    private fun <T : SysFunction> T.register(): T {
        scheduler.register(this)
        functions.add(this)
        return this
    }

    protected fun <IF : SysInterface> port(name: String, sysInterface: IF? = null): SysPort<IF> =
            SysPort(name, this, sysInterface)

    protected fun <T> input(name: String, signalRead: SysSignalRead<T>? = null): SysInput<T> =
            SysInput(name, this, signalRead)

    protected fun wireInput(name: String, signalRead: SysWireRead? = null): SysWireInput =
            SysWireInput(name, this, signalRead)

    protected fun <T> output(name: String, signalWrite: SysSignalWrite<T>? = null): SysOutput<T> =
            SysOutput(name, this, signalWrite)

    protected fun <T> signal(name: String, startValue: T): SysSignal<T> =
            SysSignal(name, startValue, scheduler, this)

    protected fun wireSignal(name: String, startValue: SysWireState = SysWireState.X) =
            SysWireSignal(name, scheduler, startValue, this)

    protected fun clockedSignal(name: String, period: SysWait.Time, startValue: SysWireState = SysWireState.ZERO) =
            SysClockedSignal(name, period, scheduler, startValue)

    protected fun <T> fifoOutput(name: String, fifo: SysFifo<T>? = null) = SysFifoOutput<T>(name, this, fifo)

    protected fun <T> fifoInput(name: String, fifo: SysFifo<T>? = null) = SysFifoInput<T>(name, this, fifo)

    protected fun wireFifoOutput(name: String, fifo: SysWireFifo? = null) = SysFifoOutput(name, this, fifo)

    protected fun wireFifoInput(name: String, fifo: SysWireFifo? = null) = SysFifoInput(name, this, fifo)

    protected fun <T> fifo(capacity: Int, name: String, startValue: T) =
            SysFifo<T>(capacity, name, startValue, scheduler, this)

    protected fun wireFifo(capacity: Int, name: String) = SysWireFifo(capacity, name, scheduler, this)

    protected fun <T> asynchronousFifo(capacity: Int, name: String, startValue: T) =
            SysAsynchronousFifo<T>(capacity, name, startValue, scheduler, this)

    protected fun <T> busPort(name: String, bus: SysBus<T>? = null) = SysBusPort<T>(name, this, bus)

    protected fun wireBusPort(name: String, bus: SysWireBus? = null) = SysBusPort(name, this, bus)

    protected fun wireBus(name: String) = SysWireBus(name, scheduler, this)

    protected fun <T> priorityBus(name: String) = SysPriorityBus<T>(name, scheduler, this)

    protected fun <T> prioriteValue(priority: Int, value: T) = SysPriorityValue<T>(priority, value)

    protected fun <T> fifoBus(name: String) = SysFifoBus<T>(name, scheduler, this)

    protected fun event(name: String): SysWait.Event = SysWait.Event(name, scheduler, this)
}

open class SysTopModule(
        name: String = "top", scheduler: SysScheduler = SysScheduler()
) : SysModule(name, scheduler, null) {

    fun start(endTime: SysWait.Time = SysWait.Time.INFINITY) {
        scheduler.start(endTime)
    }
}
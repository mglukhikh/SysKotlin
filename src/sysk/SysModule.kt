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
            private val f: (SysWait) -> Any,
            sensitivities: SysWait,
            initialize: Boolean
    ) : SysFunction(sensitivities, initialize = initialize) {
        override fun run(event: SysWait): SysWait = run(event, wait(), f)
    }

    protected fun function(run: (SysWait) -> Any,
                           sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysFunction {
        return SysModuleFunction(run, sensitivities, initialize).register()
    }

    protected fun function(sensitivities: SysWait = SysWait.Never, initialize: Boolean = true,
                           run: (SysWait) -> Unit = {}) =
            function({ run(it) }, sensitivities, initialize)

    protected fun function(clock: SysEdged, positive: Boolean = true,
                           initialize: Boolean = true,
                           run: (SysWait) -> Unit = {}) =
            function(if (positive) clock.posEdgeEvent else clock.negEdgeEvent,
                     initialize, run)

    protected fun stagedFunction(sensitivities: SysWait = SysWait.Never,
                                 init: StagedFunction.() -> Unit): StagedFunction {
        val result = StagedFunction(sensitivities)
        result.init()
        return result.register()
    }

    protected fun stagedFunction(clock: SysEdged, positive: Boolean = true,
                                 init: StagedFunction.() -> Unit) =
            stagedFunction(if (positive) clock.posEdgeEvent else clock.negEdgeEvent, init)

    private fun <T : SysFunction> T.register(): T {
        scheduler.register(this)
        functions.add(this)
        return this
    }

    protected fun <T : SysData> input(name: String, signalRead: SysSignalRead<T>? = null): SysInput<T> =
            SysInput(name, this, signalRead)

    protected fun wireInput(name: String, signalRead: SysWireRead? = null): SysWireInput =
            SysWireInput(name, this, signalRead)

    protected fun <T : SysData> output(name: String, signalWrite: SysSignalWrite<T>? = null): SysOutput<T> =
            SysOutput(name, this, signalWrite)

    protected fun <T : SysData> signal(name: String, startValue: T): SysSignal<T> =
            SysSignal(name, startValue, scheduler, this)

    protected fun wireSignal(name: String, startValue: SysBit = SysBit.X) =
            SysWireSignal(name, scheduler, startValue, this)

    protected fun clockedSignal(name: String, period: SysWait.Time, startValue: SysBit = SysBit.ZERO) =
            SysClockedSignal(name, period, scheduler, startValue)

    protected fun <T : SysData> fifoOutput(name: String, fifo: SysFifo<T>? = null) = SysFifoOutput<T>(name, this, fifo)

    protected fun <T : SysData> fifoInput(name: String, fifo: SysFifo<T>? = null) = SysFifoInput<T>(name, this, fifo)

    protected fun wireFifoOutput(name: String, fifo: SysWireFifo? = null) = SysFifoOutput(name, this, fifo)

    protected fun wireFifoInput(name: String, fifo: SysWireFifo? = null) = SysFifoInput(name, this, fifo)

    protected fun <T : SysData> fifo(capacity: Int, name: String, startValue: T) =
            SysFifo<T>(capacity, name, startValue, scheduler, this)

    protected fun wireFifo(capacity: Int, name: String) = SysWireFifo(capacity, name, scheduler, this)

    protected fun <T : SysData> asynchronousFifo(capacity: Int, name: String, startValue: T) =
            SysAsynchronousFifo<T>(capacity, name, startValue, scheduler, this)

    protected fun <T : SysData> busPort(name: String, bus: SysBus<T>? = null) = SysBusPort<T>(name, this, bus)

    protected fun wireBusPort(name: String, bus: SysWireBus? = null) = SysBusPort(name, this, bus)

    protected fun wireBus(name: String) = SysWireBus(name, scheduler, this)

    protected fun <T : SysData> priorityBus(name: String) = SysPriorityBus<T>(name, scheduler, this)

    protected fun <T : SysData> priorityValue(priority: Int, value: T) = SysPriorityValue<T>(priority, value)

    protected fun <T : SysData> fifoBus(name: String) = SysFifoBus<T>(name, scheduler, this)

    protected fun event(name: String): SysWait.Event = SysWait.Event(name, scheduler)

    companion object {

        private fun run(event: SysWait, default: SysWait, f: (SysWait) -> Any) =
                (f(event) as? SysWait)?.let { it } ?: default

    }
}

open class SysTopModule(
        name: String = "top", scheduler: SysScheduler = SysScheduler()
) : SysModule(name, scheduler, null) {

    fun start(endTime: SysWait.Time = SysWait.Time.INFINITY) {
        scheduler.start(endTime)
    }
}
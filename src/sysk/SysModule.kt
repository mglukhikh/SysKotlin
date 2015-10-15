package sysk

import java.util.*

open class SysModule internal constructor(
        name: String, val scheduler: SysScheduler, parent: SysModule? = null
): SysObject(name, parent) {

    constructor(name: String, parent: SysModule): this(name, parent.scheduler, parent)

    private val functions: MutableList<SysFunction> = ArrayList()

    // A set of helper functions

    protected val currentTime: SysWait.Time
        get() = scheduler.currentTime

    protected class SysModuleFunction(
            private val f: (Boolean) -> SysWait,
            sensitivities: SysWait,
            initialize: Boolean
    ) : SysFunction(sensitivities, initialize = initialize) {
        override fun run(initialization: Boolean): SysWait = f(initialization)
    }

    protected fun function(run: (Boolean) -> SysWait,
                           sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysFunction {
        val f = SysModuleFunction(run, sensitivities, initialize)
        scheduler.register(f)
        functions.add(f)
        return f
    }

    protected class SysModuleTriggeredFunction(
            private val f: (Boolean) -> SysWait,
            trigger: SysWait,
            sensitivities: SysWait,
            initialize: Boolean
    ) : SysTriggeredFunction(trigger, listOf(sensitivities), initialize) {

        constructor(
                f: (Boolean) -> SysWait,
                clock: SysClockedSignal,
                positive: Boolean,
                sensitivities: SysWait,
                initialize: Boolean
        ): this(f, if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

        constructor(
                f: (Boolean) -> SysWait,
                clock: SysWireInput,
                positive: Boolean,
                sensitivities: SysWait,
                initialize: Boolean
        ): this(f, if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

        override fun run(initialization: Boolean) = f(initialization)
    }

    protected fun triggeredFunction(run: (Boolean) -> SysWait,
                                    clock: SysClockedSignal, positive: Boolean = true,
                                    sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysTriggeredFunction {
        val f = SysModuleTriggeredFunction(run, clock, positive, sensitivities, initialize)
        scheduler.register(f)
        functions.add(f)
        return f
    }

    protected fun triggeredFunction(run: (Boolean) -> SysWait,
                                    clock: SysWireInput, positive: Boolean = true,
                                    sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysTriggeredFunction {
        val f = SysModuleTriggeredFunction(run, clock, positive, sensitivities, initialize)
        scheduler.register(f)
        functions.add(f)
        return f
    }

    protected fun <IF: SysInterface> port(name: String, sysInterface: IF? = null): SysPort<IF> =
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

    protected fun event(name: String): SysWait.Event = SysWait.Event(name, scheduler, this)
}

open class SysTopModule(
        name: String = "top", scheduler: SysScheduler = SysScheduler()
): SysModule(name, scheduler, null) {

    fun start(endTime: SysWait.Time = SysWait.Time.INFINITY) {
        scheduler.start(endTime)
    }
}
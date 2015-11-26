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

    // TODO: both Stage && StagedFunction should be protected (IllegalAccessError BUG): see KT-10143

    sealed class Stage {

        class Atomic(val run: () -> SysWait) : Stage()

        class Complex(
                private val sensitivities: SysWait,
                private val stages: MutableList<Stage>
        ) : Stage() {

            fun complexStage(init: Stage.Complex.() -> Unit): Stage.Complex {
                val result = Stage.Complex(sensitivities, linkedListOf())
                result.init()
                return result
            }

            fun stage(f: () -> Unit): Stage.Atomic {
                val result = Stage.Atomic {
                    f()
                    sensitivities
                }
                stages.add(result)
                return result
            }

            var stageNumber = 0

            fun run(): SysWait {
                if (stageNumber < stages.size) {
                    stages[stageNumber++].let {
                        when (it) {
                            is Stage.Atomic -> return it.run()
                            is Stage.Complex -> return it.run()
                        }
                    }
                }
                else return SysWait.Never
            }
        }
    }

    class StagedFunction private constructor(
            private val stages: MutableList<Stage>,
            private var initStage: Stage.Atomic? = null,
            private var infiniteStage: Stage.Atomic? = null,
            sensitivities: SysWait = SysWait.Never
    ): SysFunction(sensitivities, initialize = true) {
        constructor(sensitivities: SysWait = SysWait.Never):
                this(linkedListOf(), null, null, sensitivities)

        fun complexStage(init: Stage.Complex.() -> Unit): Stage.Complex {
            val result = Stage.Complex(wait(), linkedListOf())
            result.init()
            return result
        }

        fun stage(f: () -> Unit): Stage.Atomic {
            val result = Stage.Atomic {
                f()
                wait()
            }
            stages.add(result)
            return result
        }

        fun initStage(f: () -> Unit): Stage.Atomic {
            initStage = Stage.Atomic {
                f()
                wait()
            }
            return initStage!!
        }

        fun infiniteStage(f: () -> Unit): Stage.Atomic {
            infiniteStage = Stage.Atomic {
                f()
                wait()
            }
            return infiniteStage!!
        }

        private fun init(): SysWait {
            return initStage?.run() ?: wait()
        }

        private fun infinite(): SysWait {
            // BUG: return infiniteStage?.run() ?: SysWait.Never, see KT-10142
            if (infiniteStage == null) return SysWait.Never
            return infiniteStage!!.run()
        }

        var stageNumber = 0

        override fun run(event: SysWait): SysWait {
            if (event == SysWait.Initialize) {
                return init()
            }
            else if (stageNumber < stages.size) {
                stages[stageNumber++].let {
                    when (it) {
                        is Stage.Atomic -> return it.run()
                        is Stage.Complex -> return it.run()
                    }
                }
            }
            else {
                return infinite()
            }
        }
    }

    protected fun stagedFunction(sensitivities: SysWait = SysWait.Never,
                                 init: StagedFunction.() -> Unit): StagedFunction {
        val result = StagedFunction(sensitivities)
        result.init()
        return result.register()
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
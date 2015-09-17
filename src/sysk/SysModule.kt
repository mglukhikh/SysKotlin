package sysk

import java.util.*

open class SysModule(name: String, parent: SysModule? = null): SysObject(name, parent) {

    internal val functions: MutableList<SysFunction> = ArrayList()

    // A set of helper functions

    protected fun function(run: (Boolean) -> SysWait,
                           sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysFunction {
        val f = object: SysFunction(sensitivities, initialize = initialize) {
            override fun run(initialization: Boolean) = run(initialization)
        }
        functions.add(f)
        return f
    }

    protected fun triggeredFunction(run: (Boolean) -> SysWait,
                                    clock: SysClockedSignal, positive: Boolean = true,
                                    sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysTriggeredFunction {
        val f = object: SysTriggeredFunction(clock, positive, listOf(sensitivities), initialize) {
            override fun run(initialization: Boolean) = run(initialization)
        }
        functions.add(f)
        return f
    }

    protected fun triggeredFunction(run: (Boolean) -> SysWait,
                                    clock: SysBooleanInput, positive: Boolean = true,
                                    sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysTriggeredFunction {
        val f = object: SysTriggeredFunction(clock, positive, listOf(sensitivities), initialize) {
            override fun run(initialization: Boolean) = run(initialization)
        }
        functions.add(f)
        return f
    }

    protected fun <IF: SysInterface> port(name: String, sysInterface: IF? = null): SysPort<IF> =
            SysPort(name, this, sysInterface)

    protected fun <T> input(name: String, signalRead: SysSignalRead<T>? = null): SysInput<T> =
            SysInput(name, this, signalRead)


    protected fun booleanInput(name: String, signalRead: SysBooleanRead? = null): SysBooleanInput =
            SysBooleanInput(name, this, signalRead)

    protected fun <T> output(name: String, signalWrite: SysSignalWrite<T>? = null): SysOutput<T> =
            SysOutput(name, this, signalWrite)

    protected fun <T> signal(name: String, startValue: T): SysSignal<T> =
            SysSignal(name, startValue, this)

    protected fun booleanSignal(name: String, startValue: Boolean): SysBooleanSignal =
            SysBooleanSignal(name, startValue, this)

    protected fun clockedSignal(name: String, startValue: Boolean, period: SysWait.Time): SysClockedSignal =
            SysClockedSignal(name, startValue, period)

    protected fun event(name: String): SysWait.Event = SysWait.Event(name, this)
}
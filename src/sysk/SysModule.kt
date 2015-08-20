package sysk

import java.util.*

public open class SysModule(name: String, parent: SysObject? = null): SysObject(name, parent) {

    internal val functions: MutableList<SysFunction> = ArrayList()

    // A set of helper functions

    protected fun function(run: (Boolean) -> SysWait, sensitivities: SysWait = SysWait.Never, initialize: Boolean = true) {
        functions.add(object: SysFunction(sensitivities, initialize = initialize) {
            override fun run(initialization: Boolean) = run(initialization)
        })
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

    protected fun event(name: String): SysWait.Event = SysWait.Event(name, this)
}
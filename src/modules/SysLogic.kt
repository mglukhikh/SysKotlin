package modules

import sysk.SysFunction
import sysk.SysModule
import sysk.SysWireState

open class SysUnaryModule<Input, Output>(
        operation: (Input) -> Output, name: String, parent: SysModule
): SysModule(name, parent) {

    open val x = input<Input>("x")

    val y = output<Output>("y")

    private val f: SysFunction = function({
        y.value = operation(x.value)
        f.wait()
    }, sensitivities = x.defaultEvent, initialize = false)
}

open class SysUnaryWireModule<Output>(
        operation: (SysWireState) -> Output, name: String, parent: SysModule
): SysUnaryModule<SysWireState, Output>(operation, name, parent) {

    override val x = wireInput("x")
}

class SysNotModule(name: String, parent: SysModule): SysUnaryWireModule<SysWireState>({ it.not() }, name, parent)

open class SysBinaryModule<Input1, Input2, Output>(
        operation: (Input1, Input2) -> Output, name: String, parent: SysModule
): SysModule(name, parent) {

    open val x1 = input<Input1>("x1")

    open val x2 = input<Input2>("x2")

    val y = output<Output>("y")

    private val f: SysFunction = function({
        y.value = operation(x1.value, x2.value)
        f.wait()
    }, sensitivities = x1.defaultEvent.or(x2.defaultEvent), initialize = false)
}

open class SysBinaryWireModule<Output>(
        operation: (SysWireState, SysWireState) -> Output, name: String, parent: SysModule
): SysBinaryModule<SysWireState, SysWireState, Output>(operation, name, parent) {

    override val x1 = wireInput("x1")

    override val x2 = wireInput("x2")
}

class SysOrModule(name: String, parent: SysModule):
        SysBinaryWireModule<SysWireState>({ x1, x2 -> x1.or(x2)}, name, parent)

class SysAndModule(name: String, parent: SysModule):
        SysBinaryWireModule<SysWireState>({ x1, x2 -> x1.and(x2)}, name, parent)

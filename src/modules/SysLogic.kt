package modules

import sysk.SysModule
import sysk.SysBit

open class SysUnaryModule<Input, Output>(
        operation: (Input) -> Output, name: String, parent: SysModule
): SysModule(name, parent) {

    protected open fun createInput(name: String) = input<Input>("x")

    val x = createInput("x")

    val y = output<Output>("y")

    init {
        function(sensitivities = x.defaultEvent, initialize = false) {
            y.value = operation(x.value)
        }
    }
}

open class SysUnaryWireModule<Output>(
        operation: (SysBit) -> Output, name: String, parent: SysModule
): SysUnaryModule<SysBit, Output>(operation, name, parent) {

    override fun createInput(name: String) = wireInput(name)
}

class SysNotModule(name: String, parent: SysModule): SysUnaryWireModule<SysBit>({ it.not() }, name, parent)

open class SysBinaryModule<Input1, Input2, Output>(
        operation: (Input1, Input2) -> Output, name: String, parent: SysModule
): SysModule(name, parent) {

    protected open fun createInput1(name: String) = input<Input1>(name)

    protected open fun createInput2(name: String) = input<Input2>(name)

    val x1 = createInput1("x1")

    val x2 = createInput2("x2")

    val y = output<Output>("y")

    init {
        function(sensitivities = x1.defaultEvent.or(x2.defaultEvent), initialize = false) {
            y.value = operation(x1.value, x2.value)
        }
    }
}

open class SysBinaryWireModule<Output>(
        operation: (SysBit, SysBit) -> Output, name: String, parent: SysModule
): SysBinaryModule<SysBit, SysBit, Output>(operation, name, parent) {

    override fun createInput1(name: String) = wireInput(name)

    override fun createInput2(name: String) = wireInput(name)
}

class SysOrModule(name: String, parent: SysModule):
        SysBinaryWireModule<SysBit>({ x1, x2 -> x1.or(x2) }, name, parent)

class SysAndModule(name: String, parent: SysModule):
        SysBinaryWireModule<SysBit>({ x1, x2 -> x1.and(x2) }, name, parent)

class SysAndNotModule(name: String, parent: SysModule):
        SysBinaryWireModule<SysBit>({ x1, x2 -> x1.and(x2).not() }, name, parent)
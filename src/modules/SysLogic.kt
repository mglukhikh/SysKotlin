package modules

import sysk.SysModule
import sysk.SysBit
import sysk.SysData

open class SysUnaryModule<Input : SysData, Output : SysData>(
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

open class SysUnaryBitModule<Output : SysData>(
        operation: (SysBit) -> Output, name: String, parent: SysModule
): SysUnaryModule<SysBit, Output>(operation, name, parent) {

    override fun createInput(name: String) = bitInput(name)
}

class SysNotModule(name: String, parent: SysModule): SysUnaryBitModule<SysBit>({ it.not() }, name, parent)

open class SysBinaryModule<Input1 : SysData, Input2 : SysData, Output : SysData>(
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

open class SysBinaryBitModule<Output : SysData>(
        operation: (SysBit, SysBit) -> Output, name: String, parent: SysModule
): SysBinaryModule<SysBit, SysBit, Output>(operation, name, parent) {

    override fun createInput1(name: String) = bitInput(name)

    override fun createInput2(name: String) = bitInput(name)
}

class SysOrModule(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1.or(x2) }, name, parent)

class SysAndModule(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1.and(x2) }, name, parent)

class SysAndNotModule(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1.and(x2).not() }, name, parent)
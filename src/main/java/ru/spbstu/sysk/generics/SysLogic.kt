package ru.spbstu.sysk.generics

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData

open class SysUnaryModule<Input : SysData, Output : SysData>(
        operation: (Input) -> Output, name: String, parent: SysModule
): SysModule(name, parent) {

    protected open fun createInput(name: String) = input<Input>("x")

    val x = createInput("x")

    val y = output<Output>("y")

    init {
        function(sensitivities = x.defaultEvent, initialize = false) {
            y(operation(x()))
        }
    }
}

open class SysUnaryBitModule<Output : SysData>(
        operation: (SysBit) -> Output, name: String, parent: SysModule
): SysUnaryModule<SysBit, Output>(operation, name, parent) {

    override fun createInput(name: String) = bitInput(name)
}

class NOT(name: String, parent: SysModule): SysUnaryBitModule<SysBit>({ !it }, name, parent)

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
            y(operation(x1(), x2()))
        }
    }
}

open class SysBinaryBitModule<Output : SysData>(
        operation: (SysBit, SysBit) -> Output, name: String, parent: SysModule
): SysBinaryModule<SysBit, SysBit, Output>(operation, name, parent) {

    override fun createInput1(name: String) = bitInput(name)

    override fun createInput2(name: String) = bitInput(name)
}

class AND(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1 and x2 }, name, parent)

class NAND(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> !(x1 and x2) }, name, parent)

class OR(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1 or x2 }, name, parent)

class NOR(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> !(x1 or x2) }, name, parent)

class XOR(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1 xor x2 }, name, parent)

class NXOR(name: String, parent: SysModule):
        SysBinaryBitModule<SysBit>({ x1, x2 -> !(x1 xor x2) }, name, parent)
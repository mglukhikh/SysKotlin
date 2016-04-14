package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned

class Core(parent: SysModule) : SysModule("i8080", parent) {

    private val ALU = ArithmeticLogicUnit(CAPACITY.DATA, CAPACITY.COMMAND, this)
    private val RF = RegisterFile(CAPACITY.DATA, CAPACITY.ADDRESS, CAPACITY.REGISTER, CAPACITY.COMMAND, this)
    private val OF = OperationFifo(CAPACITY.DATA, CAPACITY.COMMAND, this)

    val reset = bitInput("reset")
    val clk1 = bitInput("clk1")
    val clk2 = bitInput("clk2")

    val dataBus = signal<SysUnsigned>("dataBus")
    val addressBus = signal<SysUnsigned>("addressBus")

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")

    init {
        connector("connector", RF.out, ALU.A)
        connector("connector", RF.B, ALU.B)
        connector("connector", ALU.out, RF.inp)
        OF.data bind dataBus
    }
}

package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule

class Core(parent: SysModule) : SysModule("i8080", parent) {

    private val ALU = ArithmeticLogicUnit(CAPACITY.DATA, CAPACITY.COMMAND, this)
    private val RF = RegisterFile(CAPACITY.DATA, CAPACITY.ADDRESS, CAPACITY.REGISTER, CAPACITY.COMMAND, this)

    init {
        connector("connector", RF.out, ALU.A)
        connector("connector", RF.B, ALU.B)
        connector("connector", ALU.out, RF.inp)
    }
}

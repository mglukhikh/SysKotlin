package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned

class ArithmeticLogicUnit(
        capacityData: Int,
        capacityOperation: Int,
        parent: SysModule
) : SysModule("ArithmeticLogicUnit", parent) {

    val A = input<SysUnsigned>("A")
    val B = input<SysUnsigned>("B")
    val out = output<SysUnsigned>("out")
    val operation = input<COMMAND>("command")

    init {
        function(A.defaultEvent or B.defaultEvent or operation.defaultEvent) {
            if (A().width != capacityData) throw IllegalArgumentException()
            if (B().width != capacityData) throw IllegalArgumentException()
            if (operation().width != capacityOperation) throw IllegalArgumentException()
            when (operation()) {
                COMMAND.ADD -> out(A() + B())
                COMMAND.SUB -> out(A() - B())
                COMMAND.MUL -> out(A() * B())
                COMMAND.DIV -> out(A() / B())
                COMMAND.REM -> out(A() % B())
                COMMAND.SHL -> out(A() shl (B()).toInt())
                COMMAND.SHR -> out(A() shr (B()).toInt())
                else -> {}
            }
        }
    }
}


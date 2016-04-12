package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysInteger

class ArithmeticLogicUnit(
        capacityData: Int,
        capacityOperation: Int,
        parent: SysModule
) : SysModule("ArithmeticLogicUnit", parent) {

    val A = input<SysInteger>("A")
    val B = input<SysInteger>("B")
    val C = output<SysInteger>("C")
    val operation = input<COMMAND>("command")

    init {
        function(A.defaultEvent or B.defaultEvent or operation.defaultEvent) {
            if (A().width != capacityData) throw IllegalArgumentException()
            if (B().width != capacityData) throw IllegalArgumentException()
            if (operation().width != capacityOperation) throw IllegalArgumentException()
            when (operation()) {
                COMMAND.ADD -> C(A() + B())
                COMMAND.SUB -> C(A() - B())
                COMMAND.MUL -> C(A() * B())
                COMMAND.DIV -> C(A() / B())
                COMMAND.REM -> C(A() % B())
                COMMAND.SHL -> C(A() shl (B()).toInt())
                COMMAND.SHR -> C(A() shr (B()).toInt())
                else -> {}
            }
        }
    }
}


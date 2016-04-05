package ru.spbstu.sysk.samples.microprocessors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.SysLongInteger
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned

object OPERATION {
    val ADD = unsigned(3, 0)
    val SUB = unsigned(3, 1)
    val MUL = unsigned(3, 2)
    val DIV = unsigned(3, 3)
    val REM = unsigned(3, 4)
    val SHL = unsigned(3, 5)
    val SHR = unsigned(3, 6)
}

class ArithmeticLogicUnit(capacity: Int, name: String, parent: SysModule) : SysModule(name, parent) {

    val A = input<SysInteger>("A")
    val B = input<SysInteger>("B")
    val C = output<SysInteger>("C")
    val operation = input<SysUnsigned>("command")

    init {
        function(A.defaultEvent or B.defaultEvent or operation.defaultEvent) {
            if (A().width != capacity || B().width != capacity) throw IllegalArgumentException()
            when (operation()) {
                OPERATION.ADD -> C(A() as SysLongInteger + B() as SysLongInteger)
                OPERATION.SUB -> C(A() as SysLongInteger - B() as SysLongInteger)
                OPERATION.MUL -> C(A() as SysLongInteger * B() as SysLongInteger)
                OPERATION.DIV -> C(A() as SysLongInteger / B() as SysLongInteger)
                OPERATION.REM -> C(A() as SysLongInteger % B() as SysLongInteger)
                OPERATION.SHL -> C(A() as SysLongInteger shl (B() as SysLongInteger).toInt())
                OPERATION.SHR -> C(A() as SysLongInteger shr (B() as SysLongInteger).toInt())
            }
        }
    }
}


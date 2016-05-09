package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.integer.SysUnsigned

class ArithmeticLogicUnit(
        capacityData: Int,
        capacityCommand: Int,
        parent: SysModule
) : SysModule("ArithmeticLogicUnit", parent) {

    val A = input<SysUnsigned>("A")
    val B = input<SysUnsigned>("B")
    val out = output<SysUnsigned>("out")
    val operation = input<COMMAND>("command")
    val flag = bidirPort<SysUnsigned>("flag")

    private fun SysBit.toInt() = if (one) 1 else 0

    private fun add(transfer: Boolean, capacityData: Int) {
        val A = A().truncate(capacityData + 1) as SysUnsigned
        val B = B().truncate(capacityData + 1) as SysUnsigned
        val C = A + B + if (transfer) flag()[0].toInt() else 0
        out(C.truncate(capacityData) as SysUnsigned)
        flag(flag().set(0, C[capacityData]))
    }

    private fun sub(transfer: Boolean, capacityData: Int) {
        val A = A() + ((if (transfer) flag()[0].toInt() else 0) shl capacityData)
        val B = B()
        val C = A - B
        out(C.truncate(capacityData) as SysUnsigned)
        flag(flag().set(0, ZERO))
    }

    private fun setFlag(vararg pairs: Pair<Int, SysBit>) = pairs.forEach { flag(flag().set(it.first, it.second)) }

    init {
        function(A.defaultEvent or B.defaultEvent or operation.defaultEvent) {
            if (A().width != capacityData) throw IllegalArgumentException()
            if (B().width != capacityData) throw IllegalArgumentException()
            if (operation().width != capacityCommand) throw IllegalArgumentException()
            when (operation()) {
                COMMAND.ADD -> add(false, capacityData)
                COMMAND.ADC -> add(true, capacityData)
                COMMAND.SUB -> sub(false, capacityData)
                COMMAND.SBB -> sub(true, capacityData)
                COMMAND.MUL -> out(A() * B())
                COMMAND.DIV -> out(A() / B())
                COMMAND.REM -> out(A() % B())
                COMMAND.SHL -> out(A() shl (B()).toInt())
                COMMAND.SHR -> out(A() shr (B()).toInt())
                COMMAND.XRA -> {
                    out(A() xor B())
                    setFlag(0 to ZERO, 4 to ZERO)
                }
                COMMAND.ORA -> {
                    out(A() or B())
                    setFlag(0 to ZERO, 4 to ZERO)
                }
                COMMAND.ANA -> {
                    out(A() and B())
                    setFlag(0 to ZERO, 4 to ZERO)
                }
                COMMAND.CMP -> {
                    val comp = A().compareTo(B())
                    setFlag(0 to SysBit(comp == 0), 6 to SysBit(comp < 0))
                }
                else -> {
                }
            }
        }
    }
}


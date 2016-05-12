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
    val data = input<SysUnsigned>("data")
    val out = output<SysUnsigned>("out")
    val operation = input<COMMAND>("command")
    val flag = bidirPort<SysUnsigned>("flag")
    val outside = bitInput("outside")
    val en = bitInput("en")

    private fun SysBit.toInt() = if (one) 1 else 0

    private fun inp() = if (outside.one) data() else B()

    private fun add(transfer: Boolean, capacityData: Int) {
        val A = A().extend(capacityData + 1) as SysUnsigned
        val B = inp().extend(capacityData + 1) as SysUnsigned
        val C = A + B + if (transfer) flag()[0].toInt() else 0
        out(C.extend(capacityData) as SysUnsigned)
        flag(flag().set(0, C[capacityData]))
    }

    private fun sub(transfer: Boolean, capacityData: Int) {
        val A = A() + ((if (transfer) flag()[0].toInt() else 0) shl capacityData)
        val B = inp()
        val C = A - B
        out(C.extend(capacityData) as SysUnsigned)
        flag(flag().set(0, ZERO))
    }

    private fun setFlag(vararg pairs: Pair<Int, SysBit>) = pairs.forEach { flag(flag().set(it.first, it.second)) }

    init {
        function(A.defaultEvent or B.defaultEvent or operation.defaultEvent) {
            if (A().width != capacityData) throw IllegalArgumentException()
            if (inp().width != capacityData) throw IllegalArgumentException()
            if (operation().width != capacityCommand) throw IllegalArgumentException()
            if (en.one) when (operation()) {
                COMMAND.ADD -> add(false, capacityData)
                COMMAND.ADC -> add(true, capacityData)
                COMMAND.SUB -> sub(false, capacityData)
                COMMAND.SBB -> sub(true, capacityData)
                COMMAND.MUL -> out(A() * inp())
                COMMAND.DIV -> out(A() / inp())
                COMMAND.REM -> out(A() % inp())
                COMMAND.SHL -> out(A() shl (inp()).toInt())
                COMMAND.SHR -> out(A() shr (inp()).toInt())
                COMMAND.XRA -> {
                    out(A() xor inp())
                    setFlag(0 to ZERO, 4 to ZERO)
                }
                COMMAND.ORA -> {
                    out(A() or inp())
                    setFlag(0 to ZERO, 4 to ZERO)
                }
                COMMAND.ANA -> {
                    out(A() and inp())
                    setFlag(0 to ZERO, 4 to ZERO)
                }
                COMMAND.CMP -> {
                    val comp = A().compareTo(inp())
                    setFlag(0 to SysBit(comp == 0), 6 to SysBit(comp < 0))
                }
                else -> {
                }
            }
        }
    }
}


package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned

class CommandRegister constructor(
        private val capacityData: Int,
        private val maxAddress: Int,
        capacityAddress: Int,
        capacityCommand: Int,
        parent: SysModule
) : SysModule("CommandRegister", parent) {

    private var memory = Array(maxAddress + 1, { unsigned(capacityData, 0) })

    val inp = bidirPort<SysUnsigned>("inp")
    val out = bidirPort<SysUnsigned>("out")
    val address = input<SysUnsigned>("address")
    val command = input<COMMAND>("command")
    val en = bitInput("en")
    val clk = bitInput("clk")

    init {
        stateFunction(clk) {
            infiniteState {
                if (command().width != capacityCommand) throw IllegalArgumentException()
                if (inp().width != capacityData) throw IllegalArgumentException()
                if (address().width != capacityAddress) throw IllegalArgumentException()
                if (en().one) when (command()) {
                    COMMAND.WRITE -> memory[address().toInt()] = inp()
                    COMMAND.READ -> out(memory[address().toInt()])
                    COMMAND.RESET -> reset()
                    else -> { }
                }
            }
        }
    }

    private fun reset() {
        memory = Array(maxAddress + 1, { unsigned(capacityData, 0) })
    }
}

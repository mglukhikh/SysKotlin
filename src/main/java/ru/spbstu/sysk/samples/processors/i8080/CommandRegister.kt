package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned
import ru.spbstu.sysk.samples.processors.i8080.MainConstants.COMMAND

class CommandRegister constructor(
        val capacityData: Int,
        val maxAddress: Int,
        capacityAddress: Int,
        capacityCommand: Int,
        parent: SysModule
) : SysModule("CommandRegister", parent) {

    private var memory = Array(maxAddress + 1, { unsigned(capacityData, 0) })

    val data = bidirPort<SysUnsigned>("data")
    val address = input<SysUnsigned>("address")
    val command = input<SysUnsigned>("command")

    val en = bitInput("en")
    val clk = bitInput("clk")

    init {
        stateFunction(clk) {
            infiniteState {
                if (command().width != capacityCommand) throw IllegalArgumentException()
                if (data().width != capacityData) throw IllegalArgumentException()
                if (address().width != capacityAddress) throw IllegalArgumentException()
                if (en().one) when (command()) {
                    COMMAND.WRITE -> memory[address().toInt()] = data()
                    COMMAND.READ -> data(memory[address().toInt()])
                    COMMAND.RESET -> reset()
                }
            }
        }
    }

    private fun reset() { memory = Array(maxAddress + 1, { unsigned(capacityData, 0) }) }
}

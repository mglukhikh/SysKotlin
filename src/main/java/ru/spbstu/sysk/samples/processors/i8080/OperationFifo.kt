package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import java.util.*

class OperationFifo(
        capacityData: Int,
        capacityCommand: Int,
        parent: SysModule
) : SysModule("CommandFifo", parent) {

    private var operations: Queue<OPERATION> = LinkedList<OPERATION>()

    val clk = bitInput("clk")
    val en = bitInput("en")

    val data = input<SysUnsigned>("data")
    val command = input<COMMAND>("command")
    val operation = output<OPERATION>("operation")

    init {
        stateFunction(clk) {
            infinite.state {
                if (data().width != capacityData) throw IllegalArgumentException()
                if (command().width != capacityCommand) throw IllegalArgumentException()
                if (en().one) when (command()) {
                    COMMAND.READ -> operation(operations.poll() ?: OPERATION.UNDEFINED)
                    COMMAND.WRITE -> operations.add(OPERATION[data().toInt()]
                            ?: throw IllegalArgumentException("Operation: ${data()} is not identified"))
                    COMMAND.RESET -> operations = LinkedList<OPERATION>()
                    else -> {
                    }
                }
            }
        }
    }
}

package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.integer.unsigned
import java.util.*

class OperationFifo(
        capacityData: Int,
        capacityCommand: Int,
        parent: SysModule
) : SysModule("OperationFifo", parent) {

    private var operations: Queue<SysUnsigned> = LinkedList<SysUnsigned>()

    val clk1 = bitInput("clk1")
    val clk2 = bitInput("clk2")
    val en = bitInput("en")
    val allow = bitInput("allow")

    val data = input<SysUnsigned>("data")
    val command = input<COMMAND>("command")
    val operation = output<SysUnsigned>("operation")
    val empty = bitOutput("empty")
    val dbin = bitOutput("dbin")

    val inc = bitOutput("inc")
    val read = bitOutput("read")

    init {
        stateFunction(clk1) {
            state.instance {
                read(ZERO)
                inc(ZERO)
            }
            infinite.block {
                case({ allow.one }) {
                    state {
                        read(ONE)
                        dbin(ONE)
                    }
                    sleep(1)
                    state {
                        read(ZERO)
                        dbin(ZERO)
                        inc(ONE)
                        operations.add(data())
                        empty(ZERO)
                    }
                    state.instance {
                        inc(ZERO)
                    }
                }
                otherwise { sleep(1) }
            }
        }

        stateFunction(clk2) {
            infinite.state {
                if (data().width != capacityData) throw IllegalArgumentException()
                if (command().width != capacityCommand) throw IllegalArgumentException()
                if (en.one) when (command()) {
                    COMMAND.READ -> operation(operations.poll() ?: unsigned(capacityData, OPERATION.UNDEFINED.id))
                    COMMAND.RESET -> operations = LinkedList<SysUnsigned>()
                    else -> { }
                }
                empty(SysBit(operations.isEmpty()))
            }
        }
    }
}

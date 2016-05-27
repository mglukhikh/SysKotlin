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

    private var store: Queue<SysUnsigned> = LinkedList<SysUnsigned>()

    val clk1 = bitInput("clk1")
    val clk2 = bitInput("clk2")
    val en = bitInput("en")
    val allow = bitInput("allow")
    val sleep = bitOutput("sleep")

    val data = input<SysUnsigned>("data")
    val args = output<SysUnsigned>("args")
    val command = input<COMMAND>("command")
    val operation = output<OPERATION>("operation")
    val empty = bitOutput("empty")
    val dbin = bitOutput("dbin")

    val inc = bitOutput("inc")
    val read = bitOutput("read")

    private fun SysUnsigned.toOperation() = OPERATION[this.toInt()] ?: OPERATION.UNDEFINED

    init {
        stateFunction(clk1) {
            init {
                read(ZERO)
                inc(ZERO)
                sleep(ZERO)
            }
            infinite.block {
                case({ allow.one }) {
                    state {
                        sleep(ZERO)
                        read(ONE)
                        dbin(ONE)
                    }
                    state {
                        read(ZERO)
                        dbin(ZERO)
                    }
                    state {
                        inc(ONE)
                        store.add(data())
                        empty(ZERO)
                    }
                    state.instant {
                        inc(ZERO)
                    }
                }
                otherwise {
                    state.instant { sleep(ONE) }
                    sleep(1)
                }
            }
        }

        stateFunction(clk2) {
            infinite.state {
                if (data().width != capacityData) throw IllegalArgumentException()
                if (command().width != capacityCommand) throw IllegalArgumentException()
                if (en.one) when (command()) {
                    COMMAND.READ -> operation(store.poll()?.toOperation() ?: OPERATION.UNDEFINED)
                    COMMAND.READ_DATA -> args(store.poll() ?: unsigned(capacityData, 0))
                    COMMAND.RESET -> store = LinkedList<SysUnsigned>()
                    else -> {
                    }
                }
                empty(SysBit(store.isEmpty()))
            }
        }
    }
}

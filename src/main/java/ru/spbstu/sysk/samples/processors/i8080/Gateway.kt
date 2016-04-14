package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned
import ru.spbstu.sysk.samples.processors.i8080.MainConstants.COMMAND

class Gateway constructor(
        capacity: Int,
        parent: SysModule
) : SysModule("Gateway", parent) {

    val front = bidirPort<SysUnsigned>("front")
    val back = bidirPort<SysUnsigned>("back")
    val command = bidirPort<SysUnsigned>("command")

    private var store = unsigned(capacity, 0)

    val en = bitInput("en")
    val clk = bitInput("clk")

    init {
        stateFunction(clk) {
            infiniteState {
                if (front().width != capacity) throw IllegalArgumentException()
                if (back().width != capacity) throw IllegalArgumentException()
                if (en().one) when (command()) {
                    COMMAND.READ_FRONT -> store = front()
                    COMMAND.READ_BACK -> store = back()
                    COMMAND.WRITE_FRONT -> front(store)
                    COMMAND.WRITE_BACK -> back(store)
                }
            }
        }
    }
}
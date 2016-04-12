package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned

class Gateway constructor(
        capacity: Int,
        parent: SysModule
) : SysModule("Gateway", parent) {

    private var store = unsigned(capacity, 0)

    val front = bidirPort<SysUnsigned>("front")
    val back = bidirPort<SysUnsigned>("back")
    val command = bidirPort<COMMAND>("command")
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
                    else -> {}
                }
            }
        }
    }
}
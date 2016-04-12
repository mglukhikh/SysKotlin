package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned

class TempMemory(
        capacityData: Int,
        parent: SysModule
) : SysModule("tempMemory", parent) {

    private var memory = unsigned(capacityData, 0)

    val inp = input<SysUnsigned>("inp")
    val out = output<SysUnsigned>("out")
    val en = bitInput("en")
    val clk = bitInput("clk")

    init {
        stateFunction(clk) {
            infiniteLoop {
                if (inp().width != capacityData) throw IllegalArgumentException()
                if (en().one) {
                    out(memory)
                    memory = inp()
                }
            }
        }
    }
}
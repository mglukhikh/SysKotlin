package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit

/**
 * Flip-flop D-trigger module
 */
class DFF (name: String, parent: SysModule): SysModule(name, parent) {

    val d = readOnlyBitPort("d")
    private val dval by d
    val clk = bitInput("clk")

    val q = readWritePort<SysBit>("q")
    private var qval by q

    init {
        function(clk, initialize = false) {
            // D-trigger does not require state and just provides one clock tick delay
            qval = dval
        }
    }
}


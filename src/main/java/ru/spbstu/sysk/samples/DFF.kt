package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit

/**
 * Flip-flop D-trigger module
 */
class DFF (name: String, parent: SysModule): SysModule(name, parent) {

    val d = bitInput("d")
    private val dval by bitPortReader(d)
    val clk = bitInput("clk")

    val q = output<SysBit>("q")
    private var qval by portWriter(q)

    init {
        function(clk, initialize = false) {
            // D-trigger does not require state and just provides one clock tick delay
            qval = dval
        }
    }
}


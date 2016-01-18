package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit

/**
 * Flip-flop D-trigger module
 */
class DFF (name: String, parent: SysModule): SysModule(name, parent) {

    val d = bitInput("d")
    val clk = bitInput("clk")

    val q = output<SysBit>("q")

    init {
        function(clk, initialize = false) {
            // D-trigger does not require state and just provides one clock tick delay
            q.value = d.value
        }
    }
}


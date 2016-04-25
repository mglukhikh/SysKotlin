package ru.spbstu.sysk.samples.triggers

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit.*

class RSFF(name: String, parent: SysModule): SysModule(name, parent) {

    val r   = bitInput("r")
    val s   = bitInput("s")
    val clk = bitInput("clk")

    private var state = X
    val q = bitOutput("q")

    init {
        function(clk, initialize = false) {

            if (s.one) state = ONE
            else if (r.one) state = ZERO
            q(state)
        }
    }
}

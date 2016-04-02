package ru.spbstu.sysk.samples.triggers

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*

class JKFF(name: String, parent: SysModule) : SysModule(name, parent) {

    val j = bitInput("j")
    val k = bitInput("k")
    val clk = bitInput("clk")

    private var state = ZERO
    val q = output<SysBit>("q")

    init {
        function(clk, initialize = false) {
            if (j.one && state.zero) state = ONE
            else if (k.one && state.one) state = ZERO
            q(state)
        }
    }
}

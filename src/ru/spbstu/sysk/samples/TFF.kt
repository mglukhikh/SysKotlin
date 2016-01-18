package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*

class TFF(name: String, parent: SysModule) : SysModule(name, parent) {

    val t = bitInput("t")
    val clk = bitInput("clk")

    private var state = ZERO
    val q = output<SysBit>("q")

    init {
        function(clk, initialize = false) {

            if (t.one) {
                if (state.one)
                    state = ZERO
                else
                    state = ONE
            }
            q.value = state
        }
    }
}


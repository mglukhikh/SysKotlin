package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit

public class PReg (name: String, digPerWord: Int, parent: SysModule): SysModule(name, parent) {

    val d = Array(digPerWord, {i -> bitInput("d" + i.toString())})
    val clk = bitInput("clk")

    private val state = Array(digPerWord, {i -> SysBit.X})
    val q = Array(digPerWord, {i -> output<SysBit>("q" + i.toString())})

    init {
        function(clk, initialize = false) {

            var i = 0
            while (i < digPerWord) {
                state[i] = d[i].value
                q[i].value = state[i]
                i += 1
            }
        }
    }
}

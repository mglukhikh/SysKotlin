package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit

public class SReg(name: String, digPerWord: Int, parent: SysModule) : SysModule(name, parent) {

    val d = bitInput("d")
    val clk = bitInput("clk")
    val dir = bitInput("dir")  //right: e = 0, left: e = 1
    val q = output<SysBit>("q")

    val trigQ = Array(digPerWord, { i -> SysBit.X })
    private val state = Array(digPerWord, { i -> SysBit.X })

    init {
        function(clk, initialize = false) {

            if (dir.zero) {
                state[0] = d.value
                trigQ[0] = state[0]

                var i = 1
                while (i < digPerWord) {
                    state[i] = trigQ[i - 1]
                    i += 1
                }

                i = 1
                while (i < digPerWord) {
                    trigQ[i] = state[i]
                    i += 1
                }

                q.value = trigQ[digPerWord - 1]
            }

            if (dir.one) {
                state[digPerWord - 1] = d.value
                trigQ[digPerWord - 1] = state[digPerWord - 1]

                var i = digPerWord - 2
                while (i >= 0) {
                    state[i] = trigQ[i + 1]
                    i -= 1
                }

                i = 0
                while (i < digPerWord - 1) {
                    trigQ[i] = state[i]
                    i += 1
                }

                q.value = trigQ[0]
            }
        }
    }
}


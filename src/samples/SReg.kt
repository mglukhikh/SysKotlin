package samples

import sysk.*

public class SReg (name: String, digPerWord: Int, parent: SysModule): SysModule(name, parent) {

    val d = wireInput("d")
    val clk = wireInput("clk")
    val dir = wireInput("dir")  //right: e = 0, left: e = 1
    val q = output<SysWireState>("q")

    private val trigQ = Array(digPerWord, {i -> output<SysWireState>("qIn" + i.toString())})
    private val state = Array(digPerWord, {i -> SysWireState.X})

    private val f: SysTriggeredFunction = triggeredFunction({

        println("$currentTime: d = ${d.value} q = ${q.value}")
        if (dir.zero) {
            state[0] = d.value
            trigQ[0].value = state[0]

            var i = 1
            while (i < digPerWord) {
                state[i] = trigQ[i - 1].value
                i += 1
            }

            i = 1
            while (i < digPerWord) {
                trigQ[i].value = state[i]
                i += 1
            }

            q.value = trigQ[digPerWord - 1].value
        }

        if (dir.one) {
            state[digPerWord - 1] = d.value
            trigQ[digPerWord - 1].value = state[digPerWord - 1]

            var i = digPerWord - 2
            while (i >= 0) {
                state[i] = trigQ[i + 1].value
                i -= 1
            }

            i = 0
            while (i < digPerWord - 1) {
                trigQ[i].value = state[i]
                i += 1
            }

            q.value = trigQ[0].value
        }

        f.wait()
    }, clk, initialize = false)
}


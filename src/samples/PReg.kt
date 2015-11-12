package samples

import sysk.*

public class PReg (name: String, digPerWord: Int, parent: SysModule): SysModule(name, parent) {

    val d = Array(digPerWord, {i -> wireInput("d" + i.toString())})
    val clk = wireInput("clk")

    private val state = Array(digPerWord, {i -> SysWireState.X})
    val q = Array(digPerWord, {i -> output<SysWireState>("q" + i.toString())})

    private val f: SysTriggeredFunction = triggeredFunction({

        var i = 0
        while (i < digPerWord) {
            state[i] = d[i].value
            q[i].value = state[i]
            i += 1
        }
        f.wait()
    }, clk, initialize = false)
}

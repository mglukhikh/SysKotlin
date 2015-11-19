package samples

import sysk.*

public class TFF (name: String, parent: SysModule): SysModule(name, parent) {

    val t = wireInput("t")
    val clk = wireInput("clk")

    private var state = SysWireState.ZERO
    val q = output<SysWireState>("q")

    init {
        triggeredFunction(clk, initialize = false) {

            if (t.one) {
                if (state.one)
                    state = SysWireState.ZERO
                else
                    state = SysWireState.ONE
            }
            q.value = state
        }
    }
}


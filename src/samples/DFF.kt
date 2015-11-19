package samples

import sysk.*

public class DFF (name: String, parent: SysModule): SysModule(name, parent) {

    val d = wireInput("d")
    val clk = wireInput("clk")

    private var state = SysWireState.X
    val q = output<SysWireState>("q")

    init {
        triggeredFunction(clk, initialize = false) {
            // TODO [glukhikh]: think why we have two clock ticks delay even with q.value = state after state change
            // TODO [glukhikh]: q.value = state should be ahead: here and in all other FFs
            if (d.one) state = SysWireState.ONE
            else if (d.zero) state = SysWireState.ZERO
            q.value = state
        }
    }
}


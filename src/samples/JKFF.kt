package samples

import sysk.*

class JKFF(name: String, parent: SysModule): SysModule(name, parent) {

    val j   = wireInput("j")
    val k   = wireInput("k")
    val clk = wireInput("clk")

    private var state = SysWireState.ZERO
    val q = output<SysWireState>("q")

    init {
        function(clk, initialize = false) {
            if (j.one && state.zero) state = SysWireState.ONE
            else if (k.one && state.one) state = SysWireState.ZERO
            q.value = state
        }
    }
}

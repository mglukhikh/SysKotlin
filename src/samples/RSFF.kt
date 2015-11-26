package samples

import sysk.*

class RSFF(name: String, parent: SysModule): SysModule(name, parent) {

    val r   = wireInput("r")
    val s   = wireInput("s")
    val clk = wireInput("clk")

    private var state = SysWireState.X
    val q = output<SysWireState>("q")

    init {
        function(clk, initialize = false) {

            if (s.one) state = SysWireState.ONE
            else if (r.one) state = SysWireState.ZERO
            q.value = state
        }
    }
}

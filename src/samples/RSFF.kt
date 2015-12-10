package samples

import sysk.*

class RSFF(name: String, parent: SysModule): SysModule(name, parent) {

    val r   = wireInput("r")
    val s   = wireInput("s")
    val clk = wireInput("clk")

    private var state = SysBit.X
    val q = output<SysBit>("q")

    init {
        function(clk, initialize = false) {

            if (s.one) state = SysBit.ONE
            else if (r.one) state = SysBit.ZERO
            q.value = state
        }
    }
}

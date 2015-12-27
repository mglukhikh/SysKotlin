package sysk.samples

import sysk.core.SysModule
import sysk.data.SysBit

class RSFF(name: String, parent: SysModule): SysModule(name, parent) {

    val r   = bitInput("r")
    val s   = bitInput("s")
    val clk = bitInput("clk")

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

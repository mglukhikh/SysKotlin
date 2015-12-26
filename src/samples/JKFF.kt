package samples

import sysk.*

class JKFF(name: String, parent: SysModule) : SysModule(name, parent) {

    val j = bitInput("j")
    val k = bitInput("k")
    val clk = bitInput("clk")

    private var state = SysBit.ZERO
    val q = output<SysBit>("q")

    init {
        function(clk, initialize = false) {
            if (j.one && state.zero) state = SysBit.ONE
            else if (k.one && state.one) state = SysBit.ZERO
            q.value = state
        }
    }
}

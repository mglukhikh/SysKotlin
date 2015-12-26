package samples

import sysk.*

public class TFF(name: String, parent: SysModule) : SysModule(name, parent) {

    val t = bitInput("t")
    val clk = bitInput("clk")

    private var state = SysBit.ZERO
    val q = output<SysBit>("q")

    init {
        function(clk, initialize = false) {

            if (t.one) {
                if (state.one)
                    state = SysBit.ZERO
                else
                    state = SysBit.ONE
            }
            q.value = state
        }
    }
}


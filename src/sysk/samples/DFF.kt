package sysk.samples

import sysk.core.SysModule
import sysk.data.SysBit

/**
 * Flip-flop D-trigger module
 */
public class DFF (name: String, parent: SysModule): SysModule(name, parent) {

    val d = bitInput("d")
    val clk = bitInput("clk")

    val q = output<SysBit>("q")

    init {
        function(clk, initialize = false) {
            // D-trigger does not require state and just provides one clock tick delay
            q.value = d.value
        }
    }
}


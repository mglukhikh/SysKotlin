package samples

import sysk.*

/**
 * Flip-flop D-trigger module
 */
public class DFF (name: String, parent: SysModule): SysModule(name, parent) {

    val d = wireInput("d")
    val clk = wireInput("clk")

    val q = output<SysWireState>("q")

    init {
        function(clk, initialize = false) {
            // D-trigger does not require state and just provides one clock tick delay
            q.value = d.value
        }
    }
}


package sysk

import sysk.*

/**
 * Register for storing any kind of information
 */
public class SysRegister <T : SysData> (name: String, defValue: T, parent: SysModule): SysModule(name, parent) {

    val d = input<T>("d")      // data input
    val en = bitInput("en")   // enable
    val clk = bitInput("clk") // clock

    private var state = defValue
    val q = output<T>("q")     // data output

    init {
        function(clk, initialize = false) {
            // TODO [mglukhikh]: tests
            if (en.one) {
                q.value = d.value
                state = d.value
            }
            else {
                q.value = state
            }
        }
    }
}

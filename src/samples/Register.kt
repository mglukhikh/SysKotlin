package samples

import sysk.*

/**
 * Register for storing any kind of information
 */
public class Register <T> (name: String, defValue: T, parent: SysModule): SysModule(name, parent) {

    val d = input<T>("d")      // data input
    val en = wireInput("en")   // enable
    val clk = wireInput("clk") // clock

    private var state: T = defValue
    val q = output<T>("q")     // data output

    init {
        function(clk, initialize = false) {
            // TODO [veronika]: tests
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

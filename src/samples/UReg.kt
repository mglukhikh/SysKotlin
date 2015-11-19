package samples

import sysk.*

public class UReg <T> (name: String, defValue: T, parent: SysModule): SysModule(name, parent) {

    val d = input<T>("d")
    val clk = wireInput("clk")

    private var state: T = defValue
    val q = output<T>("q")

    init {
        triggeredFunction(clk, initialize = false) {

            q.value = state
            state = d.value
        }
    }
}

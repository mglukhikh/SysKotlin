package samples

import sysk.*

public class UReg <T> (name: String, defValue: T, parent: SysModule): SysModule(name, parent) {

    val d = input<T>("d")
    val clk = wireInput("clk")

    private var state: T = defValue
    val q = output<T>("q")

    private val f: SysTriggeredFunction = triggeredFunction({

        q.value = state
        state = d.value

        f.wait()
    }, clk, initialize = false)
}

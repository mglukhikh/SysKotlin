package samples

import sysk.*

public class UReg <T> (name: String, parent: SysModule): SysModule(name, parent) {

    val d = input<T>("d")
    val clk = wireInput("clk")

    private var state: T? = null
    val q = output<T>("q")

    private val f: SysTriggeredFunction = triggeredFunction({

        state = d.value
        q.value = state!!

        f.wait()
    }, clk, initialize = false)
}

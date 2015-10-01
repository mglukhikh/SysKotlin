package modules

import sysk.SysModule
import sysk.SysTriggeredFunction

open class SysUnaryMoore<Input, State, Output>(
        transition: (State, Input) -> State,
        result: (State) -> Output,
        private var state: State,
        name: String,
        parent: SysModule
): SysModule(name, parent) {

    protected open fun createInput(name: String) = input<Input>("x")

    val x = createInput("x")

    val clk = wireInput("clk")

    val y = output<Output>("y")

    private val f: SysTriggeredFunction = triggeredFunction({
        y.value = result(state)
        state = transition(state, x.value)
        f.wait()
    }, clk, initialize = false)
}

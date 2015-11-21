package modules

import sysk.SysModule
import sysk.SysWait
import sysk.SysWireState

open class SysUnaryMoore<Input, State, Output>(
        transition: (State, Input) -> State,
        result: (State) -> Output,
        private var state: State,
        name: String,
        parent: SysModule
): SysModule(name, parent) {

    protected open fun createInput(name: String) = input<Input>(name)

    val x = createInput("x")

    val clk = wireInput("clk")

    val y = output<Output>("y")

    init {
        stagedFunction(clk.posEdgeEvent) {
            initStage {
                y.value = result(state)
            }
            infiniteStage {
                // First calculate state, then output, one tick delay is provided by clock sensitivity
                state = transition(state, x.value)
                y.value = result(state)
            }
        }
    }
}

open class SysUnaryWireMoore<State, Output>(
        transition: (State, SysWireState) -> State,
        result: (State) -> Output,
        state: State,
        name: String,
        parent: SysModule
): SysUnaryMoore<SysWireState, State, Output>(transition, result, state, name, parent) {

    override fun createInput(name: String) = wireInput(name)
}

class LatchTriggerMoore(name: String, parent: SysModule): SysUnaryWireMoore<SysWireState, SysWireState>(
        { prev: SysWireState, data: SysWireState -> data },
        { it },
        SysWireState.X,
        name,
        parent
)

class CountTriggerMoore(name: String, parent: SysModule): SysUnaryWireMoore<SysWireState, SysWireState>(
        { prev: SysWireState, data: SysWireState ->
            when (data) {
                SysWireState.ONE -> !prev
                else -> prev
            }
        },
        { it },
        SysWireState.ZERO,
        name,
        parent
)

open class SysBinaryMoore<Input1, Input2, State, Output>(
        transition: (State, Input1, Input2) -> State,
        result: (State) -> Output,
        private var state: State,
        name: String,
        parent: SysModule
): SysModule(name, parent) {

    protected open fun createInput1(name: String) = input<Input1>(name)

    protected open fun createInput2(name: String) = input<Input2>(name)

    val x1 = createInput1("x1")

    val x2 = createInput2("x2")

    val clk = wireInput("clk")

    val y = output<Output>("y")

    init {
        triggeredFunction(clk, initialize = false) {
            // First calculate state, then output, one tick delay is provided by clock sensitivity
            state = transition(state, x1.value, x2.value)
            y.value = result(state)
        }
    }
}

open class SysBinaryWireMoore<State, Output>(
        transition: (State, SysWireState, SysWireState) -> State,
        result: (State) -> Output,
        state: State,
        name: String,
        parent: SysModule
): SysBinaryMoore<SysWireState, SysWireState, State, Output>(transition, result, state, name, parent) {

    override fun createInput1(name: String) = wireInput(name)

    override fun createInput2(name: String) = wireInput(name)
}

class SetResetTriggerMoore(name: String, parent: SysModule): SysBinaryWireMoore<SysWireState, SysWireState>(
        { prev: SysWireState, set: SysWireState, reset: SysWireState ->
            if (set.zero && reset.zero) prev
            else if (reset.one && set.zero) SysWireState.ZERO
            else if (set.one && reset.zero) SysWireState.ONE
            else SysWireState.X
        },
        { it },
        SysWireState.X,
        name,
        parent
)

class JumpKeepTriggerMoore(name: String, parent: SysModule): SysBinaryWireMoore<SysWireState, SysWireState>(
        { prev: SysWireState, jump: SysWireState, keep: SysWireState ->
            if (jump.zero && keep.zero) prev
            else if (keep.one && jump.zero) SysWireState.ZERO
            else if (jump.one && keep.zero) SysWireState.ONE
            else if (jump.one && keep.one) !prev
            else SysWireState.X
        },
        { it },
        SysWireState.X,
        name,
        parent
)

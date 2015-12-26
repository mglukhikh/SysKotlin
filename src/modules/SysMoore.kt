package modules

import sysk.SysModule
import sysk.SysBit
import sysk.SysData

open class SysUnaryMoore<Input : SysData, State, Output : SysData>(
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
        stagedFunction(clk) {
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

open class SysUnaryWireMoore<State, Output : SysData>(
        transition: (State, SysBit) -> State,
        result: (State) -> Output,
        state: State,
        name: String,
        parent: SysModule
): SysUnaryMoore<SysBit, State, Output>(transition, result, state, name, parent) {

    override fun createInput(name: String) = wireInput(name)
}

class LatchTriggerMoore(name: String, parent: SysModule): SysUnaryWireMoore<SysBit, SysBit>(
        { prev: SysBit, data: SysBit -> data },
        { it },
        SysBit.X,
        name,
        parent
)

class CountTriggerMoore(name: String, parent: SysModule): SysUnaryWireMoore<SysBit, SysBit>(
        { prev: SysBit, data: SysBit ->
            when (data) {
                SysBit.ONE -> !prev
                else -> prev
            }
        },
        { it },
        SysBit.ZERO,
        name,
        parent
)

open class SysBinaryMoore<Input1 : SysData, Input2 : SysData, State, Output : SysData>(
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
        function(clk, initialize = false) {
            // First calculate state, then output, one tick delay is provided by clock sensitivity
            state = transition(state, x1.value, x2.value)
            y.value = result(state)
        }
    }
}

open class SysBinaryWireMoore<State, Output : SysData>(
        transition: (State, SysBit, SysBit) -> State,
        result: (State) -> Output,
        state: State,
        name: String,
        parent: SysModule
): SysBinaryMoore<SysBit, SysBit, State, Output>(transition, result, state, name, parent) {

    override fun createInput1(name: String) = wireInput(name)

    override fun createInput2(name: String) = wireInput(name)
}

class SetResetTriggerMoore(name: String, parent: SysModule): SysBinaryWireMoore<SysBit, SysBit>(
        { prev: SysBit, set: SysBit, reset: SysBit ->
            if (set.zero && reset.zero) prev
            else if (reset.one && set.zero) SysBit.ZERO
            else if (set.one && reset.zero) SysBit.ONE
            else SysBit.X
        },
        { it },
        SysBit.X,
        name,
        parent
)

class JumpKeepTriggerMoore(name: String, parent: SysModule): SysBinaryWireMoore<SysBit, SysBit>(
        { prev: SysBit, jump: SysBit, keep: SysBit ->
            if (jump.zero && keep.zero) prev
            else if (keep.one && jump.zero) SysBit.ZERO
            else if (jump.one && keep.zero) SysBit.ONE
            else if (jump.one && keep.one) !prev
            else SysBit.X
        },
        { it },
        SysBit.X,
        name,
        parent
)

package ru.spbstu.sysk.generics

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.SysData

open class SysUnaryMoore<Input : SysData, State, Output : SysData>(
        transition: (State, Input) -> State,
        result: (State) -> Output,
        private var state: State,
        name: String,
        parent: SysModule
): SysModule(name, parent) {

    protected open fun createInput(name: String) = input<Input>(name)

    val x = createInput("x")

    val clk = bitInput("clk")

    val y = output<Output>("y")

    init {
        stateFunction(clk) {
            Init {
                y(result(this@SysUnaryMoore.state))
            }
            InfiniteState {
                // First calculate state, then output, one tick delay is provided by clock sensitivity
                this@SysUnaryMoore.state = transition(this@SysUnaryMoore.state, x())
                y(result(this@SysUnaryMoore.state))
            }
        }
    }
}

open class SysUnaryBitMoore<State, Output : SysData>(
        transition: (State, SysBit) -> State,
        result: (State) -> Output,
        state: State,
        name: String,
        parent: SysModule
): SysUnaryMoore<SysBit, State, Output>(transition, result, state, name, parent) {

    override fun createInput(name: String) = bitInput(name)
}

class LatchTriggerMoore(name: String, parent: SysModule): SysUnaryBitMoore<SysBit, SysBit>(
        { prev: SysBit, data: SysBit -> data },
        { it },
        X,
        name,
        parent
)

class CountTriggerMoore(name: String, parent: SysModule): SysUnaryBitMoore<SysBit, SysBit>(
        { prev: SysBit, data: SysBit ->
            when (data) {
                ONE -> !prev
                else -> prev
            }
        },
        { it },
        ZERO,
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

    val clk = bitInput("clk")

    val y = output<Output>("y")

    init {
        function(clk, initialize = false) {
            // First calculate state, then output, one tick delay is provided by clock sensitivity
            state = transition(state, x1(), x2())
            y(result(state))
        }
    }
}

open class SysBinaryBitMoore<State, Output : SysData>(
        transition: (State, SysBit, SysBit) -> State,
        result: (State) -> Output,
        state: State,
        name: String,
        parent: SysModule
): SysBinaryMoore<SysBit, SysBit, State, Output>(transition, result, state, name, parent) {

    override fun createInput1(name: String) = bitInput(name)

    override fun createInput2(name: String) = bitInput(name)
}

class SetResetTriggerMoore(name: String, parent: SysModule): SysBinaryBitMoore<SysBit, SysBit>(
        { prev: SysBit, set: SysBit, reset: SysBit ->
            if (set.zero && reset.zero) prev
            else if (reset.one && set.zero) ZERO
            else if (set.one && reset.zero) ONE
            else X
        },
        { it },
        X,
        name,
        parent
)

class JumpKeepTriggerMoore(name: String, parent: SysModule): SysBinaryBitMoore<SysBit, SysBit>(
        { prev: SysBit, jump: SysBit, keep: SysBit ->
            if (jump.zero && keep.zero) prev
            else if (keep.one && jump.zero) ZERO
            else if (jump.one && keep.zero) ONE
            else if (jump.one && keep.one) !prev
            else X
        },
        { it },
        X,
        name,
        parent
)

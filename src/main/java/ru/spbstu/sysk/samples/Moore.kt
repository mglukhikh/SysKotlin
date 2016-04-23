package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.samples.Input.*
import ru.spbstu.sysk.samples.State.*
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysDataCompanion
import ru.spbstu.sysk.generics.SysUnaryMoore

enum class State {
    q0, q1, q2, q3
}

enum class Input : SysData {
    a, b, c, x;

    companion object : SysDataCompanion<Input> {
        override val undefined: Input
            get() = Input.x
    }
}

class Moore(parent: SysModule) : SysUnaryMoore<Input, State, SysBit>(
        transition = { state: State, inp: Input ->
            when (state) {
                q0 -> when (inp) {
                    b -> q2
                    c -> q1
                    else -> state
                }
                q1 -> when (inp) {
                    a -> q0
                    b -> q2
                    else -> state
                }
                q2 -> when (inp) {
                    a -> q1
                    else -> state
                }
                q3 -> q0
            }
        },
        result = { state: State ->
            when (state) {
                q0 -> ONE
                q1 -> ZERO
                q2 -> ONE
                q3 -> X
            }
        },
        startState = q3,
        name = "State Machine",
        parent = parent
)
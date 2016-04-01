package ru.spbstu.sysk.channels

import org.junit.Ignore
import org.junit.Test
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.data.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.core.TimeUnit.*

class MicroprocessorExample : SysTopModule() {

    val CPU: Microprocessor
    val clk: SysClock

    init {
        CPU = Microprocessor("M001", this)
        clk = clock("clock", 10(FS))
        CPU.clk bind clk
    }

    @Ignore
    @Test
    fun show() = start(1(NS))
}

class ArithmeticLogicUnit constructor(
        name: String, parent: SysModule
) : SysModule(name, parent) {

}

class ControlUnit constructor(
        name: String, parent: SysModule
) : SysModule(name, parent) {

}

class Register constructor(
        val capacity: Int, name: String, parent: SysModule
) : SysModule(name, parent) {

    val D: SysBusPort<SysBit>
    val Q: SysBusPort<SysBit>
    val V: SysBusPort<SysBit>

    val clk: SysBitInput

    init {
        D = busPort(capacity, "input data")
        Q = busPort(capacity, "output data")
        V = busPort(2, "control")
        clk = bitInput("clock")

        stateFunction(clk) {
            infiniteState {
                if (V[0] == ONE && V[1] == ONE) {
                    for (i in 0..capacity - 1) Q(D[i], i)
                } else if (V[0] == ONE && V[1] == ZERO) {
                    for (i in 0..capacity - 2) Q(Q[i + 1], i)
                    Q(D[capacity - 1], capacity - 1)
                } else if (V[0] == ZERO && V[1] == ONE) {
                    for (i in 1..capacity - 1) Q(Q[i], i - 1)
                    Q(D[0], 0)
                }
            }
        }
    }
}

class Microprocessor constructor(
        name: String, parent: SysModule
) : SysModule(name, parent) {

    val ALU: ArithmeticLogicUnit
    val CU: ControlUnit
    val AX: Register
    val BX: Register
    val CX: Register
    val DX: Register

    val clk: SysBitInput

    val CAPACITY_DATA = 64

    init {
        ALU = ArithmeticLogicUnit("A001", this)
        CU = ControlUnit("C001", this)
        AX = Register(CAPACITY_DATA, "R001", this)
        BX = Register(CAPACITY_DATA, "R002", this)
        CX = Register(CAPACITY_DATA, "R003", this)
        DX = Register(CAPACITY_DATA, "R004", this)

        clk = bitInput("clock")
    }
}
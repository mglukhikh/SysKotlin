package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.unsigned

class ALUTest : SysTopModule() {

    val ALU = ArithmeticLogicUnit(CAPACITY.DATA, CAPACITY.COMMAND, this)

    val operation = signal("command", COMMAND.ADD)
    val a = signal("a", unsigned(CAPACITY.DATA, 0))
    val b = signal("b", unsigned(CAPACITY.DATA, 0))
    val out = signal("out", unsigned(CAPACITY.DATA, 0))
    val clk = clock("clock", 20(FS))

    init {
        ALU.operation bind operation
        ALU.A bind a
        ALU.B bind b
        ALU.out bind out
        ALU.flag bind signal("flag", unsigned(CAPACITY.DATA, 0))
        ALU.outside bind bitSignalStub("outside", SysBit.ZERO)
        ALU.data bind signalStub("data", unsigned(CAPACITY.DATA, 0))
        ALU.en bind bitSignalStub("en", SysBit.ONE)

        stateFunction(clk, false) {
            state {
                a(unsigned(CAPACITY.DATA, 30))
                b(unsigned(CAPACITY.DATA, 100))
            }
            state {
                assert(out() == unsigned(CAPACITY.DATA, 130))
                a(unsigned(CAPACITY.DATA, 50))
                b(unsigned(CAPACITY.DATA, 70))
            }
            state {
                assert(out() == unsigned(CAPACITY.DATA, 120))
                a(unsigned(CAPACITY.DATA, 5))
                b(unsigned(CAPACITY.DATA, 7))
                operation(COMMAND.MUL)
            }
            state {
                assert(out() == unsigned(CAPACITY.DATA, 35))
                a(unsigned(CAPACITY.DATA, 13))
                b(unsigned(CAPACITY.DATA, 3))
            }
            state {
                assert(out() == unsigned(CAPACITY.DATA, 39))
                a(unsigned(CAPACITY.DATA, 13))
                b(unsigned(CAPACITY.DATA, 3))
                operation(COMMAND.SHL)
            }
            state {
                assert(out() == unsigned(CAPACITY.DATA, 104))
            }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start(1(S))
}
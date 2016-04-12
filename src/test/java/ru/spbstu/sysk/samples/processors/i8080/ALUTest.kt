package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.integer

class ALUTest : SysTopModule() {

    val ALU = ArithmeticLogicUnit(CAPACITY.DATA, CAPACITY.COMMAND, this)

    val operation = signal("command", COMMAND.ADD)
    val a = signal("a", integer(CAPACITY.DATA, 0))
    val b = signal("b", integer(CAPACITY.DATA, 0))
    val c = signal("c", integer(CAPACITY.DATA, 0))
    val clk = clock("clock", 20(FS))

    init {
        ALU.operation bind operation
        ALU.A bind a
        ALU.B bind b
        ALU.C bind c


        stateFunction(clk) {
            state {
                a(integer(CAPACITY.DATA, 30))
                b(integer(CAPACITY.DATA, -128))
            }
            state {
                println(c())
                a(integer(CAPACITY.DATA, 50))
                b(integer(CAPACITY.DATA, -70))
            }
            state {
                println(c())
                a(integer(CAPACITY.DATA, 5))
                b(integer(CAPACITY.DATA, 7))
                operation(COMMAND.MUL)
            }
            state {
                println(c())
                a(integer(CAPACITY.DATA, 13))
                b(integer(CAPACITY.DATA, -3))
            }
            state {
                println(c())
                a(integer(CAPACITY.DATA, 13))
                b(integer(CAPACITY.DATA, 3))
                operation(COMMAND.SHL)
            }
            state {
                println(c())
            }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start(1(S))
}
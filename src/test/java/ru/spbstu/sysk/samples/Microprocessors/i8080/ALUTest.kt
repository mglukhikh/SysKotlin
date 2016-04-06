package ru.spbstu.sysk.samples.microprocessors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.integer
import ru.spbstu.sysk.samples.microprocessors.i8080.MainConstants.OPERATION

class ALUTest : SysTopModule() {

    val CAPACITY = 8

    val ALU = ArithmeticLogicUnit(CAPACITY, "ALU", this)

    val operation = signal("command", OPERATION.ADD)
    val a = signal("a", integer(CAPACITY, 0))
    val b = signal("b", integer(CAPACITY, 0))
    val c = signal("c", integer(CAPACITY, 0))
    val clk = clock("clock", 20(FS))

    init {
        ALU.operation bind operation
        ALU.A bind a
        ALU.B bind b
        ALU.C bind c


        stateFunction(clk) {
            state {
                a(integer(CAPACITY, 30))
                b(integer(CAPACITY, -128))
            }
            state {
                println(c())
                a(integer(CAPACITY, 50))
                b(integer(CAPACITY, -70))
            }
            state {
                println(c())
                a(integer(CAPACITY, 5))
                b(integer(CAPACITY, 7))
                operation(OPERATION.MUL)
            }
            state {
                println(c())
                a(integer(CAPACITY, 13))
                b(integer(CAPACITY, -3))
            }
            state {
                println(c())
                a(integer(CAPACITY, 13))
                b(integer(CAPACITY, 3))
                operation(OPERATION.SHL)
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
package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.integer
import ru.spbstu.sysk.data.integer.unsigned

class RegisterFileTest : SysTopModule() {

    val RF = RegisterFile(CAPACITY.DATA, CAPACITY.ADDRESS, CAPACITY.REGISTER, CAPACITY.COMMAND, this)

    val data = signal("data", unsigned(CAPACITY.DATA, 0))
    val command = signal("command", COMMAND.UNDEFINED)
    val register = signal("register", REGISTER.UNDEFINED)
    val address = signal("address", unsigned(CAPACITY.ADDRESS, 0))
    val en = signal<SysBit>("en")
    val clk = clock("clk", 2(FS))

    init {
        RF.register bind register
        RF.address bind address
        RF.clk bind clk
        RF.command bind command
        RF.data bind data
        RF.en bind en

        stateFunction(clk, false) {
            state {
                data(unsigned(CAPACITY.DATA, 255))
                command(COMMAND.WRITE)
                register(REGISTER.B)
                en(ONE)
            }
            state {
                data(unsigned(CAPACITY.DATA, 10))
                command(COMMAND.WRITE)
                register(REGISTER.E)
            }
            state {
                en(ZERO)
            }
            state {
                command(COMMAND.READ)
                register(REGISTER.B)
                en(ONE)
            }
            state {
                assert(data() == unsigned(CAPACITY.DATA, 255))
                command(COMMAND.READ)
                register(REGISTER.E)
            }
            state {
                assert(data() == unsigned(CAPACITY.DATA, 10))
                en(ZERO)
            }
            stop(scheduler)
        }
    }

    @Test
    fun set() {
        println("SysUnsigned")
        println(java.lang.Long.toBinaryString(unsigned(5, 21).toLong()))
        println(java.lang.Long.toBinaryString(unsigned(9, 268).toLong()))
        println(java.lang.Long.toBinaryString(unsigned(9, 268).sets(5, 1, unsigned(5, 21).bits()).toLong()))

        println("SysLongInteger")
        println(java.lang.Long.toBinaryString(integer(5, 21).toLong()))
        println(java.lang.Long.toBinaryString(integer(9, 268).toLong()))
        println(java.lang.Long.toBinaryString(integer (9, 268).sets(5, 1, unsigned(5, 21).bits()).toLong()))
    }

    @Test
    fun show() = start(1(S))
}
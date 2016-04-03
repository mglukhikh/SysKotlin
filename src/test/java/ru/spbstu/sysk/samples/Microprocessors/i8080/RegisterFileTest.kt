package ru.spbstu.sysk.samples.microprocessors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.integer
import ru.spbstu.sysk.data.integer.unsigned

class RegisterFileTest : SysTopModule() {

    val RegFile = RegisterFile(8, this)

    val data = signal<SysUnsigned>("data")
    val command = signal("command", COMMAND.STORAGE)
    val register = signal<SysUnsigned>("register")
    val address = signal<SysUnsigned>("address")
    val clk = clock("clk", 2(FS))

    init {
        RegFile.register bind register
        RegFile.address bind address
        RegFile.clk bind clk
        RegFile.command bind command
        RegFile.data bind data

        stateFunction(clk, false) {
            state {
                data(unsigned(8, 255))
                command(COMMAND.WRITE)
                register(REGISTER.B)
            }
            state {
                data(unsigned(8, 10))
                command(COMMAND.WRITE)
                register(REGISTER.E)
            }
            state {
                command(COMMAND.STORAGE)
            }
            state {
                command(COMMAND.READ)
                register(REGISTER.B)
            }
            state {
                assert(data() == unsigned(8, 255))
                command(COMMAND.READ)
                register(REGISTER.E)
            }
            state {
                assert(data() == unsigned(8, 10))
                command(COMMAND.STORAGE)
            }
            state {
                scheduler.stop()
            }
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
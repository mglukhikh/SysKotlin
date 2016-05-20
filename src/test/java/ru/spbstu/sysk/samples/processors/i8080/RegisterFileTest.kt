package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.integer
import ru.spbstu.sysk.data.integer.unsigned

class RegisterFileTest : SysTopModule() {

    val RF = RegisterFile(CAPACITY.DATA, CAPACITY.ADDRESS, CAPACITY.REGISTER, CAPACITY.COMMAND, this)

    val data = signal("data", unsigned(CAPACITY.DATA, 0))
    val inp = signal("inp", unsigned(CAPACITY.DATA, 0))
    val out = signal("out", unsigned(CAPACITY.DATA, 0))
    val b = signal("b", unsigned(CAPACITY.DATA, 0))
    val command = signal("command", COMMAND.UNDEFINED)
    val register = signal("register", REGISTER.UNDEFINED)
    val address = signal("address", unsigned(CAPACITY.ADDRESS, 0))
    val en = bitSignal("en")
    val clk = clock("clk", 2(FS))

    init {
        RF.register bind register
        RF.address bind address
        RF.clk bind clk
        RF.command bind command
        RF.data bind data
        RF.inp bind inp
        RF.out bind out
        RF.B bind b
        RF.en bind en
        RF.inc bind bitSignalStub("inc", ZERO)
        RF.read bind bitSignalStub("read", ZERO)
        RF.flag bind signalStub("flag", unsigned(CAPACITY.DATA, 0))
        RF.pc bind address

        stateFunction(clk, false) {
            state {
                data(unsigned(CAPACITY.DATA, 255))
                command(COMMAND.WRITE_DATA)
                register(REGISTER.B)
                en(ONE)
            }
            state {
                data(unsigned(CAPACITY.DATA, 10))
                command(COMMAND.WRITE_DATA)
                register(REGISTER.E)
            }
            state {
                command(COMMAND.SET_CURRENT)
                register(REGISTER.A)
            }
            state {
                command(COMMAND.SET_A)
                register(REGISTER.B)
            }
            state {
                command(COMMAND.SET_B)
                register(REGISTER.E)
            }
            state {
                inp(out() + b())
            }
            state {
                command(COMMAND.READ_DATA)
                register(REGISTER.A)
            }
            state {
                assert(data() == unsigned(CAPACITY.DATA, 9))
                en(ZERO)
            }
            state {
                command(COMMAND.READ_DATA)
                register(REGISTER.B)
                en(ONE)
            }
            state {
                assert(data() == unsigned(CAPACITY.DATA, 255))
                command(COMMAND.READ_DATA)
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
        val first = unsigned(5, 21)
        val second = unsigned(9, 268)
        assert(second.set(5, 1, first) == unsigned(9, 298))

        val secondInt = integer(10, 268)
        assert(secondInt.set(5, 1, first) == integer(10, 298))
    }

    @Test
    fun show() = start(1(S))
}
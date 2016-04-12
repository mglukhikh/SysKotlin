package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.unsigned

class CommandRegisterTest : SysTopModule() {

    val CR = CommandRegister(CAPACITY.DATA, VALUE.MAX_ADDRESS, CAPACITY.ADDRESS, CAPACITY.COMMAND, this)

    val data = signal("data", unsigned(CAPACITY.DATA, 0))
    val command = signal("command", COMMAND.UNDEFINED)
    val address = signal("address", unsigned(CAPACITY.ADDRESS, 0))
    val en = signal<SysBit>("en")
    val clk = clock("clk", 2(FS))

    init {
        CR.address bind address
        CR.clk bind clk
        CR.command bind command
        CR.data bind data
        CR.en bind en

        val i = iterator(0..11)
        val j = iterator(0..7)
        stateFunction(clk, false) {
            loop(i) {
                state {
                    data(unsigned(CAPACITY.DATA, i.it))
                    command(COMMAND.WRITE)
                    address(unsigned(CAPACITY.ADDRESS, i.it))
                    en(ONE)
                }
            }
            state {
                en(ZERO)
            }
            state {
                command(COMMAND.READ)
                address(unsigned(CAPACITY.ADDRESS, 1))
                en(ONE)
            }
            loop(j) {
                state {
                    assert(data() == unsigned(CAPACITY.DATA, j.it + 1))
                    command(COMMAND.READ)
                    address(unsigned(CAPACITY.ADDRESS, j.it + 2))
                    if (j.it == 7) en(ZERO) else en(ONE)
                }
            }
            state {
                assert(data() == unsigned(CAPACITY.DATA, j.it + 1))
                en(ZERO)
            }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start(1(S))
}
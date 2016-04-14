package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.integer.unsigned

class OperationFifoTest : SysTopModule() {

    val CF = OperationFifo(CAPACITY.DATA, CAPACITY.COMMAND, this)

    val data = signal("data", unsigned(CAPACITY.DATA, 0))
    val command = signal("command", COMMAND.UNDEFINED)
    val operation = signal("operation", OPERATION.UNDEFINED)
    val en = bitSignal("en")
    val clk = clock("clk", 2(FS))

    init {
        CF.clk bind clk
        CF.command bind command
        CF.data bind data
        CF.en bind en
        CF.operation bind operation

        stateFunction(clk, false) {
            state {
                command(COMMAND.WRITE)
                data(unsigned(CAPACITY.DATA, OPERATION.ADD_H.id))
                en(ONE)
            }
            state {
                data(unsigned(CAPACITY.DATA, OPERATION.MOV_A_A.id))
            }
            state {
                data(unsigned(CAPACITY.DATA, OPERATION.MOV_B_A.id))
            }
            state {
                command(COMMAND.READ)
            }
            state {
                assert(operation() == OPERATION.ADD_H)
                en(ZERO)
            }
            state {
                assert(operation() == OPERATION.ADD_H)
                command(COMMAND.READ)
                en(ONE)
            }
            state{
                assert(operation() == OPERATION.MOV_A_A)
            }
            state{
                assert(operation() == OPERATION.MOV_B_A)
            }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start()
}

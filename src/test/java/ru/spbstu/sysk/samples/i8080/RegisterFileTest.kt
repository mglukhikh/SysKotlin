package ru.spbstu.sysk.samples.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.FS
import ru.spbstu.sysk.core.TimeUnit.S
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.SysInteger

class RegisterFileTest : SysTopModule() {

    val RegFile = RegisterFile(this)

    val data = bitBus(8, "data")
    val command = signal("command", STORAGE)
    val address = signal<SysInteger>("address")
    val clk = clock("clk", 20(FS))
    val dataPort = bitBusPort(8, "data")

    init {
        RegFile.address bind address
        RegFile.clk bind clk
        RegFile.command bind command
        RegFile.data bind data
        dataPort bind data

        stateFunction(clk, false) {
            state {
                dataPort(SysInteger(8, 127))
                command(PUSH)
                address(B)
            }
            state {
                dataPort(SysInteger(8, -127))
                command(PUSH)
                address(E)
            }
            state {
                dataPort.disable()
                command(STORAGE)
            }
            state {
                command(PULL)
                address(B)
            }
            state {
                assert(dataPort() == SysInteger(8, 127))
                command(PULL)
                address(E)
            }
            state {
                assert(dataPort() == SysInteger(8, -127))
                command(STORAGE)
            }
            state {
                scheduler.stop()
            }
        }
    }

    @Test
    fun show() = start(1(S))
}
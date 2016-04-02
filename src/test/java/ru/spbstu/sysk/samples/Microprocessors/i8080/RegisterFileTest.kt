package ru.spbstu.sysk.samples.microprocessors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.FS
import ru.spbstu.sysk.core.TimeUnit.S
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned

class RegisterFileTest : SysTopModule() {

    val RegFile = RegisterFile(this)

    val data = signal<SysUnsigned>("data")
    val command = signal("command", STORAGE)
    val address = signal<SysUnsigned>("address")
    val clk = clock("clk", 20(FS))
    val dataPort = bidirPort<SysUnsigned>("data")

    init {
        RegFile.address bind address
        RegFile.clk bind clk
        RegFile.command bind command
        RegFile.data bind data
        dataPort bind data

        stateFunction(clk, false) {
            state {
                dataPort(unsigned(8, 255))
                command(WRITE)
                address(B)
            }
            state {
                dataPort(unsigned(8, 10))
                command(WRITE)
                address(E)
            }
            state {
                println(dataPort())
                command(STORAGE)
            }
            state {
                println(dataPort())
                command(READ)
                address(B)
            }
            state {
                println(dataPort())
//                assert(dataPort() == unsigned(8, 255))
                command(READ)
                address(E)
            }
            state {
                println(dataPort())
//                assert(dataPort() == unsigned(8, 10))
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


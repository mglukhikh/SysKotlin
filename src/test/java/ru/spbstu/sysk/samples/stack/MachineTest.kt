package ru.spbstu.sysk.samples.stack

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.integer

class MachineTest {

    private class MachineTester : SysTopModule("tester") {

        private val addrWidth = 8
        private val dataWidth = 8

        val m = Machine("machine", addrWidth, dataWidth, this)

        val clk = clock("clk", 20(TimeUnit.NS))

        var mInput by signalWriter("mInput", m.din)
        val mOutput by signalReader("mOutput", m.dout)
        var mOpcode by signalWriter("mOpcode", m.opcode)

        init {
            bind(m.clk to clk)

            stateFunction(clk) {
                init {
                    mInput = integer(dataWidth, 0)
                    mOpcode = Opcode.NOP
                }
                state {
                    mInput = integer(dataWidth, 42)
                    mOpcode = Opcode.PUSH
                }
                state {
                    mOpcode = Opcode.POP
                }
                state {
                    assertEquals(integer(dataWidth, 42), mOutput)
                }
            }
        }
    }

    @Test
    fun tester() {
        MachineTester().start()
    }
}
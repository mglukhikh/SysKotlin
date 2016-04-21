package ru.spbstu.sysk.samples.stack

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.integer
import ru.spbstu.sysk.samples.stack.Opcode.*

class MachineTest {

    private abstract class AbstractMachineTester(
            protected val addrWidth: Int = 8,
            protected val dataWidth: Int = 8
    ) : SysTopModule("tester") {

        val m = Machine("machine", addrWidth, dataWidth, this)

        val clk = clock("clk", 20(TimeUnit.NS))

        var mInput by signalWriter("mInput", m.din)
        val mOutput by signalReader("mOutput", m.dout)
        var mOpcode by signalWriter("mOpcode", m.opcode)

        init {
            bind(m.clk to clk)
        }

        protected fun resetInput() {
            mInput = integer(dataWidth, 0)
            mOpcode = NOP
        }
    }

    private class MachineTester1 : AbstractMachineTester() {
        init {
            stateFunction(clk) {
                init {
                    resetInput()
                }
                state {
                    mInput = integer(dataWidth, 42)
                    mOpcode = PUSH
                }
                state {
                    mOpcode = POP
                }
                state {
                    assertEquals(integer(dataWidth, 42), mOutput)
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun tester1() {
        MachineTester1().start()
    }

    private class MachineTester2 : AbstractMachineTester() {
        init {
            stateFunction(clk) {
                init {
                    resetInput()
                }
                state {
                    mInput = integer(dataWidth, 2)
                    mOpcode = PUSH
                }
                state {
                    mInput = integer(dataWidth, 3)
                }
                state {
                    mOpcode = PLUS
                }
                state {
                    mOpcode = POP
                }
                state {
                    assertEquals(integer(dataWidth, 5), mOutput)
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun tester2() {
        MachineTester2().start()
    }
}
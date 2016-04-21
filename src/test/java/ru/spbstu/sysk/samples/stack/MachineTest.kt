package ru.spbstu.sysk.samples.stack

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.samples.stack.Opcode.*

class MachineTest {

    private abstract class AbstractMachineTester(
            protected val addrWidth: Int = 8,
            protected val dataWidth: Int = 8
    ) : SysTopModule("tester") {

        val m = Machine("machine", addrWidth, dataWidth, this)

        val clk = clock("clk", 20(TimeUnit.NS))

        var mInput by numberSignalWriter("mInput", dataWidth, m.din)
        val mOutput by numberSignalReader("mOutput", m.dout)
        var mOpcode by signalWriter("mOpcode", m.opcode)

        init {
            bind(m.clk to clk)
        }

        protected fun resetInput() {
            mInput = 0
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
                    mInput = 42
                    mOpcode = PUSH
                }
                state {
                    mOpcode = POP
                }
                state {
                    assertEquals(42L, mOutput)
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
                    mInput = 2
                    mOpcode = PUSH
                }
                state {
                    mInput = 3
                }
                state {
                    mOpcode = PLUS
                }
                state {
                    mOpcode = POP
                }
                state {
                    assertEquals(5L, mOutput)
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
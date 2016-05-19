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
            addrWidth: Int = 8,
            dataWidth: Int = 8
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

    private class MachineTester3 : AbstractMachineTester() {
        init {
            stateFunction(clk) {
                init {
                    resetInput()
                }
                state {
                    mInput = 10
                    mOpcode = PUSH
                }
                state {
                    mInput = 4
                }
                state {
                    mInput = 3
                }
                state(2) {
                    mOpcode = PLUS
                }
                state(2) {
                    mOpcode = TIMES
                }
                state {
                    mOpcode = POP
                }
                state {
                    assertEquals(70L, mOutput)
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun tester3() {
        MachineTester3().start()
    }

    private class MachineTester4 : AbstractMachineTester() {
        init {
            stateFunction(clk) {
                init {
                    resetInput()
                }
                state {
                    mInput = 12
                    mOpcode = PUSH
                }
                state {
                    mInput = 34
                }
                state {
                    mInput = 56
                }
                state {
                    mInput = 78
                }
                state {
                    mInput = 110
                }
                state {
                    mInput = 127
                }
                state {
                    mOpcode = POP
                }
                state(2) {
                    assertEquals(127L, mOutput)
                }
                state(2) {
                    assertEquals(110L, mOutput)
                }
                state(2) {
                    assertEquals(78L, mOutput)
                }
                state(2) {
                    assertEquals(56L, mOutput)
                }
                state(2) {
                    assertEquals(34L, mOutput)
                }
                state {
                    assertEquals(12L, mOutput)
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun tester4() {
        MachineTester4().start()
    }
}
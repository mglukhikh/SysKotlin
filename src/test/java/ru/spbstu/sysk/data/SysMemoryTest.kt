package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit
import ru.spbstu.sysk.core.time

class SysMemoryTest {

    private class SequenceTester : SysTopModule("tester") {
        val m = integerMemory("memory", 8, 8)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        var en by readWriteSignal("men", m.en)
        var wr by readWriteSignal("mwr", m.wr)
        var d by readWriteSignal("md", m.din)
        val q by readOnlySignal("mq", m.dout)
        var addr by readWriteSignal("maddr", m.addr)

        init {
            bind(m.clk to clk)

            stateFunction(clk) {
                init {
                    en = SysBit.ZERO
                    wr = SysBit.ZERO
                    addr = SysUnsigned.valueOf(8, 0)
                }
                val i = iterator(0..255)
                loop(i) {
                    state {
                        en = SysBit.ONE
                        wr = SysBit.ONE
                        addr = SysUnsigned.valueOf(8, i.it)
                        d = SysInteger(8, i.it - 128)
                    }
                }
                val j = iterator(0..255)
                loop(j) {
                    state {
                        en = SysBit.ONE
                        wr = SysBit.ZERO
                        addr = SysUnsigned.valueOf(8, j.it)
                    }
                    state {}
                    state {
                        val expected = SysInteger(8, j.it - 128)
                        assert(q == expected) {
                            "#${j.it}: Expected $expected, Actual $q"
                        }
                    }
                }
                state {
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun sequenceTester() {
        SequenceTester().start()
    }

    private class LoadTester : SysTopModule("tester") {
        val m = memory<SysBit>("memory", 8, SysBit.X)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        var en by readWriteSignal("men", m.en)
        var wr by readWriteSignal("mwr", m.wr)
        var d by readWriteSignal("md", m.din)
        val q by readOnlyBitSignal("mq", m.dout)
        var addr by readWriteSignal("maddr", m.addr)

        init {
            bind(m.clk to clk)

            m.load {
                if (it.toInt() % 2 == 0) SysBit.ZERO else SysBit.ONE
            }
            assert(m.check {
                if (it.toInt() % 2 == 0) SysBit.ZERO else SysBit.ONE
            })

            stateFunction(clk) {
                init {
                    en = SysBit.ZERO
                    wr = SysBit.ZERO
                    addr = SysUnsigned.valueOf(8, 0)
                }
                val j = iterator(0..255)
                loop(j) {
                    state {
                        en = SysBit.ONE
                        wr = SysBit.ZERO
                        addr = SysUnsigned.valueOf(8, j.it)
                    }
                    state {}
                    state {
                        val expected = if (j.it % 2 == 0) SysBit.ZERO else SysBit.ONE
                        assert(q == expected) {
                            "#${j.it}: Expected $expected, Actual $q"
                        }
                    }
                }
                state {
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun loadTester() {
        LoadTester().start()
    }
}

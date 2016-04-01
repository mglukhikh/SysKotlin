package ru.spbstu.sysk.channels

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.NS
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.SysUnsigned

class SysMemoryTest {

    private class SequenceTester : SysTopModule("tester") {
        val m = integerMemory("memory", 8, 8)

        val clk = clock("clk", 20(NS))

        var en by signalWriter("men", m.en)
        var wr by signalWriter("mwr", m.wr)
        var d by signalWriter("md", m.din)
        val q by signalReader("mq", m.dout)
        var addr by signalWriter("maddr", m.addr)

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

        val clk = clock("clk", 20(NS))

        var en by signalWriter("men", m.en)
        var wr by signalWriter("mwr", m.wr)
        val q by bitSignalReader("mq", m.dout)
        var addr by signalWriter("maddr", m.addr)

        init {
            connector("md", m.din)
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

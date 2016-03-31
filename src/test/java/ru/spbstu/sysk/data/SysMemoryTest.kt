package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit
import ru.spbstu.sysk.core.time

class SysMemoryTest {

    private class Tester : SysTopModule("tester") {
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
                    addr = SysInteger(8, 0)
                }
                val i = iterator(0..127)
                loop(i) {
                    state {
                        en = SysBit.ONE
                        wr = SysBit.ONE
                        addr = SysInteger(8, i.it)
                        d = SysInteger(8, i.it)
                    }
                }
                val j = iterator(0..127)
                loop(j) {
                    state {
                        en = SysBit.ONE
                        wr = SysBit.ZERO
                        addr = SysInteger(8, j.it)
                    }
                    state {}
                    state {
                        assert(q == SysInteger(8, j.it)) {
                            "#${j.it}: Expected ${j.it}, Actual $q"
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
    fun tester() {
        Tester().start()
    }
}
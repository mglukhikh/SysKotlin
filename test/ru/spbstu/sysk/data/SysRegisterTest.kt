package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit
import ru.spbstu.sysk.core.time
import ru.spbstu.sysk.data.SysBit.*

class SysRegisterTest {

    private class Counter : SysTopModule("counter") {
        val a = register("a", SysInteger(8, 0))

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        var en by readWriteSignal("aen", a.en)
        var d by readWriteSignal("ad", a.d)
        val q by readOnlySignal("aq", a.q)

        init {
            bind(a.clk to clk)

            stateFunction(clk) {
                init {
                    en = ZERO
                }
                forEach(0..9) {
                    state {
                        en = ONE
                        d = q + SysInteger.valueOf(1)
                        assert(q == SysInteger(8, it / 2)) {
                            "#$it: Expected ${it/2}, Actual ${q.value}"
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
    fun counter() {
        Counter().start()
    }

    private class FibonacciProcessor : SysTopModule("fibonacci") {
        val a = register("a", SysInteger(8, 1))

        val b = register("b", SysInteger(8, 1))

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        var aen by readWriteSignal("aen", a.en)
        var ben by readWriteSignal("ben", b.en)

        var ad by readWriteSignal("ad", a.d)
        var bd by readWriteSignal("bd", b.d)

        val aq by readOnlySignal("aq", a.q)
        val bq by readOnlySignal("bq", b.q)

        init {
            bind(a.clk to clk, b.clk to clk)

            val values = arrayOf(1, 2, 3, 5, 8, 13, 21, 34, 55)

            stateFunction(clk) {
                init {
                    aen = ZERO
                    ben = ZERO
                }
                forEach(0..17) {
                    state {
                        aen = ONE
                        ben = ONE
                        ad = bq
                        bd = aq + bq
                        assert(bq == SysInteger(8, values[it / 2])) {
                            "#$it: Expected ${values[it/2]}, Actual $bq"
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
    fun fibonacci() {
        FibonacciProcessor().start()
    }
}
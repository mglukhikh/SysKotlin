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
        var d by readWriteSignal("ad", a.d.inp)
        val q by readOnlySignal("aq", a.q.out)

        init {
            bind(a.clk to clk)

            stateFunction(clk) {
                init {
                    en = ZERO
                }
                val i = iterator(0..9)
                loop(i) {
                    state {
                        en = ONE
                        d = q + 1
                        assert(q == SysInteger(8, i.it / 2)) {
                            "#${i.it}: Expected ${i.it / 2}, Actual $q"
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

        var ad by readWriteSignal("ad", a.d.inp)
        var bd by readWriteSignal("bd", b.d.inp)

        val aq by readOnlySignal("aq", a.q.out)
        val bq by readOnlySignal("bq", b.q.out)

        init {
            bind(a.clk to clk, b.clk to clk)

            val values = arrayOf(1, 2, 3, 5, 8, 13, 21, 34, 55)

            stateFunction(clk) {
                init {
                    aen = ZERO
                    ben = ZERO
                }
                val i = iterator(0..17)
                loop(i) {
                    state {
                        aen = ONE
                        ben = ONE
                        ad = bq
                        bd = aq + bq
                        assert(bq == SysInteger(8, values[i.it / 2])) {
                            "#${i.it}: Expected ${values[i.it / 2]}, Actual $bq"
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
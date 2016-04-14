package ru.spbstu.sysk.channels

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.NS
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit.ONE
import ru.spbstu.sysk.data.SysBit.ZERO
import ru.spbstu.sysk.data.integer.SysInteger

class SysRegisterTest {

    private class Counter : SysTopModule("counter") {
        val a = register("a", SysInteger(8, 0))

        val clk = clock("clk", 20(NS))

        var en by bitSignalWriter("aen", a.en)
        var d by signalWriter("ad", a.d)
        val q by signalReader("aq", a.q)

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

        val clk = clock("clk", 20(NS))

        var aen by bitSignalWriter("aen", a.en)
        var ben by bitSignalWriter("ben", b.en)

        var ad by signalWriter("ad", a.d)
        var bd by signalWriter("bd", b.d)

        val aq by signalReader("aq", a.q)
        val bq by signalReader("bq", b.q)

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
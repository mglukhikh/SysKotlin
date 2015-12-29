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

        val en = bitSignal("aen")
        val d = signal<SysInteger>("ad")
        val q = signal<SysInteger>("aq")

        init {
            bind(a.clk to clk, a.en to en)
            bind(a.d to d)
            bind(a.q to q)

            stateFunction(clk) {
                init {
                    en.value = ZERO
                }
                forEach(0..9) {
                    state {
                        en.value = ONE
                        d.value = q.value + SysInteger.valueOf(1)
                        assert(q.value == SysInteger(8, it / 2)) {
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

        val aen = bitSignal("aen")
        val ben = bitSignal("ben")

        val ad = signal<SysInteger>("ad")
        val bd = signal<SysInteger>("bd")

        val aq = signal<SysInteger>("aq")
        val bq = signal<SysInteger>("bq")

        init {
            bind(a.clk to clk, b.clk to clk, a.en to aen, b.en to ben)
            bind(a.d to ad, b.d to bd)
            bind(a.q to aq, b.q to bq)

            val values = arrayOf(1, 2, 3, 5, 8, 13, 21, 34, 55)

            stateFunction(clk) {
                init {
                    aen.value = ZERO
                    ben.value = ZERO
                }
                forEach(0..17) {
                    state {
                        aen.value = ONE
                        ben.value = ONE
                        ad.value = bq.value
                        bd.value = aq.value + bq.value
                        assert(bq.value == SysInteger(8, values[it / 2])) {
                            "#$it: Expected ${values[it/2]}, Actual ${bq.value}"
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
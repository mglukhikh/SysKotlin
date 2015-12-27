package sysk.data

import org.junit.Test
import sysk.core.SysTopModule
import sysk.core.TimeUnit
import sysk.core.time

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

            stagedFunction(clk) {
                initStage {
                    en.value = SysBit.ZERO
                }
                iterativeStage(0..9) {
                    stage {
                        en.value = SysBit.ONE
                        d.value = q.value + SysInteger.valueOf(1)
                        assert(q.value == SysInteger(8, it / 2)) {
                            "#$it: Expected ${it/2}, Actual ${q.value}"
                        }
                    }
                }
                stage {
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

            stagedFunction(clk) {
                initStage {
                    aen.value = SysBit.ZERO
                    ben.value = SysBit.ZERO
                }
                iterativeStage(0..17) {
                    stage {
                        aen.value = SysBit.ONE
                        ben.value = SysBit.ONE
                        ad.value = bq.value
                        bd.value = aq.value + bq.value
                        assert(bq.value == SysInteger(8, values[it / 2])) {
                            "#$it: Expected ${values[it/2]}, Actual ${bq.value}"
                        }
                    }
                }
                stage {
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
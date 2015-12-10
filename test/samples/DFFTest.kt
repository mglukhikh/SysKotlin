package samples

import modules.SysAndNotModule
import org.junit.Test
import sysk.*

class DFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val d = output<SysBit>("d")

        val clk = wireInput("clk")
        val q   = wireInput("q")

        private var counter = 0
        private var phase = 0

        init {
            function(clk) {
                if (it is SysWait.Initialize) {
                    d.value = SysBit.X
                } else {
                    when (counter) {
                        0 -> {
                            assert(q.x) { "q should be x at the beginning" }
                        }
                        1 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            // All changes at clock N are received at clock N+1 and processed at clock N+2
                            d.value = SysBit.ONE
                        }
                        2 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            d.value = SysBit.ZERO
                        }
                        3 -> {
                            assert(q.one) { "q should be true after D = 1" }
                        }
                        4 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                        }
                    }
                    counter++
                    if (counter > 4) {
                        counter = 1
                        phase++
                        if (phase == 4) {
                            scheduler.stop()
                        }
                    }
                }
            }
        }
    }

    private class Top : SysTopModule("top") {
        val d = signal("d", SysBit.X)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))
        val q = signal("q", SysBit.X)

        val ff = DFF("my", this)

        private val tb = Testbench("your", this)

        init {
            bind(ff.d to d, ff.clk to clk, tb.clk to clk, tb.q to q)
            bind(ff.q to q, tb.d to d)
        }
    }

    @Test
    fun test() {
        Top().start()
    }

    private class NotTestbench : SysTopModule("top") {
        val d = signal("d", SysBit.X)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))
        val q = wireSignal("q", SysBit.X)

        val swapOrOne = signal("en", SysBit.X)

        val ff = DFF("my", this)
        val andNot = SysAndNotModule("andNot", this)

        init {
            bind(ff.d to d, ff.clk to clk, andNot.x1 to q, andNot.x2 to swapOrOne)
            bind(ff.q to q, andNot.y to d)

            stagedFunction(clk) {
                stage {
                    assert(q.x)
                    swapOrOne.value = SysBit.ZERO
                }
                stage {
                    assert(q.x)
                    swapOrOne.value = SysBit.ONE
                }
                stage {
                    assert(q.one)
                }
                stage {
                    assert(q.zero)
                }
                stage {
                    assert(q.one)
                }
                stage {
                    assert(q.zero)
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun notTest() {
        NotTestbench().start()
    }
}

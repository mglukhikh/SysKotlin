package samples

import org.junit.Test
import sysk.*

class RSFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val r = output<SysBit>("j")
        val s = output<SysBit>("k")

        val clk = wireInput("clk")
        val q   = wireInput("q")

        private var counter = 0
        private var phase = 0

        init {
            function(clk) {
                if (it is SysWait.Initialize) {
                    r.value = SysBit.X
                    s.value = SysBit.X
                } else {
                    when (counter) {
                        0 -> {
                            assert(q.x) { "q should be x at the beginning" }
                        }
                        1 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and RS = XX" }
                            else
                                assert(q.zero) { "q should be false after q = false and RS = 00" }

                            // All changes at clock N are received at clock N+1 and processed at clock N+2
                            s.value = SysBit.ONE
                        }
                        2 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and RS = XX" }
                            else
                                assert(q.zero) { "q should be false after q = false and RS = 00" }

                            s.value = SysBit.ZERO
                        }
                        3 -> {
                            assert(q.one) { "q should be true after RS = 01 or RS = X1" }
                            r.value = SysBit.ONE
                        }
                        4 -> {
                            assert(q.one) { "q should be true after q = true and RS = 00 or RS = X0" }
                            r.value = SysBit.ZERO
                        }
                        5 -> {
                            assert(q.zero) { "q should be false after RS = 10" }
                        }
                    }
                    counter++
                    if (counter > 5) {
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

    private class Top : SysTopModule("top", SysScheduler()) {
        val r = signal("r", SysBit.X)
        val s = signal("s", SysBit.X)

        val q = signal("q", SysBit.X)
        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        val ff = RSFF("my", this)
        private val tb = Testbench("your", this)

        init {
            bind(ff.r to r, ff.s to s, ff.clk to clk, tb.clk to clk, tb.q to q)
            bind(ff.q to q, tb.r to r, tb.s to s)
        }
    }

    @Test
    fun test() {
        Top().start()
    }
}

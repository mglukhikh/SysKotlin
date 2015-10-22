package samples

import org.junit.Test
import sysk.*

class RSFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val r = output<SysWireState>("j")
        val s = output<SysWireState>("k")

        val clk = wireInput("clk")
        val q   = wireInput("q")

        private var counter = 0
        private var phase = 0

        private val f: SysTriggeredFunction = triggeredFunction({
            if (it) {
                r.value = SysWireState.X
                s.value = SysWireState.X
            }
            else {
                println("$currentTime: q = ${q.value} counter = $counter")
                when (counter) {
                    0 -> {
                        assert(q.x) { "q should be x at the beginning" }
                        println("ZERO")
                    }
                    1 -> {
                        if (phase == 0)
                            assert(q.x) { "q should be x after q = x and RS = XX" }
                        else
                            assert(q.zero) { "q should be false after q = false and RS = 00" }

                        // All changes at clock N are received at clock N+1 and processed at clock N+2
                        s.value = SysWireState.ONE
                        println("ONE")
                    }
                    2 -> {
                        if (phase == 0)
                            assert(q.x) { "q should be x after q = x and RS = XX" }
                        else
                            assert(q.zero) { "q should be false after q = false and RS = 00" }

                        s.value = SysWireState.ZERO
                        println("TWO")
                    }
                    3 -> {
                        assert(q.one) { "q should be true after RS = 01 or RS = X1" }
                        r.value = SysWireState.ONE
                        println("THREE")
                    }
                    4 -> {
                        assert(q.one) { "q should be true after q = true and RS = 00 or RS = X0" }
                        r.value = SysWireState.ZERO
                        println("FOUR")
                    }
                    5 -> {
                        assert(q.zero) { "q should be false after RS = 10" }
                        println("FIVE")
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
            f.wait()
        }, clk)
    }

    private class Top : SysTopModule("top", SysScheduler()) {
        val r = signal("r", SysWireState.X)
        val s = signal("s", SysWireState.X)

        val q = signal("q", SysWireState.X)
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

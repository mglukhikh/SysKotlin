package samples

import org.junit.Test
import sysk.*

class JKFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val j = output<SysWireState>("j")
        val k = output<SysWireState>("k")

        val clk = wireInput("clk")
        val q   = wireInput("q")

        init {
            stagedFunction(clk.posEdgeEvent) {
                initStage {
                    j.value = SysWireState.ZERO
                    k.value = SysWireState.ZERO
                }
                stage {
                    assert(q.zero) { "q should be false at the beginning" }
                }
                iterativeStage(0, 4) {
                    stage {
                        assert(q.zero) { "q should be false after q = true and JK = 11" }
                        // All changes at clock N are received at clock N+1 and processed at clock N+2
                        j.value = SysWireState.ONE
                    }
                    stage {
                        assert(q.zero) { "q should be false after q = false and JK = 00" }
                        j.value = SysWireState.ZERO
                    }
                    stage {
                        assert(q.one) { "q should be true after JK = 10" }
                        k.value = SysWireState.ONE
                    }
                    stage {
                        assert(q.one) { "q should be true after q = true and JK = 00" }
                        j.value = SysWireState.ONE
                    }
                    stage {
                        assert(q.zero) { "q should be false after JK = 01" }
                    }
                    stage {
                        assert(q.one) { "q should be true after q = false and JK = 11" }
                        j.value = SysWireState.ZERO
                        k.value = SysWireState.ZERO
                    }
                }
                stage {
                    scheduler.stop()
                }
            }
        }
    }

    private class Top: SysTopModule("top", SysScheduler()) {
        val j = signal("j", SysWireState.ZERO)
        val k = signal("k", SysWireState.ZERO)
        val q = signal("q", SysWireState.ZERO)
        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        val ff = JKFF("my", this)
        private val tb = Testbench("your", this)

        init {
            bind(ff.j to j, ff.k to k, ff.clk to clk, tb.clk to clk, tb.q to q)
            bind(ff.q to q, tb.j to j, tb.k to k)
        }
    }

    @Test
    fun test() {
        Top().start()
    }
}
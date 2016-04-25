package ru.spbstu.sysk.samples.triggers

import org.junit.Test
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.data.SysBit.*

class JKFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val j = bitOutput("j")
        val k = bitOutput("k")

        val clk = bitInput("clk")
        val q   = bitInput("q")

        init {
            stateFunction(clk) {
                init {
                    j(ZERO)
                    k(ZERO)
                }
                state {
                    assert(q.zero) { "q should be false at the beginning" }
                }
                loop(0..4) {
                    state {
                        assert(q.zero) { "q should be false after q = true and JK = 11" }
                        // All changes at clock N are received at clock N+1 and processed at clock N+2
                        j(ONE)
                    }
                    state {
                        assert(q.zero) { "q should be false after q = false and JK = 00" }
                        j(ZERO)
                    }
                    state {
                        assert(q.one) { "q should be true after JK = 10" }
                        k(ONE)
                    }
                    state {
                        assert(q.one) { "q should be true after q = true and JK = 00" }
                        j(ONE)
                    }
                    state {
                        assert(q.zero) { "q should be false after JK = 01" }
                    }
                    state {
                        assert(q.one) { "q should be true after q = false and JK = 11" }
                        j(ZERO)
                        k(ZERO)
                    }
                }
                stop(scheduler)
            }
        }
    }

    private class Top: SysTopModule("top", SysScheduler()) {
        val j = bitSignal("j", ZERO)
        val k = bitSignal("k", ZERO)
        val q = bitSignal("q", ZERO)
        val clk = clock("clk", 20(NS))

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
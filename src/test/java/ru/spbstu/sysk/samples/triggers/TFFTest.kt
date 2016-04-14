package ru.spbstu.sysk.samples.triggers

import org.junit.Test
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.channels.bind

class TFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val t = output<SysBit>("t")

        val clk = bitInput("clk")
        val q   = bitInput("q")

        private var counter = 0
        private var phase = 0

        init {
            function(clk) {
                if (it is SysWait.Initialize) {
                    t(ZERO)
                } else {
                    when (counter) {
                        0 -> {
                            assert(q.zero) { "q should be false at the beginning" }
                        }
                        1 -> {
                            assert(q.zero) { "q should be false after q = true and T = 1" }
                            // All changes at clock N are received at clock N+1 and processed at clock N+2
                            t(ONE)
                        }
                        2 -> {
                            assert(q.zero) { "q should be false after q = false and T = 0" }
                            t(ZERO)
                        }
                        3 -> {
                            assert(q.one) { "q should be true after T = 1" }
                            t(ONE)
                        }
                        4 -> {
                            assert(q.one) { "q should be true after q = true and T = 0" }
                            t(ZERO)
                        }
                        5 -> {
                            assert(q.zero) { "q should be false after q = true and T = 1" }
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
        val t = bitSignal("t", ZERO)

        val clk = clock("clk", 20(NS))
        val q = bitSignal("q", ZERO)

        val ff = TFF("my", this)
        private val tb = Testbench("your", this)

        init {
            bind(ff.t to t, ff.clk to clk, tb.clk to clk, tb.q to q)
            bind(ff.q to q, tb.t to t)
        }
    }

    @Test
    fun test() {
        Top().start()
    }
}

package ru.spbstu.sysk.samples.triggers

import org.junit.Test
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.channels.bind

class RSFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val r = output<SysBit>("j")
        val s = output<SysBit>("k")

        val clk = bitInput("clk")
        val q   = bitInput("q")

        private var counter = 0
        private var phase = 0

        init {
            function(clk) {
                if (it is SysWait.Initialize) {
                    r(X)
                    s(X)
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
                            s(ONE)
                        }
                        2 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and RS = XX" }
                            else
                                assert(q.zero) { "q should be false after q = false and RS = 00" }

                            s(ZERO)
                        }
                        3 -> {
                            assert(q.one) { "q should be true after RS = 01 or RS = X1" }
                            r(ONE)
                        }
                        4 -> {
                            assert(q.one) { "q should be true after q = true and RS = 00 or RS = X0" }
                            r(ZERO)
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
        val r = bitSignal("r", X)
        val s = bitSignal("s", X)

        val q = bitSignal("q", X)
        val clk = clock("clk", 20(NS))

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

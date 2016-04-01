package ru.spbstu.sysk.samples

import org.junit.Test
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.channels.SysBitInput
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.generics.NAND

class DFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val d = output<SysBit>("d")
        private var dval by portWriter(d)

        val clk = bitInput("clk")

        val q   = bitInput("q")
        private val qval by bitPortReader(q)

        private var counter = 0
        private var phase = 0

        init {
            function(clk) {
                if (it is SysWait.Initialize) {
                    dval = X
                } else {
                    when (counter) {
                        0 -> {
                            assert(qval == X) { "q should be x at the beginning" }
                        }
                        1 -> {
                            if (phase == 0)
                                assert(qval == X) { "q should be x after q = x and D = X" }
                            else
                                assert(qval == ZERO) { "q should be false after q = false and D = 0" }

                            // All changes at clock N are received at clock N+1 and processed at clock N+2
                            dval = ONE
                        }
                        2 -> {
                            if (phase == 0)
                                assert(qval == X) { "q should be x after q = x and D = X" }
                            else
                                assert(qval == ZERO) { "q should be false after q = false and D = 0" }

                            dval = ZERO
                        }
                        3 -> {
                            assert(qval == ONE) { "q should be true after D = 1" }
                        }
                        4 -> {
                            assert(qval == ZERO) { "q should be false after D = 0" }
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
        private val ff = DFF("my", this)

        private val tb = Testbench("your", this)

        val clk = clock("clk", 20(NS))

        init {
            connector("d", tb.d, ff.d)
            connector("q", ff.q, tb.q)
            bind(ff.clk to clk, tb.clk to clk)
        }
    }

    @Test
    fun test() {
        Top().start()
    }

    private class NotTestbench : SysTopModule("top") {
        val ff = DFF("my", this)

        val andNot = NAND("andNot", this)

        val clk = clock("clk", 20(NS))
        val q by bitSignalReader("q", ff.q, andNot.x1 as SysBitInput)

        var swapOrOne by bitSignalWriter("en", andNot.x2 as SysBitInput)

        init {
            connector("d", andNot.y, ff.d)
            bind(ff.clk to clk)

            stateFunction(clk) {
                state {
                    assert(q.x)
                    swapOrOne = ZERO
                }
                state {
                    assert(q.x)
                    swapOrOne = ONE
                }
                state {
                    assert(q.one)
                }
                state {
                    assert(q.zero)
                }
                state {
                    assert(q.one)
                }
                state {
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

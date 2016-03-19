package ru.spbstu.sysk.samples

import org.junit.Test
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.SysBitInput
import ru.spbstu.sysk.data.bind
import ru.spbstu.sysk.generics.SysAndNotModule

class DFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val d = readWritePort<SysBit>("d")
        private var dval by d

        val clk = bitInput("clk")

        val q   = readOnlyBitPort("q")
        private val qval by q

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

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bindSignal("d", tb.d.out, ff.d.inp)
            bindSignal("q", ff.q.out, tb.q.inp)
            bind(ff.clk to clk, tb.clk to clk)
        }
    }

    @Test
    fun test() {
        Top().start()
    }

    private class NotTestbench : SysTopModule("top") {
        val ff = DFF("my", this)

        val andNot = SysAndNotModule("andNot", this)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))
        val q by readOnlyBitSignal("q", ff.q.out, andNot.x1 as SysBitInput)

        var swapOrOne by readWriteBitSignal("en", andNot.x2 as SysBitInput)

        init {
            bindSignal("d", andNot.y, ff.d.inp)
            bind(ff.clk to clk)

            stateFunction(clk) {
                State {
                    assert(q.x)
                    swapOrOne = ZERO
                }
                State {
                    assert(q.x)
                    swapOrOne = ONE
                }
                State {
                    assert(q.one)
                }
                State {
                    assert(q.zero)
                }
                State {
                    assert(q.one)
                }
                State {
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

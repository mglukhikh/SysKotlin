package samples

import org.junit.Test
import sysk.*

class SRegTest {

    private class Testbench(name: String, digPerWord: Int, parent: SysModule): SysModule(name, parent) {

        val d   = output<SysWireState>("d")
        val dir = output<SysWireState>("dir")

        val clk = wireInput("clk")
        val q   = wireInput("q")

        private var counter = 0
        private var phase = 0

        init {
            triggeredFunction(clk) {

                if (it is SysWait.Initialize) {
                    d.value = SysWireState.X
                    dir.value = SysWireState.X
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
                            dir.value = SysWireState.ZERO
                            d.value = SysWireState.ONE
                        }
                        2 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            dir.value = SysWireState.ZERO
                            d.value = SysWireState.ZERO
                        }
                        3 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            dir.value = SysWireState.ZERO
                            d.value = SysWireState.ONE
                        }
                        4 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            dir.value = SysWireState.ZERO
                            d.value = SysWireState.ZERO
                        }
                        5 -> {
                            assert(q.one) { "q should be true after D = 1" }
                            dir.value = SysWireState.ZERO
                        }
                        6 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysWireState.ZERO
                        }
                        7 -> {
                            assert(q.one) { "q should be true after D = 1" }
                        }
                        8 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysWireState.ONE
                            d.value = SysWireState.ONE
                        }
                        9 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysWireState.ONE
                            d.value = SysWireState.ZERO
                        }
                        10 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysWireState.ONE
                            d.value = SysWireState.ONE
                        }
                        11 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysWireState.ONE
                            d.value = SysWireState.ZERO
                        }
                        12 -> {
                            assert(q.one) { "q should be true after D = 1" }
                            dir.value = SysWireState.ONE
                        }
                        13 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysWireState.ONE
                        }
                        14 -> {
                            assert(q.one) { "q should be true after D = 1" }
                        }
                        15 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                        }
                    }
                    counter++
                    if (counter > 15) {
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
        val digPerWord = 4

        val d = signal("d", SysWireState.X)
        val q = signal("q", SysWireState.X)
        val dir = signal("dir", SysWireState.X)
        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        val ff = SReg("my", digPerWord, this)
        private val tb = Testbench("your", digPerWord, this)

        init {
            bind(ff.d to d, ff.dir to dir, ff.clk to clk, tb.clk to clk, tb.q to q)
            bind(ff.q to q, tb.d to d, tb.dir to dir)
        }
    }

    @Test
    fun test() {
        Top().start()
    }
}


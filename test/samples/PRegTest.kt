package samples

import java.util.Random
import org.junit.Test
import sysk.*

class PRegTest {

    private class Testbench(name: String, digPerWord: Int, parent: SysModule): SysModule(name, parent) {

        val d = Array(digPerWord, {i -> output<SysBit>("d" + i.toString())})
        val clk = bitInput("clk")
        val q   = Array(digPerWord, {i -> bitInput("q" + i.toString())})

        private var counter = 0
        private var phase = 0
        private var i = 0
        private var r = Random()
        private var arr = BooleanArray(digPerWord)

        init {
            function(clk) {
                if (it is SysWait.Initialize) {
                    while (i < digPerWord) {
                        d[i].value = SysBit.X
                        i += 1
                    }
                } else {
                    when (counter) {
                        0 -> {
                            i = 0
                            while (i < digPerWord) {
                                assert(q[i].x) { "q[$i] should be x at the beginning" }
                                i += 1
                            }
                        }
                        1 -> {
                            if (phase == 0) {
                                i = 0
                                while (i < digPerWord) {
                                    assert(q[i].x) { "q[$i] should be x after q[$i] = x and D[$i] = X" }
                                    i += 1
                                }
                            } else {
                                i = 0
                                while (i < digPerWord) {
                                    assert(q[i].zero) { "q[$i] should be false after q[$i] = false and D[$i] = 0" }
                                    i += 1
                                }
                            }
                            // All changes at clock N are received at clock N+1 and processed at clock N+2

                            i = 0
                            while (i < digPerWord) {
                                arr[i] = r.nextBoolean()
                                if (arr[i]) d[i].value = SysBit.ONE
                                else d[i].value = SysBit.ZERO
                                i += 1
                            }
                        }
                        2 -> {
                            if (phase == 0) {
                                i = 0
                                while (i < digPerWord) {
                                    assert(q[i].x) { "q[$i] should be x after q[$i] = x and D[$i] = X" }
                                    i += 1
                                }
                            } else {
                                i = 0
                                while (i < digPerWord) {
                                    assert(q[i].zero) { "q[$i] should be false after q[$i] = false and D[$i] = 0" }
                                    i += 1
                                }
                            }

                            i = 0
                            while (i < digPerWord) {
                                d[i].value = SysBit.ZERO
                                i += 1
                            }
                        }
                        3 -> {
                            i = 0
                            while (i < digPerWord) {
                                if (arr[i]) assert(q[i].one) { "q[$i] should be true after D[$i] = 1" }
                                else assert(q[i].zero) { "q[$i] should be false after D[$i] = 0" }
                                i += 1
                            }
                        }
                        4 -> {
                            i = 0
                            while (i < digPerWord) {
                                assert(q[i].zero) { "q[$i] should be false after D = 0" }
                                i += 1
                            }
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

    private class Top : SysTopModule("top", SysScheduler()) {
        val digPerWord = 64
        val d = Array(digPerWord, {i -> signal<SysBit>("d" + i.toString())})

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))
        val q = Array(digPerWord, {i -> signal<SysBit>("q" + i.toString())})

        val ff = PReg("my", digPerWord, this)
        private val tb = Testbench("your", digPerWord, this)

        init {
            bind( ff.clk to clk, tb.clk to clk)
            bindArrays(ff.d to d, tb.q to q)
            bindArrays(ff.q to q, tb.d to d)
        }
    }

    @Test
    fun test() {
        Top().start()
    }
}

package ru.spbstu.sysk.samples

import org.junit.Test
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.bind

class SRegTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val d   = output<SysBit>("d")
        val dir = output<SysBit>("dir")

        val clk = bitInput("clk")
        val q   = bitInput("q")

        private var counter = 0
        private var phase = 0

        init {
            function(clk) {

                if (it is SysWait.Initialize) {
                    d.value = SysBit.X
                    dir.value = SysBit.X
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
                            dir.value = SysBit.ZERO
                            d.value = SysBit.ONE
                        }
                        2 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            dir.value = SysBit.ZERO
                            d.value = SysBit.ZERO
                        }
                        3 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            dir.value = SysBit.ZERO
                            d.value = SysBit.ONE
                        }
                        4 -> {
                            if (phase == 0)
                                assert(q.x) { "q should be x after q = x and D = X" }
                            else
                                assert(q.zero) { "q should be false after q = false and D = 0" }

                            dir.value = SysBit.ZERO
                            d.value = SysBit.ZERO
                        }
                        5 -> {
                            assert(q.one) { "q should be true after D = 1" }
                            dir.value = SysBit.ZERO
                        }
                        6 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysBit.ZERO
                        }
                        7 -> {
                            assert(q.one) { "q should be true after D = 1" }
                        }
                        8 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysBit.ONE
                            d.value = SysBit.ONE
                        }
                        9 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysBit.ONE
                            d.value = SysBit.ZERO
                        }
                        10 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysBit.ONE
                            d.value = SysBit.ONE
                        }
                        11 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysBit.ONE
                            d.value = SysBit.ZERO
                        }
                        12 -> {
                            assert(q.one) { "q should be true after D = 1" }
                            dir.value = SysBit.ONE
                        }
                        13 -> {
                            assert(q.zero) { "q should be false after D = 0" }
                            dir.value = SysBit.ONE
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

        val d = signal<SysBit>("d")
        val q = signal<SysBit>("q")
        val dir = signal<SysBit>("dir")
        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        val ff = SReg("my", digPerWord, this)
        private val tb = Testbench("your", this)

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


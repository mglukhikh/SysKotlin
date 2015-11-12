package samples

import org.junit.Test
import sysk.*

class SRegTest {

    private class Testbench(name: String, digPerWord: Int, parent: SysModule): SysModule(name, parent) {

        val d = output<SysWireState>("d")
        val dir = output<SysWireState>("dir")

        val clk = wireInput("clk")
        val q   = wireInput("q")

        private var counter = 0
        private var phase = 0

        private val f: SysTriggeredFunction = triggeredFunction({
            if (it is SysWait.Initialize) {
                d.value = SysWireState.X
                dir.value = SysWireState.X
            }
            else {
                println("$currentTime: q = $q counter = $counter")
                when (counter) {
                    0 -> {
                        assert(q.x) { "q should be x at the beginning" }
                        println("ZERO")
                    }
                    1 -> {
                        if (phase == 0)
                            assert(q.x) { "q should be x after q = x and D = X" }
                        else
                            assert(q.zero) { "q should be false after q = false and D = 0" }

                        // All changes at clock N are received at clock N+1 and processed at clock N+2
                        dir.value = SysWireState.ZERO
                        d.value = SysWireState.ONE
                        println("ONE")
                    }
                    2 -> {
                        if (phase == 0)
                            assert(q.x) { "q should be x after q = x and D = X" }
                        else
                            assert(q.zero) { "q should be false after q = false and D = 0" }

                        dir.value = SysWireState.ZERO
                        d.value = SysWireState.ZERO
                        println("TWO")
                    }
                    3 -> {
                        if (phase == 0)
                            assert(q.x) { "q should be x after q = x and D = X" }
                        else
                            assert(q.zero) { "q should be false after q = false and D = 0" }

                        dir.value = SysWireState.ZERO
                        d.value = SysWireState.ONE
                        println("THREE")
                    }
                    4 -> {
                        if (phase == 0)
                            assert(q.x) { "q should be x after q = x and D = X" }
                        else
                            assert(q.zero) { "q should be false after q = false and D = 0" }

                        dir.value = SysWireState.ZERO
                        d.value = SysWireState.ZERO
                        println("FOUR")
                    }
                    5 -> {
                        assert(q.one) { "q should be true after q = x and D = 1" }
                        dir.value = SysWireState.ZERO
                        println("FIVE")
                    }
                    6 -> {
                        assert(q.zero) { "q should be false after q = 1 and D = 0" }
                        dir.value = SysWireState.ZERO
                        println("SIX")
                    }
                    7 -> {
                        assert(q.one) { "q should be true after q = 0 and D = 1" }
                        println("SEVEN")
                    }
                    8 -> {
                        assert(q.zero) { "q should be false after q = 1 and D = 0" }
                        dir.value = SysWireState.ONE
                        d.value = SysWireState.ONE
                        println("EIGHT")
                    }
                    9 -> {
                        assert(q.zero) { "q should be false after q = 1 and D = 0" }
                        dir.value = SysWireState.ONE
                        d.value = SysWireState.ZERO
                        println("NINE")
                    }
                    10 -> {
                        assert(q.zero) { "q should be false after q = 1 and D = 0" }
                        dir.value = SysWireState.ONE
                        d.value = SysWireState.ONE
                        println("TEN")
                    }
                    11 -> {
                        assert(q.zero) { "q should be false after q = 1 and D = 0" }
                        dir.value = SysWireState.ONE
                        d.value = SysWireState.ZERO
                        println("ELEVEN")
                    }
                    12 -> {
                        assert(q.one) { "q should be true after q = 0 and D = 1" }
                        dir.value = SysWireState.ONE
                        println("TWELVE")
                    }
                    13 -> {
                        assert(q.zero) { "q should be false after q = 1 and D = 0" }
                        dir.value = SysWireState.ONE
                        println("THIRTEEN")
                    }
                    14 -> {
                        assert(q.one) { "q should be true after q = 0 and D = 1" }
                        println("FOURTEEN")
                    }
                    15 -> {
                        assert(q.zero) { "q should be false after q = 1 and D = 0" }
                        println("FIFTEEN")
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
            f.wait()
        }, clk)
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
            bindArrays(ff.)
        }
    }

    @Test
    fun test() {
        Top().start()
    }
}


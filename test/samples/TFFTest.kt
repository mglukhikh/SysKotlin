package samples

import org.junit.Test
import sysk.*

private class TestbenchTTrigger(name: String, parent: SysModule): SysModule(name, parent) {

    val t = output<SysWireState>("t")

    val clk = wireInput("clk")
    val q   = wireInput("q")

    private var counter = 0
    private var phase = 0

    private val f: SysTriggeredFunction = triggeredFunction({
        if (it) {
            t.value = SysWireState.ZERO
        }
        else {
            println("$currentTime: q = ${q.value} counter = $counter")
            when (counter) {
                0 -> {
                    assert(q.zero) { "q should be false at the beginning" }
                    println("ZERO")
                }
                1 -> {
                    assert(q.zero) { "q should be false after q = true and T = 1" }
                    // All changes at clock N are received at clock N+1 and processed at clock N+2
                    t.value = SysWireState.ONE
                    println("ONE")
                }
                2 -> {
                    assert(q.zero) { "q should be false after q = false and T = 0" }
                    t.value = SysWireState.ZERO
                    println("TWO")
                }
                3 -> {
                    assert(q.one) { "q should be true after T = 1" }
                    t.value = SysWireState.ONE
                    println("THREE")
                }
                4 -> {
                    assert(q.one) { "q should be true after q = true and T = 0" }
                    t.value = SysWireState.ZERO
                    println("FOUR")
                }
                5 -> {
                    assert(q.zero) { "q should be false after q = true and T = 1" }
                    println("FIVE")
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
        f.wait()
    }, clk)
}

internal class TopTTrigger: SysTopModule("top", SysScheduler()) {
    val t = signal("t", SysWireState.ZERO)

    val clk = clockedSignal("clk", time(20, TimeUnit.NS))
    val q = signal("q", SysWireState.ZERO)

    val ff = TFF("my", this)
    val tb = TestbenchTTrigger("your", this)

    init {
        bind(ff.t to t, ff.clk to clk, tb.clk to clk, tb.q to q)
        bind(ff.q to q, tb.t to t)
    }
}

class TFFTest {

    @Test
    fun test() {
        TopTTrigger().start()
    }
}

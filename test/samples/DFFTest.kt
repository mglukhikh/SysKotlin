package samples

import org.junit.Test
import sysk.*

private class TestbenchDTrigger(name: String, parent: SysModule): SysModule(name, parent) {

    val d = output<SysWireState>("d")

    val clk = wireInput("clk")
    val q   = wireInput("q")

    private var counter = 0
    private var phase = 0

    private val f: SysTriggeredFunction = triggeredFunction({
        if (it) {
            d.value = SysWireState.ZERO
        }
        else {
            println("$currentTime: q = ${q.value} counter = $counter")
            when (counter) {
                0 -> {
                    assert(q.zero) { "q should be false at the beginning" }
                    println("ZERO")
                }
                1 -> {
                    assert(q.zero) { "q should be false after q = false and D = 0" }
                    // All changes at clock N are received at clock N+1 and processed at clock N+2
                    d.value = SysWireState.ONE
                    println("ONE")
                }
                2 -> {
                    assert(q.zero) { "q should be false after q = false and D = 0" }
                    d.value = SysWireState.ZERO
                    println("TWO")
                }
                3 -> {
                    assert(q.one) { "q should be true after D = 1" }
                    println("THREE")
                }
                4 -> {
                    assert(q.zero) { "q should be false after D = 0" }
                    println("FOUR")
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
        f.wait()
    }, clk)
}

internal class TopDTrigger: SysTopModule("top", SysScheduler()) {
    val d = signal("d", SysWireState.ZERO)

    val clk = clockedSignal("clk", time(20, TimeUnit.NS))
    val q = signal("q", SysWireState.ZERO)

    val ff = DFF("my", this)
    val tb = TestbenchDTrigger("your", this)

    init {
        bind(ff.d to d, ff.clk to clk, tb.clk to clk, tb.q to q)
        bind(ff.q to q, tb.d to d)
    }
}

class DFFTest {

    @Test
    fun test() {
        TopDTrigger().start()
    }
}

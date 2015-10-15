package samples

import sysk.*

class JKFF(name: String, parent: SysModule): SysModule(name, parent) {

    val j   = wireInput("j")
    val k   = wireInput("k")
    val clk = wireInput("clk")

    private var state = SysWireState.ZERO
    val q = output<SysWireState>("q")

    private val f: SysTriggeredFunction = triggeredFunction({
        println("$currentTime: j = ${j.value} k = ${k.value} state = $state")
        if (j.one && state.zero) state = SysWireState.ONE
        else if (k.one && state.one) state = SysWireState.ZERO
        q.value = state
        f.wait()
    }, clk, initialize = false)
}

private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

    val j = output<SysWireState>("j")
    val k = output<SysWireState>("k")

    val clk = wireInput("clk")
    val q   = wireInput("q")

    private var counter = 0
    private var phase = 0

    private val f: SysTriggeredFunction = triggeredFunction({
        if (it) {
            j.value = SysWireState.ZERO
            k.value = SysWireState.ZERO
        }
        else {
            println("$currentTime: q = ${q.value} counter = $counter")
            when (counter) {
                0 -> {
                    assert(q.zero) { "q should be false at the beginning" }
                    println("ZERO")
                }
                1 -> {
                    assert(q.zero) { "q should be false after q = true and JK = 11" }
                    // All changes at clock N are received at clock N+1 and processed at clock N+2
                    j.value = SysWireState.ONE
                    println("ONE")
                }
                2 -> {
                    assert(q.zero) { "q should be false after q = false and JK = 00" }
                    j.value = SysWireState.ZERO
                    println("TWO")
                }
                3 -> {
                    assert(q.one) { "q should be true after JK = 10" }
                    k.value = SysWireState.ONE
                    println("THREE")
                }
                4 -> {
                    assert(q.one) { "q should be true after q = true and JK = 00" }
                    j.value = SysWireState.ONE
                    println("FOUR")
                }
                5 -> {
                    assert(q.zero) { "q should be false after JK = 01" }
                    println("FIVE")
                }
                6 -> {
                    assert(q.one) { "q should be true after q = false and JK = 11" }
                    j.value = SysWireState.ZERO
                    k.value = SysWireState.ZERO
                    println("SIX")
                }
            }
            counter++
            if (counter > 6) {
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

internal class Top: SysTopModule("top", SysScheduler()) {
    val j = signal("j", SysWireState.ZERO)
    val k = signal("k", SysWireState.ZERO)
    val q = signal("q", SysWireState.ZERO)
    val clk = clockedSignal("clk", time(20, TimeUnit.NS))

    val ff = JKFF("my", this)
    private val tb = Testbench("your", this)

    init {
        bind(ff.j to j, ff.k to k, ff.clk to clk, tb.clk to clk, tb.q to q)
        bind(ff.q to q, tb.j to j, tb.k to k)
    }
}

fun main(args: Array<String>) {
    Top().start()
}

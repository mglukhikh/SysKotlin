package samples

import sysk.*

public class JKFF(name: String, parent: SysModule? = null): SysModule(name, parent) {

    public val j: SysBooleanInput = booleanInput("j")
    public val k: SysBooleanInput = booleanInput("k")
    public val clk: SysBooleanInput = booleanInput("clk")

    private var state = false
    public val q: SysOutput<Boolean> = output("q")

    private val f: SysTriggeredFunction = triggeredFunction({
        println("${SysScheduler.currentTime}: j = ${j.value} k = ${k.value} state = $state")
        if (j.value && !state) state = true
        else if (k.value && state) state = false
        q.value = state
        f.wait()
    }, clk, initialize = false)
}

private class Testbench(name: String): SysModule(name) {

    public val j: SysOutput<Boolean> = output("j")
    public val k: SysOutput<Boolean> = output("k")

    public val clk: SysBooleanInput = booleanInput("clk")
    public val q: SysBooleanInput = booleanInput("q")

    private var counter = 0

    private val f: SysTriggeredFunction = triggeredFunction({
        if (it) {
            j.value = false
            k.value = false
        }
        else {
            println("${SysScheduler.currentTime}: q = ${q.value} counter = $counter")
            when (counter) {
                0 -> {
                    assert(!q.value, "q should be false at the beginning")
                    println("ZERO")
                }
                1 -> {
                    assert(!q.value, "q should be false after q = true and JK = 11")
                    // All changes at clock N are received at clock N+1 and processed at clock N+2
                    j.value = true
                    println("ONE")
                }
                2 -> {
                    assert(!q.value, "q should be false after q = false and JK = 00")
                    j.value = false
                    println("TWO")
                }
                3 -> {
                    assert(q.value, "q should be true after JK = 10")
                    k.value = true
                    println("THREE")
                }
                4 -> {
                    assert(q.value, "q should be true after q = true and JK = 00")
                    j.value = true
                    println("FOUR")
                }
                5 -> {
                    assert(!q.value, "q should be false after JK = 01")
                    println("FIVE")
                }
                6 -> {
                    assert(q.value, "q should be true after q = false and JK = 11")
                    j.value = false
                    k.value = false
                    println("SIX")
                }
            }
            counter++
            if (counter > 6) counter = 1
        }
        f.wait()
    }, clk)
}

public fun main(args: Array<String>) {
    val j = SysBooleanSignal("j", false)
    val k = SysBooleanSignal("k", false)
    val clk = SysClockedSignal("clk", false, time(20, TimeUnit.NS))
    val q = SysBooleanSignal("q", false)

    val ff = JKFF("my")
    val tb = Testbench("your")

    bind(ff.j to j, ff.k to k, ff.clk to clk, tb.clk to clk, tb.q to q)
    bind(ff.q to q, tb.j to j, tb.k to k)

    SysScheduler.start(time(1, TimeUnit.US))
}

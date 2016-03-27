package ru.spbstu.sysk.generics.triggers

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.samples.triggers.nRS
import java.util.*

class nRSTest : SysTopModule() {

    val nrs = nRS("nrs", this)

    val nr = bitSignal("r")
    val ns = bitSignal("s")
    val q = bitSignal("q")
    val nq = bitSignal("nq")

    /** BUG?: val clk = clockedSignal("clk", 1(FS)) */
    val clk = clockedSignal("clk", 2(FS))

    val jumps = arrayOf(
            /** arrayOf(nR, nS, Q(t), nQ(t), Q(t+1), nQ(t+1)*/
            arrayOf(ONE, ONE, X, X, X, X),
            arrayOf(ONE, ONE, ONE, X, ONE, X),
            arrayOf(ONE, ONE, ZERO, X, ZERO, X),
            arrayOf(ONE, ONE, X, ONE, X, ONE),
            arrayOf(ONE, ONE, X, ZERO, X, ZERO),
            arrayOf(ONE, ONE, ONE, ZERO, ONE, ZERO),
            arrayOf(ONE, ONE, ZERO, ONE, ZERO, ONE),

            arrayOf(ZERO, ONE, X, X, ZERO, ONE),
            arrayOf(ZERO, ONE, ONE, X, ZERO, ONE),
            arrayOf(ZERO, ONE, ZERO, X, ZERO, ONE),
            arrayOf(ZERO, ONE, X, ONE, ZERO, ONE),
            arrayOf(ZERO, ONE, X, ZERO, ZERO, ONE),
            arrayOf(ZERO, ONE, ONE, ZERO, ZERO, ONE),
            arrayOf(ZERO, ONE, ZERO, ONE, ZERO, ONE),

            arrayOf(ONE, ZERO, X, X, ONE, ZERO),
            arrayOf(ONE, ZERO, ONE, X, ONE, ZERO),
            arrayOf(ONE, ZERO, ZERO, X, ONE, ZERO),
            arrayOf(ONE, ZERO, X, ONE, ONE, ZERO),
            arrayOf(ONE, ZERO, X, ZERO, ONE, ZERO),
            arrayOf(ONE, ZERO, ONE, ZERO, ONE, ZERO),
            arrayOf(ONE, ZERO, ZERO, ONE, ONE, ZERO),

            arrayOf(ONE, X, X, X, X, X),
            arrayOf(ONE, X, ONE, X, X, X),
            arrayOf(ONE, X, ZERO, X, X, X),
            arrayOf(ONE, X, X, ONE, X, X),
            arrayOf(ONE, X, X, ZERO, X, X),
            arrayOf(ONE, X, ONE, ZERO, ONE, ZERO),
            arrayOf(ONE, X, ZERO, ONE, X, X),

            arrayOf(X, ONE, X, X, X, X),
            arrayOf(X, ONE, ONE, X, X, X),
            arrayOf(X, ONE, ZERO, X, X, X),
            arrayOf(X, ONE, X, ONE, X, X),
            arrayOf(X, ONE, X, ZERO, X, X),
            arrayOf(X, ONE, ONE, ZERO, X, X),
            arrayOf(X, ONE, ZERO, ONE, ZERO, ONE)
    )

    val random = Random(10)
    var currentState = 0
    var prevState = 0
    val rndJump: () -> Unit = {
        val currentJump = jumps[currentState]
        assert(q().equals(currentJump[4]) && nq().equals(currentJump[5])) { "Failed jump from $prevState to $currentState" }
        val IPVT = ArrayList<Int>()
        for (i in 0..jumps.lastIndex) {
            if (currentJump[4] == jumps[i][2] && currentJump[5] == jumps[i][3]) {
                IPVT.add(i)
            }
        }
        if (IPVT.isEmpty()) throw AssertionError()
        prevState = currentState
        currentState = IPVT[random.nextInt(IPVT.size)]
        nr(jumps[currentState][0])
        ns(jumps[currentState][1])
    }

    init {
        nrs.nR bind nr
        nrs.nS bind ns
        nrs.Q bind q
        nrs.nQ bind nq

        val i = iterator(0..1000)
        stateFunction(clk) {
            loop(i) {
                state(rndJump)
            }
            state {
                scheduler.stop()
            }
        }
    }

    @Test
    fun show() = start(1(S))
}

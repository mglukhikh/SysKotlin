package ru.spbstu.sysk.samples.triggers

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit.*
import java.util.*

class RSTest : SysTopModule() {

    val rs = RS("rs", this)

    val r = bitSignal("r")
    val s = bitSignal("s")
    val q = bitSignal("q")
    val nq = bitSignal("nq")

    /** BUG?: val clk = clockedSignal("clk", 1(FS)) */
    val clk = clock("clk", 2(FS))

    val jumps = arrayOf(
            /** arrayOf(R, S, Q(t), nQ(t), Q(t+1), nQ(t+1)*/
            arrayOf(ZERO, ZERO, X, X, X, X),
            arrayOf(ZERO, ZERO, ONE, X, ONE, X),
            arrayOf(ZERO, ZERO, ZERO, X, ZERO, X),
            arrayOf(ZERO, ZERO, X, ONE, X, ONE),
            arrayOf(ZERO, ZERO, X, ZERO, X, ZERO),
            arrayOf(ZERO, ZERO, ONE, ZERO, ONE, ZERO),
            arrayOf(ZERO, ZERO, ZERO, ONE, ZERO, ONE),

            arrayOf(ONE, ZERO, X, X, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, X, ZERO, ONE),
            arrayOf(ONE, ZERO, ZERO, X, ZERO, ONE),
            arrayOf(ONE, ZERO, X, ONE, ZERO, ONE),
            arrayOf(ONE, ZERO, X, ZERO, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, ZERO, ZERO, ONE),
            arrayOf(ONE, ZERO, ZERO, ONE, ZERO, ONE),

            arrayOf(ZERO, ONE, X, X, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, X, ONE, ZERO),
            arrayOf(ZERO, ONE, ZERO, X, ONE, ZERO),
            arrayOf(ZERO, ONE, X, ONE, ONE, ZERO),
            arrayOf(ZERO, ONE, X, ZERO, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, ZERO, ONE, ZERO),
            arrayOf(ZERO, ONE, ZERO, ONE, ONE, ZERO),

            arrayOf(ZERO, X, X, X, X, X),
            arrayOf(ZERO, X, ONE, X, X, X),
            arrayOf(ZERO, X, ZERO, X, X, X),
            arrayOf(ZERO, X, X, ONE, X, X),
            arrayOf(ZERO, X, X, ZERO, X, X),
            arrayOf(ZERO, X, ONE, ZERO, ONE, ZERO),
            arrayOf(ZERO, X, ZERO, ONE, X, X),

            arrayOf(X, ZERO, X, X, X, X),
            arrayOf(X, ZERO, ONE, X, X, X),
            arrayOf(X, ZERO, ZERO, X, X, X),
            arrayOf(X, ZERO, X, ONE, X, X),
            arrayOf(X, ZERO, X, ZERO, X, X),
            arrayOf(X, ZERO, ONE, ZERO, X, X),
            arrayOf(X, ZERO, ZERO, ONE, ZERO, ONE)
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
        r(jumps[currentState][0])
        s(jumps[currentState][1])
    }

    init {
        rs.R bind r
        rs.S bind s
        rs.Q bind q
        rs.nQ bind nq

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

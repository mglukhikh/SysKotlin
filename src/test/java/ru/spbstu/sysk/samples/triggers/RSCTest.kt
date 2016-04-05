package ru.spbstu.sysk.samples.triggers

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.SysBit.*
import java.util.*

class RSCTest : SysTopModule() {

    val rsc = RSC("rsc", this)

    val r = bitSignal("r")
    val s = bitSignal("s")
    val c = bitSignal("c")
    val q = bitSignal("q")
    val nq = bitSignal("nq")

    /** BUG?: val clk = clockedSignal("clk", 1(FS)) */
    val clk = clock("clk", 2(FS))

    val jumps = arrayOf(
            /** arrayOf(R, S, C, Q(t), nQ(t), Q(t+1), nQ(t+1)*/
            arrayOf(ZERO, ZERO, ONE, X, X, X, X),
            arrayOf(ZERO, ZERO, ONE, ONE, X, ONE, X),
            arrayOf(ZERO, ZERO, ONE, ZERO, X, ZERO, X),
            arrayOf(ZERO, ZERO, ONE, X, ONE, X, ONE),
            arrayOf(ZERO, ZERO, ONE, X, ZERO, X, ZERO),
            arrayOf(ZERO, ZERO, ONE, ONE, ZERO, ONE, ZERO),
            arrayOf(ZERO, ZERO, ONE, ZERO, ONE, ZERO, ONE),

            arrayOf(ONE, ZERO, ONE, X, X, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, ONE, X, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, ZERO, X, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, X, ONE, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, X, ZERO, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, ONE, ZERO, ZERO, ONE),
            arrayOf(ONE, ZERO, ONE, ZERO, ONE, ZERO, ONE),

            arrayOf(ZERO, ONE, ONE, X, X, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, ONE, X, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, ZERO, X, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, X, ONE, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, X, ZERO, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, ONE, ZERO, ONE, ZERO),
            arrayOf(ZERO, ONE, ONE, ZERO, ONE, ONE, ZERO),

            arrayOf(ZERO, X, ONE, X, X, X, X),
            arrayOf(ZERO, X, ONE, ONE, X, X, X),
            arrayOf(ZERO, X, ONE, ZERO, X, X, X),
            arrayOf(ZERO, X, ONE, X, ONE, X, X),
            arrayOf(ZERO, X, ONE, X, ZERO, X, X),
            arrayOf(ZERO, X, ONE, ONE, ZERO, ONE, ZERO),
            arrayOf(ZERO, X, ONE, ZERO, ONE, X, X),

            arrayOf(X, ZERO, ONE, X, X, X, X),
            arrayOf(X, ZERO, ONE, ONE, X, X, X),
            arrayOf(X, ZERO, ONE, ZERO, X, X, X),
            arrayOf(X, ZERO, ONE, X, ONE, X, X),
            arrayOf(X, ZERO, ONE, X, ZERO, X, X),
            arrayOf(X, ZERO, ONE, ONE, ZERO, X, X),
            arrayOf(X, ZERO, ONE, ZERO, ONE, ZERO, ONE),

            arrayOf(X, X, ZERO, X, X, X, X),
            arrayOf(X, X, ZERO, ONE, X, ONE, X),
            arrayOf(X, X, ZERO, ZERO, X, ZERO, X),
            arrayOf(X, X, ZERO, X, ONE, X, ONE),
            arrayOf(X, X, ZERO, X, ZERO, X, ZERO),
            arrayOf(X, X, ZERO, ONE, ZERO, ONE, ZERO),
            arrayOf(X, X, ZERO, ZERO, ONE, ZERO, ONE)
    )

    val random = Random(10)
    var currentState = 0
    var prevState = 0
    val rndJump: () -> Unit = {
        val currentJump = jumps[currentState]
        assert(q().equals(currentJump[5]) && nq().equals(currentJump[6])) { "Failed jump from $prevState to $currentState" }
        val IPVT = ArrayList<Int>()
        for (i in 0..jumps.lastIndex) {
            if (currentJump[5] == jumps[i][3] && currentJump[6] == jumps[i][4]) {
                IPVT.add(i)
            }
        }
        if (IPVT.isEmpty()) throw AssertionError()
        prevState = currentState
        currentState = IPVT[random.nextInt(IPVT.size)]
        r(jumps[currentState][0])
        s(jumps[currentState][1])
        c(jumps[currentState][2])

    }

    init {
        rsc.R bind r
        rsc.S bind s
        rsc.C bind c
        rsc.Q bind q
        rsc.nQ bind nq

        val i = iterator(0..1000)
        stateFunction(clk) {
            loop(i) {
                state(rndJump)
            }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start(1(S))
}

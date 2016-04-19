package ru.spbstu.sysk.samples.triggers

import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.channels.to
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.samples.NAND

class RSC(name: String, parent: SysModule) : SysModule(name, parent) {

    val R = bitInput("r")
    val S = bitInput("s")
    val C = bitInput("c")
    val Q = bitOutput("q")
    val nQ = bitOutput("nq")

    private val u1 = NAND("u1", this)
    private val d1 = NAND("d1", this)
    private val nrs = nRS("nrs", this)

    private val s1 = bitSignal("s1")
    private val s2 = bitSignal("s2")

    init {
        bind(u1.x1 to S, d1.x1 to C)
        bind(u1.x2 to C, d1.x2 to R)
        bind(u1.y to s1, d1.y to s2)
        bind(nrs.nS to s1, nrs.nR to s2)
        bind(nrs.Q to Q, nrs.nQ to nQ)
    }
}

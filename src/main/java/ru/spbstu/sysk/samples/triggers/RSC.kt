package ru.spbstu.sysk.samples.triggers

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
        u1.x1 bind S
        u1.x2 bind C
        u1.y bind s1

        d1.x1 bind C
        d1.x2 bind R
        d1.y bind s2

        nrs.nS bind s1
        nrs.nR bind s2
        nrs.Q bind Q
        nrs.nQ bind nQ
    }
}

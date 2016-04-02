package ru.spbstu.sysk.samples.triggers

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.samples.NAND

class RSC(name: String, parent: SysModule) : SysModule(name, parent) {

    val R = bitInput("r")
    val S = bitInput("s")
    val C = bitInput("c")
    val Q = output<SysBit>("q")
    val nQ = output<SysBit>("nq")

    private val u1 = NAND("u1", this)
    private val d1 = NAND("d1", this)
    private val nrs = nRS("nrs", this)

    private val r = bitSignal("r")
    private val s = bitSignal("s")
    private val c = bitSignal("c")
    private val q = bitSignal("q")
    private val nq = bitSignal("nq")
    private val s1 = bitSignal("s1")
    private val s2 = bitSignal("s2")

    init {
        u1.x1 bind s
        u1.x2 bind c
        u1.y bind s1

        d1.x1 bind c
        d1.x2 bind r
        d1.y bind s2

        nrs.nS bind s1
        nrs.nR bind s2
        nrs.Q bind q
        nrs.nQ bind nq

        function(R.defaultEvent or S.defaultEvent or C.defaultEvent) {
            r(R())
            s(S())
            c(C())
        }
        function(q.defaultEvent or nq.defaultEvent) {
            Q(q())
            nQ(nq())
        }
    }
}

package ru.spbstu.sysk.samples.triggers

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.samples.NOR

class RS(name: String, parent: SysModule) : SysModule(name, parent) {

    val R = bitInput("r")
    val S = bitInput("s")
    val Q = output<SysBit>("q")
    val nQ = output<SysBit>("nq")

    private val u1 = NOR("u1", this)
    private val d1 = NOR("d1", this)

    private val r = bitSignal("r")
    private val s = bitSignal("s")
    private val q = bitSignal("q")
    private val nq = bitSignal("nq")

    init {
        u1.x1 bind r
        u1.x2 bind nq
        u1.y bind q

        d1.x1 bind q
        d1.x2 bind s
        d1.y bind nq

        function(R.defaultEvent or S.defaultEvent) {
            r(R())
            s(S())
        }
        function(q.defaultEvent or nq.defaultEvent) {
            Q(q())
            nQ(nq())
        }
    }
}

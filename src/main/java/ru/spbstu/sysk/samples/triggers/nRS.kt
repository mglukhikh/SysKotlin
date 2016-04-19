package ru.spbstu.sysk.samples.triggers

import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.channels.to
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.samples.NAND

class nRS(name: String, parent: SysModule) : SysModule(name, parent) {

    val nR = bitInput("nr")
    val nS = bitInput("ns")
    val Q =  bitOutput("q")
    val nQ = bitOutput("nq")

    private val u1 = NAND("u1", this)
    private val d1 = NAND("d1", this)

    private val q = bitSignal("q")
    private val nq = bitSignal("nq")

    init {
        bind(u1.x2 to nq, d1.x1 to q)
        bind(u1.y to q, d1.y to nq)
        bind(u1.x1 to nS, d1.x2 to nR)

        function(q.defaultEvent or nq.defaultEvent) {
            Q(q())
            nQ(nq())
        }
    }
}
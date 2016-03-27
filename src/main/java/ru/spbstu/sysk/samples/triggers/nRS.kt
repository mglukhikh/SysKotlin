package ru.spbstu.sysk.samples.triggers

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.generics.NAND

class nRS(name: String, parent: SysModule) : SysModule(name, parent) {

    val nR = bitInput("nr")
    val nS = bitInput("ns")
    val Q = output<SysBit>("q")
    val nQ = output<SysBit>("nq")

    private val u1 = NAND("u1", this)
    private val d1 = NAND("d1", this)

    private val nr = bitSignal("nr")
    private val ns = bitSignal("ns")
    private val q = bitSignal("q")
    private val nq = bitSignal("nq")

    init {
        u1.x1 bind ns
        u1.x2 bind nq
        u1.y bind q

        d1.x1 bind q
        d1.x2 bind nr
        d1.y bind nq

        function(nR.defaultEvent or nS.defaultEvent) {
            nr(nR())
            ns(nS())
        }
        function(q.defaultEvent or nq.defaultEvent) {
            Q(q())
            nQ(nq())
        }
    }
}
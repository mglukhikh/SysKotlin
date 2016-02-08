package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysInteger
import ru.spbstu.sysk.data.undefined

class Stub {
    internal class Involuator constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {

        val clk = bitInput("clk", null)
        val exp = input("exp", null, SysInteger(32, 1))
        val pow = input("pos", null, SysInteger(32, 1))
        val result = output<SysInteger>("result", null)

        private val involution: (SysWait) -> Unit = {
            println("involution")
            result.value = SysInteger(32, Math.pow(exp.value.value.toDouble(), pow.value.value.toDouble()).toLong())
        }

        private val change: (SysWait) -> SysWait = {
            println("change ${clk.value}")
            clk.defaultEvent
        }

        private val pos: (SysWait) -> SysWait = {
            println("pos ${clk.value}")
            clk.defaultEvent
        }

        private val neg: (SysWait) -> SysWait = {
            println("neg ${clk.value}")
            clk.defaultEvent
        }

        init {
            function(change, clk.defaultEvent, false)
            function(pos, clk.posEdgeEvent, false)
            function(neg, clk.negEdgeEvent, false)
            function(clk, true, false, involution)
        }
    }

    internal class Tester constructor(
            private var qCycles: Int, name: String, parent: SysModule)
    : SysModule(name, parent) {

        val clk = output<SysBit>("clk", null)
        val exp = output<SysInteger>("exp", null)
        val result = input<SysInteger>("result", null)

        val nextCycleEvent = event("nextCycle")
        val nextStepInitializationEvent = event("nextStepInitialization")
        val checkEvent = event("check")

        private var A: SysInteger = undefined()

        private val initStep1: (SysWait) -> SysWait = {
            println("initStep1")
            if (qCycles == 0) scheduler.stop()
            clk.value = SysBit.ZERO
            nextStepInitializationEvent.happens()
            nextCycleEvent
        }

        private val initStep2: (SysWait) -> SysWait = {
            println("initStep2")
            A = SysInteger(32, qCycles)
            exp.value = A
            clk.value = SysBit.ONE
            --qCycles
            checkEvent.happens(SysWait.Time(1))
            nextStepInitializationEvent
        }

        private val check: (SysWait) -> SysWait = {
            println("check")
//            assert(result.value.equals(SysInteger(32, Math.pow(A.value.toDouble(), 2.0).toLong()))) { "Lel" }
            nextCycleEvent.happens()
            checkEvent
        }

        init {
            function(initStep1, nextCycleEvent, true)
            function(initStep2, nextStepInitializationEvent, false)
            function(check, checkEvent, false)
        }
    }

    internal object TopModule : SysTopModule("Connectors", SysScheduler()) {
        init {
            val involuator = Involuator("involuator", this)
            val tester = Tester(3, "Tester", this)
            val clkWire = signal<SysBit>("clk")
            val expWire = signal<SysInteger>("exp")
            val resultWire = signal<SysInteger>("result")
            involuator.clk.bind(clkWire)
            tester.clk.bind(clkWire)
            involuator.exp.bind(expWire)
            tester.exp.bind(expWire)
            involuator.result.bind(resultWire)
            tester.result.bind(resultWire)
            involuator.pow.bind(signalStub("pow", SysInteger(32, 2)))
            //bind(involuator.clk to clkWire, tester.clk to clkWire, involuator.exp to expWire, tester.exp to expWire,
            //        involuator.result to resultWire, tester.result to resultWire,
            //        involuator.pow to signalStub("pow", SysInteger(32, 2)))
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}
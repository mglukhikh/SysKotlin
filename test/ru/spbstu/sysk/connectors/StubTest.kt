package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysInteger

class StubTest {
    internal class Involuator constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {

        val clk = bitInput("clk", null)
        val exp = input("exp", SysInteger(32, 1), null)
        val pow = input("pos", SysInteger(32, 1), null)
        val result = output<SysInteger>("result", null)

        private val involution: () -> Unit = {
            println("involution")
            result.value = SysInteger(32, Math.pow(exp.value.value.toDouble(), pow.value.value.toDouble()).toLong())
        }

        init {
            stateFunction(clk, true) {
                infinite(involution)
            }
        }
    }

    internal class Tester constructor(
            private var qCycles: Int, name: String, parent: SysModule)
    : SysModule(name, parent) {

        val clk = bitInput("clk", null)
        val exp = output<SysInteger>("exp", null)
        val result = input<SysInteger>("result", null)

        private var A: SysInteger = SysInteger(32, 0)
        private var one: SysInteger = SysInteger(32, 1)

        private val init: () -> Unit = {
            println("init")
            if (qCycles.toLong() == A.value) scheduler.stop()
            A = A.plus(one)
            exp.value = A
        }

        private val check: () -> Unit = {
            println("check")
            assert(result.value.equals(SysInteger(32, Math.pow(A.value.toDouble(), 2.0).toLong()))) { "Lel" }
        }

        init {
            stateFunction(clk, true) {
                state(init)
                state({})
                state(check)
                goTo(0)
            }
        }
    }

    internal object TopModule : SysTopModule("Connectors", SysScheduler()) {
        init {
            val involuator = Involuator("involuator", this)
            val tester = Tester(3, "Tester", this)
            val clk = clockedSignal("clk", SysWait.Time(1), SysBit.ZERO)
            val expWire = signal<SysInteger>("exp")
            val resultWire = signal<SysInteger>("result")
            involuator.clk.bind(clk)
            tester.clk.bind(clk)
            involuator.exp.bind(expWire)
            tester.exp.bind(expWire)
            involuator.result.bind(resultWire)
            tester.result.bind(resultWire)
            involuator.pow.bind(signalStub("pow", SysInteger(32, 2)))
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}
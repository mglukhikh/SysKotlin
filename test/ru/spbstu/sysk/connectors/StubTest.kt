package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysInteger

private const val EXPONENT = 3

class StubTest {

    internal class Involuator constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {

        val clk = bitInput("clk", null)
        val exp = input("exp", SysInteger(32, 1), null)
        val pow = input("pos", SysInteger(32, 1), null)
        val result = output<SysInteger>("result", null)

        private val involution: () -> Unit = {
            result.value = SysInteger(32, Math.pow(exp.value.value.toDouble(), pow.value.value.toDouble()).toLong())
        }

        init {
            stateFunction(clk, true) {
                infiniteBlock {
                    state(involution)
                    sleep(3)
                }
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

        private val init: () -> Unit = {
            if (qCycles.toLong() == A.value) scheduler.stop()
            A++
            exp.value = A
        }

        private val check: () -> Unit = {
            assert(result.value.equals(SysInteger(32, Math.pow(A.value.toDouble(), EXPONENT.toDouble()).toLong())))
        }

        init {
            stateFunction(clk, true) {
                infiniteBlock {
                    state(init)
                    sleep(4)
                    state(check)
                }
            }
        }
    }

    internal object TopModule : SysTopModule("Connectors", SysScheduler()) {
        init {
            val involuator = Involuator("involuator", this)
            val tester = Tester(10, "Tester", this)
            val clk = clockedSignal("clk", SysWait.Time(1), SysBit.ZERO)
            val expWire = signal<SysInteger>("exp")
            val resultWire = signal<SysInteger>("result")
            involuator.clk.bind(clk)
            tester.clk.bind(clk)
            involuator.exp.bind(expWire)
            tester.exp.bind(expWire)
            involuator.result.bind(resultWire)
            tester.result.bind(resultWire)
            involuator.pow.bind(signalStub("pow", SysInteger(32, EXPONENT)))
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}
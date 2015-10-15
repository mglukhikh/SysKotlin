package modules

import org.junit.Test
import sysk.SysFunction
import sysk.SysTopModule
import sysk.SysWireState
import sysk.bind

class SysSimpleAutomataTest {
    class SysLatchTester : SysTopModule() {

        val dut = LatchTriggerMoore("not", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        init {
            bind(dut.x to x)
            bind(dut.y to y)
        }

        val counter = 0

        private val f: SysFunction = function({
            when (counter) {
                0 -> {
                    assert(y.x)
                    x.value = SysWireState.ZERO
                }
                1 -> {
                    assert(y.x)
                    x.value = SysWireState.ONE
                }
                2 -> {
                    assert(y.zero)
                    x.value = SysWireState.ZERO
                }
                3 -> {
                    assert(y.one)
                    x.value = SysWireState.ONE
                }
                4 -> {
                    assert(y.zero)
                }
                5 -> {
                    assert(y.one)
                    scheduler.stop()
                }
            }
            f.wait()
        }, sensitivities = y.defaultEvent, initialize = false)
    }

    @Test
    fun latchTest() {
        SysLatchTester().start()
    }

    class SysCountTester : SysTopModule() {

        val dut = CountTriggerMoore("not", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        init {
            bind(dut.x to x)
            bind(dut.y to y)
        }

        val counter = 0

        private val f: SysFunction = function({
            when (counter) {
                0 -> {
                    assert(y.zero)
                    x.value = SysWireState.ZERO
                }
                1 -> {
                    assert(y.zero)
                    x.value = SysWireState.ONE
                }
                2 -> {
                    assert(y.zero)
                }
                3 -> {
                    assert(y.one)
                    x.value = SysWireState.ZERO
                }
                4 -> {
                    assert(y.zero)
                }
                5 -> {
                    assert(y.zero)
                    scheduler.stop()
                }
            }
            f.wait()
        }, sensitivities = y.defaultEvent, initialize = false)
    }

    @Test
    fun countTest() {
        SysCountTester().start()
    }
}
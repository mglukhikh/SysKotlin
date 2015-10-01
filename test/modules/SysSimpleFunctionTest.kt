package modules

import org.junit.Test
import sysk.SysFunction
import sysk.SysTopModule
import sysk.SysWireState
import sysk.bind

class SysSimpleFunctionTest {

    class SysNotTester: SysTopModule() {

        val dut = SysNotModule("not", this)

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
                    assert(y.one)
                    x.value = SysWireState.ONE
                }
                2 -> {
                    assert(y.zero)
                    scheduler.stop()
                }
            }
            f.wait()
        }, sensitivities = y.defaultEvent, initialize = false)
    }

    @Test
    fun notTest() {
        SysNotTester().start()
    }

    class SysOrTester: SysTopModule() {
        val dut = SysOrModule("or", this)

        val x1 = wireSignal("x1")
        val x2 = wireSignal("x2")
        val y = wireSignal("y")

        init {
            bind(dut.x1 to x1, dut.x2 to x2)
            bind(dut.y to y)
        }

        val counter = 0

        private val f: SysFunction = function({
            when (counter) {
                0 -> {
                    assert(y.x)
                    x1.value = SysWireState.ZERO
                    x2.value = SysWireState.ZERO
                }
                1 -> {
                    assert(y.zero)
                    x1.value = SysWireState.ONE
                }
                2 -> {
                    assert(y.one)
                    x2.value = SysWireState.ONE
                }
                3 -> {
                    assert(y.one)
                    x1.value = SysWireState.ZERO
                }
                4 -> {
                    assert(y.one)
                    x2.value = SysWireState.ZERO
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
    fun orTest() {
        SysOrTester().start()
    }
}
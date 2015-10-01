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
}
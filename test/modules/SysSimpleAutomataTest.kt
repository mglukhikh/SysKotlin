package modules

import org.junit.Test
import sysk.*

class SysSimpleAutomataTest {

    class SysLatchStagedTester : SysTopModule() {
        val dut = LatchTriggerMoore("latch", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.x to x, dut.clk to clk)
            bind(dut.y to y)
        }

        init {
            stagedFunction(clk.posEdgeEvent) {
                stage {
                    assert(y.x)
                    x.value = SysWireState.ZERO
                }
                stage {
                    assert(y.x)
                    x.value = SysWireState.ONE
                }
                stage {
                    assert(y.zero)
                    x.value = SysWireState.ZERO
                }
                stage {
                    assert(y.one)
                    x.value = SysWireState.ONE
                }
                stage {
                    assert(y.zero)
                }
                stage {
                    assert(y.one)
                    scheduler.stop()
                }
            }
        }
    }

    class SysLatchTester : SysTopModule() {

        val dut = LatchTriggerMoore("latch", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.x to x, dut.clk to clk)
            bind(dut.y to y)
        }

        var counter = 0

        init {
            function(sensitivities = clk.posEdgeEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x) { "Expected X at 0 but was ${y.value}"}
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
                counter++
            }
        }
    }

    @Test
    fun latchStagedTest() {
        SysLatchStagedTester().start()
    }

    @Test
    fun latchTest() {
        SysLatchTester().start()
    }

    class SysCountTester : SysTopModule() {

        val dut = CountTriggerMoore("count", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.x to x, dut.clk to clk)
            bind(dut.y to y)
        }

        init {
            stagedFunction(clk.posEdgeEvent) {
                stage {
                    // TODO [mglukhikh]: recheck this place, looks like it should be zero here
                    assert(y.x)
                    x.value = SysWireState.ZERO
                }
                stage {
                    assert(y.zero) { "Expected ZERO at stage 1 but was ${y.value}"}
                    x.value = SysWireState.ONE
                }
                stage {
                    assert(y.zero)
                }
                stage {
                    assert(y.one)
                    x.value = SysWireState.ZERO
                }
                stage {
                    assert(y.zero)
                }
                stage {
                    assert(y.zero)
                    scheduler.stop()
                }
            }
        }
    }

    @Test
    fun countTest() {
        SysCountTester().start()
    }
}
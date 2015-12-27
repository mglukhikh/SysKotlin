package sysk.generics

import org.junit.Test
import sysk.core.SysTopModule
import sysk.core.SysWait
import sysk.core.TimeUnit
import sysk.core.time
import sysk.data.SysBit
import sysk.data.bind

class SysSimpleAutomataTest {

    class SysLatchStagedTester : SysTopModule() {
        val dut = LatchTriggerMoore("latch", this)

        val x = bitSignal("x")
        val y = bitSignal("y")

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.x to x, dut.clk to clk)
            bind(dut.y to y)
        }

        fun assertTime(time: SysWait.Time) {
            assert(currentTime == time) { "Expected $time but was $currentTime" }
        }

        init {
            stagedFunction(clk) {
                stage {
                    assert(y.x)
                    x.value = SysBit.ZERO
                    assertTime(time(10, TimeUnit.NS))
                }
                complexStage {
                    stage {
                        assert(y.x)
                        x.value = SysBit.ONE
                        assertTime(time(30, TimeUnit.NS))
                    }
                    complexStage {
                        stage {
                            assert(y.zero)
                            x.value = SysBit.ZERO
                            assertTime(time(50, TimeUnit.NS))
                        }
                        stage {
                            assert(y.one)
                            x.value = SysBit.ONE
                            assertTime(time(70, TimeUnit.NS))
                        }
                    }
                }
                stage {
                    assert(y.zero)
                    assert(currentTime == time(90, TimeUnit.NS)) { "Expected 90 ns but was $currentTime"}
                    assertTime(time(90, TimeUnit.NS))
                }
                stage {
                    assert(y.one)
                    assertTime(time(110, TimeUnit.NS))
                    scheduler.stop()
                }
            }
        }
    }

    class SysLatchTester : SysTopModule() {

        val dut = LatchTriggerMoore("latch", this)

        val x = bitSignal("x")
        val y = bitSignal("y")

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
                        x.value = SysBit.ZERO
                    }
                    1 -> {
                        assert(y.x)
                        x.value = SysBit.ONE
                    }
                    2 -> {
                        assert(y.zero)
                        x.value = SysBit.ZERO
                    }
                    3 -> {
                        assert(y.one)
                        x.value = SysBit.ONE
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

        val x = bitSignal("x")
        val y = bitSignal("y")

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.x to x, dut.clk to clk)
            bind(dut.y to y)
        }

        init {
            stagedFunction(clk) {
                stage {
                    assert(y.zero)
                    x.value = SysBit.ZERO
                }
                stage {
                    assert(y.zero) { "Expected ZERO at stage 1 but was ${y.value}"}
                    x.value = SysBit.ONE
                }
                stage {
                    assert(y.zero)
                }
                stage {
                    assert(y.one)
                    x.value = SysBit.ZERO
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
package ru.spbstu.sysk.generics

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.core.TimeUnit
import ru.spbstu.sysk.core.time
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.bind

class SysSimpleAutomataTest {

    class SysLatchStagedTester : SysTopModule() {
        val dut = LatchTriggerMoore("latch", this)

        var x by signalWriter("x", dut.x)
        val y by signalReader("y", dut.y)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.clk to clk)
        }

        fun assertTime(time: SysWait.Time) {
            assert(currentTime == time) { "Expected $time but was $currentTime" }
        }

        init {
            stateFunction(clk) {
                state {
                    assert(y.x)
                    x = ZERO
                    assertTime(time(10, TimeUnit.NS))
                }
                state {
                    assert(y.x)
                    x = ONE
                    assertTime(time(30, TimeUnit.NS))
                }
                state {
                    assert(y.zero)
                    x = ZERO
                    assertTime(time(50, TimeUnit.NS))
                }
                state {
                    assert(y.one)
                    x = ONE
                    assertTime(time(70, TimeUnit.NS))
                }
                state {
                    assert(y.zero)
                    assert(currentTime == time(90, TimeUnit.NS)) { "Expected 90 ns but was $currentTime" }
                    assertTime(time(90, TimeUnit.NS))
                }
                state {
                    assert(y.one)
                    assertTime(time(110, TimeUnit.NS))
                    scheduler.stop()
                }
            }
        }
    }

    class SysLatchTester : SysTopModule() {

        val dut = LatchTriggerMoore("latch", this)

        var x by signalWriter("x", dut.x)
        val y by signalReader("y", dut.y)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.clk to clk)
        }

        var counter = 0

        init {
            function(sensitivities = clk.posEdgeEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x) { "Expected X at 0 but was $y" }
                        x = ZERO
                    }
                    1 -> {
                        assert(y.x)
                        x = ONE
                    }
                    2 -> {
                        assert(y.zero)
                        x = ZERO
                    }
                    3 -> {
                        assert(y.one)
                        x = ONE
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

        var x by signalWriter("x", dut.x)
        val y by signalReader("y", dut.y)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))

        init {
            bind(dut.clk to clk)
        }

        init {
            stateFunction(clk) {
                state {
                    assert(y.zero)
                    x = ZERO
                }
                state {
                    assert(y.zero) { "Expected ZERO at stage 1 but was $y" }
                    x = ONE
                }
                state {
                    assert(y.zero)
                }
                state {
                    assert(y.one)
                    x = ZERO
                }
                state {
                    assert(y.zero)
                }
                state {
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
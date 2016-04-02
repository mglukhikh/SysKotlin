package ru.spbstu.sysk.generics

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.channels.SysBitInput
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.samples.AND
import ru.spbstu.sysk.samples.NOT
import ru.spbstu.sysk.samples.OR

class SysSimpleFunctionTest {

    class SysNotTester: SysTopModule() {

        val dut = NOT("not", this)

        var x by bitSignalWriter("x", dut.x as SysBitInput)
        val y = bitSignal("y")

        init {
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x)
                        x = ZERO
                    }
                    1 -> {
                        assert(y.one)
                        x = ONE
                    }
                    2 -> {
                        assert(y.zero)
                        scheduler.stop()
                    }
                }
            }
        }
    }

    @Test
    fun notTest() {
        SysNotTester().start()
    }

    class SysOrTester: SysTopModule() {
        val dut = OR("or", this)

        var x1 by bitSignalWriter("x1", dut.x1 as SysBitInput)
        var x2 by bitSignalWriter("x2", dut.x2 as SysBitInput)
        val y = bitSignal("y")

        init {
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x)
                        x1 = ZERO
                        x2 = ZERO
                    }
                    1 -> {
                        assert(y.zero)
                        x1 = ONE
                    }
                    2 -> {
                        assert(y.one)
                        x2 = ONE
                    }
                    3 -> {
                        assert(y.one)
                        x1 = ZERO
                    }
                    4 -> {
                        assert(y.one)
                        x2 = ZERO
                    }
                    5 -> {
                        assert(y.zero)
                        scheduler.stop()
                    }
                }
            }
        }
    }

    @Test
    fun orTest() {
        SysOrTester().start()
    }

    class SysAndTester: SysTopModule() {
        val dut = AND("and", this)

        var x1 by bitSignalWriter("x1", dut.x1 as SysBitInput)
        var x2 by bitSignalWriter("x2", dut.x2 as SysBitInput)
        val y = bitSignal("y")

        init {
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x)
                        x1 = ZERO
                        x2 = ZERO
                    }
                    1 -> {
                        assert(y.zero)
                        x1 = ONE
                    }
                    2 -> {
                        assert(y.zero)
                        x2 = ONE
                    }
                    3 -> {
                        assert(y.one)
                        x1 = ZERO
                    }
                    4 -> {
                        assert(y.zero)
                        x2 = ZERO
                    }
                    5 -> {
                        assert(y.zero)
                        scheduler.stop()
                    }
                }
            }
        }
    }

    @Test
    fun andTest() {
        SysAndTester().start()
    }
}
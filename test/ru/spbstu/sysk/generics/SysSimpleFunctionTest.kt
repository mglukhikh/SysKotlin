package ru.spbstu.sysk.generics

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.bind

class SysSimpleFunctionTest {

    class SysNotTester: SysTopModule() {

        val dut = SysNotModule("not", this)

        val x = bitSignal("x")
        val y = bitSignal("y")

        init {
            bind(dut.x to x)
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x)
                        x.value = ZERO
                    }
                    1 -> {
                        assert(y.one)
                        x.value = ONE
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
        val dut = SysOrModule("or", this)

        val x1 = bitSignal("x1")
        val x2 = bitSignal("x2")
        val y = bitSignal("y")

        init {
            bind(dut.x1 to x1, dut.x2 to x2)
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x)
                        x1.value = ZERO
                        x2.value = ZERO
                    }
                    1 -> {
                        assert(y.zero)
                        x1.value = ONE
                    }
                    2 -> {
                        assert(y.one)
                        x2.value = ONE
                    }
                    3 -> {
                        assert(y.one)
                        x1.value = ZERO
                    }
                    4 -> {
                        assert(y.one)
                        x2.value = ZERO
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
        val dut = SysAndModule("and", this)

        val x1 = bitSignal("x1")
        val x2 = bitSignal("x2")
        val y = bitSignal("y")

        init {
            bind(dut.x1 to x1, dut.x2 to x2)
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x)
                        x1.value = ZERO
                        x2.value = ZERO
                    }
                    1 -> {
                        assert(y.zero)
                        x1.value = ONE
                    }
                    2 -> {
                        assert(y.zero)
                        x2.value = ONE
                    }
                    3 -> {
                        assert(y.one)
                        x1.value = ZERO
                    }
                    4 -> {
                        assert(y.zero)
                        x2.value = ZERO
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
package ru.spbstu.sysk.samples.gates

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.samples.NAND

class NANDTest : SysTopModule() {

    val clk = clock("clock", 1(FS))

    val nand = NAND("nand", this)

    val x1 = bitSignal("x1", X)
    val x2 = bitSignal("x2", X)
    val y = bitSignal("y", X)

    init {
        nand.x1 bind x1
        nand.x2 bind x2
        nand.y bind y

        stateFunction(clk) {
            state {
                assert(x1.x && x2.x && y.x) {"Fail: NANDTest.state.1"}
                x1(ONE)
            }
            state {
                assert(x1.one && x2.x && y.x) {"Fail: NANDTest.state.2"}
                x2(ONE)
            }
            state {
                assert(x1.one && x2.one && y.zero) {"Fail: NANDTest.state.3"}
                x1(ZERO)
            }
            state {
                assert(x1.zero && x2.one && y.one) {"Fail: NANDTest.state.4"}
                x2(ZERO)
            }
            state {
                assert(x1.zero && x2.zero && y.one) {"Fail: NANDTest.state.5"}
                x1(X)
            }
            state {
                assert(x1.x && x2.zero && y.one) {"Fail: NANDTest.state.6"}
                x2(X)
            }
            state {
                assert(x1.x && x2.x && y.x) {"Fail: NANDTest.state.7"}
                x2(ONE)
            }
            state {
                assert(x1.x && x2.one && y.x) {"Fail: NANDTest.state.8"}
                x1(ZERO)
                x2(X)
            }
            state {
                assert(x1.zero && x2.x && y.one) {"Fail: ORTest.state.9"}
            }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start(1(NS))
}

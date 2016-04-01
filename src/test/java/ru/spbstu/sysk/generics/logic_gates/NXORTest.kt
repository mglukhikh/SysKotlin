package ru.spbstu.sysk.generics.logic_gates

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.generics.NXOR

class NXORTest : SysTopModule() {

    val clk = clock("clock", 1(FS))

    val nxor = NXOR("nxor", this)

    val x1 = bitSignal("x1", X)
    val x2 = bitSignal("x2", X)
    val y = bitSignal("y", X)

    init {
        nxor.x1 bind x1
        nxor.x2 bind x2
        nxor.y bind y

        stateFunction(clk) {
            state {
                assert(x1().x && x2().x && y().x) {"Fail: NXORTest.state.1"}
                x1(ONE)
            }
            state {
                assert(x1().one && x2().x && y().x) {"Fail: NXORTest.state.2"}
                x2(ONE)
            }
            state {
                assert(x1().one && x2().one && y().one) {"Fail: NXORTest.state.3"}
                x1(ZERO)
            }
            state {
                assert(x1().zero && x2().one && y().zero) {"Fail: NXORTest.state.4"}
                x2(ZERO)
            }
            state {
                assert(x1().zero && x2().zero && y().one) {"Fail: NXORTest.state.5"}
                x1(X)
            }
            state {
                assert(x1().x && x2().zero && y().x) {"Fail: NXORTest.state.6"}
                x2(X)
            }
            state {
                assert(x1().x && x2().x && y().x) {"Fail: NXORTest.state.7"}
                x2(ONE)
            }
            state {
                assert(x1().x && x2().one && y().x) {"Fail: NXORTest.state.8"}
                x1(ZERO)
                x2(X)
            }
            state {
                assert(x1().zero && x2().x && y().x) {"Fail: NXORTest.state.9"}
            }
            state {
                scheduler.stop()
            }
        }
    }

    @Test
    fun show() = start(1(NS))
}

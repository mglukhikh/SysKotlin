package ru.spbstu.sysk.generics.logic_gates

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.generics.NOT

class NOTTest : SysTopModule() {

    val clk = clockedSignal("clock", 1(FS))

    val not = NOT("not", this)

    val x = bitSignal("x", X)
    val y = bitSignal("y", X)

    init {
        not.x bind x
        not.y bind y

        stateFunction(clk) {
            state {
                assert(x().x && y().x) {"Fail: NOTTest.state.1"}
                x(ONE)
            }
            state {
                assert(x().one && y().zero) {"Fail: NOTTest.state.2"}
                x(ZERO)
            }
            state {
                assert(x().zero && y().one) {"Fail: NOTTest.state.3"}
                x(X)
            }
            state {
                assert(x().x && y().x) {"Fail: NOTTest.state.4"}
            }
            state {
                scheduler.stop()
            }
        }
    }

    @Test
    fun show() = start(1(NS))
}

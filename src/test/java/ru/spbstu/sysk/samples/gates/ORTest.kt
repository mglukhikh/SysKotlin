package ru.spbstu.sysk.samples.gates

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.samples.OR

class ORTest : SysTopModule() {

    val clk = clock("clock", 1(FS))

    val or = OR("or", this)

    val x1 = bitSignal("x1", X)
    val x2 = bitSignal("x2", X)
    val y = bitSignal("y", X)

    init {
        or.x1 bind x1
        or.x2 bind x2
        or.y bind y

        stateFunction(clk) {
            state {
                assert(x1.x && x2.x && y.x) { "Fail: ORTest.state.1" }
                x1(ONE)
            }
            state {
                assert(x1.one && x2.x && y.one) { "Fail: ORTest.state.2" }
                x2(ONE)
            }
            state {
                assert(x1.one && x2.one && y.one) { "Fail: ORTest.state.3" }
                x1(ZERO)
            }
            state {
                assert(x1.zero && x2.one && y.one) { "Fail: ORTest.state.4" }
                x2(ZERO)
            }
            state {
                assert(x1.zero && x2.zero && y.zero) { "Fail: ORTest.state.5" }
                x1(X)
            }
            state {
                assert(x1.x && x2.zero && y.x) { "Fail: ORTest.state.6" }
                x2(X)
            }
            state {
                assert(x1.x && x2.x && y.x) { "Fail: ORTest.state.7" }
                x2(ONE)
            }
            state {
                assert(x1.x && x2.one && y.one) { "Fail: ORTest.state.8" }
                x1(ZERO)
                x2(X)
            }
            state {
                assert(x1.zero && x2.x && y.x) { "Fail: ORTest.state.9" }
            }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start(1(NS))
}

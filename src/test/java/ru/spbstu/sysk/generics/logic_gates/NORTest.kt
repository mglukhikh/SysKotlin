package ru.spbstu.sysk.generics.logic_gates

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.samples.NOR

class NORTest : SysTopModule() {

    val clk = clock("clock", 1(FS))

    val nor = NOR("nor", this)

    val x1 = bitSignal("x1", X)
    val x2 = bitSignal("x2", X)
    val y = bitSignal("y", X)

    init {
        nor.x1 bind x1
        nor.x2 bind x2
        nor.y bind y

        stateFunction(clk) {
            state {
                assert(x1().x && x2().x && y().x) {"Fail: NORTest.state.1"}
                x1(ONE)
            }
            state {
                assert(x1().one && x2().x && y().zero) {"Fail: NORTest.state.2"}
                x2(ONE)
            }
            state {
                assert(x1().one && x2().one && y().zero) {"Fail: NORTest.state.3"}
                x1(ZERO)
            }
            state {
                assert(x1().zero && x2().one && y().zero) {"Fail: NORTest.state.4"}
                x2(ZERO)
            }
            state {
                assert(x1().zero && x2().zero && y().one) {"Fail: NORTest.state.5"}
                x1(X)
            }
            state {
                assert(x1().x && x2().zero && y().x) {"Fail: NORTest.state.6"}
                x2(X)
            }
            state {
                assert(x1().x && x2().x && y().x) {"Fail: NORTest.state.7"}
                x2(ONE)
            }
            state {
                assert(x1().x && x2().one && y().zero) {"Fail: NORTest.state.8"}
                x1(ZERO)
                x2(X)
            }
            state {
                assert(x1().zero && x2().x && y().x) {"Fail: NORTest.state.9"}
            }
            state {
                scheduler.stop()
            }
        }
    }

    @Test
    fun show() = start(1(NS))
}

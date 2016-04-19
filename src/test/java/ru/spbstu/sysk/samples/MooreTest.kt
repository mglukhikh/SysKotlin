package ru.spbstu.sysk.samples

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.samples.Input.*

class MooreTest : SysTopModule() {

    val abtomat = Moore(this)

    val inp = signal<Input>("inp")
    val out = bitSignal("out")
    val clk = clock("inp", 20(FS))

    init {
        abtomat.x bind inp
        abtomat.y bind out
        abtomat.clk bind clk

        stateFunction(clk, false) {
            state {
                println("${inp()} ${out()}")
                inp(c)
            }
            writeln { "${inp()} ${out()}" }
            writeln { "${inp()} ${out()}" }
            writeln { "${inp()} ${out()}" }
            state {
                println("${inp()} ${out()}")
                inp(b)
            }
            writeln { "${inp()} ${out()}" }
            writeln { "${inp()} ${out()}" }
            state {
                println("${inp()} ${out()}")
                inp(a)
            }
            writeln { "${inp()} ${out()}" }
            writeln { "${inp()} ${out()}" }
            stop(scheduler)
        }
    }

    @Test
    fun show() = start(1(S))
}

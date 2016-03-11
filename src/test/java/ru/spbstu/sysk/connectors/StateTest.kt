package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysInteger
import ru.spbstu.sysk.data.bind

class StateTest {

    internal class Test1(name: String, parent: SysModule) : SysModule(name, parent) {

        val clk = bitInput("clk")
        val result = fifoOutput<SysInteger>("result")

        fun put(value: SysInteger) {
            result.value = value
            result.push = SysBit.ONE
        }

        init {
            stateFunction(clk, true) {
                var switch = false
                var counter = 0
                label("begin")
                If({ counter++ == 10 }) { jump("end") }
                state { println("start loop") }
                state {
                    switch = !switch
                    println("before IF-Else")
                }
                If ({ switch }) {
                    state { println("If-1") }
                    state { println("If-2") }
                    state { println("If-3") }
                }
                If ({ !switch }) { sleep(3) }
                state { println("after IF-Else") }
                state { println("end loop") }
                jump("begin")
                label("end")
                state { println("end") }
            }
        }
    }

    internal class Tester(name: String, parent: SysModule) : SysModule(name, parent) {

        val clk = bitInput("clk")
        val result = Array(1, { fifoInput<SysInteger>("result") })

        fun check(value: SysInteger) {
            for (i in 0..result.lastIndex) {
                if (!result[i].empty) {
                    assert(result[i].value == value)
                    result[i].pop = SysBit.ONE
                }
            }
        }

        init {
            stateFunction(clk, true) {
                forEach(0..10) {
                    var switch = false
                    state { println("start loop") }
                    state {
                        switch = !switch
                        println("before IF-Else")
                    }
                    If ({ switch }) {
                        state { println("If-1") }
                        state { println("If-2") }
                        state { println("If-3") }
                    }
                    Else {
                        state { println("Else-1") }
                        state { println("Else-2") }
                        state { println("Else-3") }
                    }
                    state { println("after IF-Else") }
                    state { println("end loop") }
                }
                state() {
                    println("end")
                    scheduler.stop()
                }
            }
        }
    }

    internal object TopModule : SysTopModule() {
        init {
            val clk = clockedSignal("clk", SysWait.Time(1))
            val fifo1 = fifo<SysInteger>(1, "fifo")
            val tester = Tester("tester", this)
            val test1 = Test1("test1", this)
            bind(tester.clk to clk, test1.clk to clk)
            bind(test1.result to fifo1)
            bind(tester.result[0] to fifo1)
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}

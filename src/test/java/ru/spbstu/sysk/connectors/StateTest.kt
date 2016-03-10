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
                label("start")
                state { put(SysInteger.valueOf(1)) }
                state {
                    switch = !switch
                    put(SysInteger.valueOf(2))
                }
                If ({ switch }) {
                    state { put(SysInteger.valueOf(3)) }
                    state { put(SysInteger.valueOf(4)) }
                    state { put(SysInteger.valueOf(5)) }
                }
                If ({ !switch }) { sleep(3) }
                state { put(SysInteger.valueOf(9)) }
                state { put(SysInteger.valueOf(10)) }
                jump("start")
            }
        }
    }

    internal class Test2(name: String, parent: SysModule) : SysModule(name, parent) {

        val clk = bitInput("clk")
        val result = fifoOutput<SysInteger>("result")

        fun put(value: SysInteger) {
            result.value = value
            result.push = SysBit.ONE
        }

        init {
            stateFunction(clk, true) {
                var switch = false
                label("start")
                state { put(SysInteger.valueOf(1)) }
                state {
                    switch = !switch
                    put(SysInteger.valueOf(2))
                }
                If ({ switch }) {
                    state { put(SysInteger.valueOf(3)) }
                    state { put(SysInteger.valueOf(4)) }
                    state { put(SysInteger.valueOf(5)) }
                }
                Else {
                    state { put(SysInteger.valueOf(6)) }
                    state { put(SysInteger.valueOf(7)) }
                    state { put(SysInteger.valueOf(8)) }
                }
                sleep(1)
                state { put(SysInteger.valueOf(10)) }
                jump("start")
            }
        }
    }

    internal class Test3(name: String, parent: SysModule) : SysModule(name, parent) {

        val clk = bitInput("clk")
        val result = fifoOutput<SysInteger>("result")

        fun put(value: SysInteger) {
            result.value = value
            result.push = SysBit.ONE
        }

        init {
            stateFunction(clk, true) {
                var switch = false
                label("start")
                state { put(SysInteger.valueOf(1)) }
                state {
                    switch = !switch
                    put(SysInteger.valueOf(2))
                }
                If ({ switch }) {
                    state { put(SysInteger.valueOf(3)) }
                    state { put(SysInteger.valueOf(4)) }
                    state { put(SysInteger.valueOf(5)) }
                }
                If ({ !switch }) { sleep(3) }
                sleep(1)
                state { put(SysInteger.valueOf(10)) }
                jump("start")
            }
        }
    }

    internal class Tester(name: String, parent: SysModule) : SysModule(name, parent) {

        val clk = bitInput("clk")
        val result = Array(3, { fifoInput<SysInteger>("result") })

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
                var counter = 0
                infinite {
                    if (counter == 30) scheduler.stop()
                    counter++
                }
            }
            stateFunction(clk, false) {
                var switch = false
                label("start")
                state { check(SysInteger.valueOf(1)) }
                state {
                    switch = !switch
                    check(SysInteger.valueOf(2))
                }
                If ({ switch }) {
                    state { check(SysInteger.valueOf(3)) }
                    state { check(SysInteger.valueOf(4)) }
                    state { check(SysInteger.valueOf(5)) }
                }
                Else {
                    state { check(SysInteger.valueOf(6)) }
                    state { check(SysInteger.valueOf(7)) }
                    state { check(SysInteger.valueOf(8)) }
                }
                state { check(SysInteger.valueOf(9)) }
                state { check(SysInteger.valueOf(10)) }
                jump("start")
            }
        }
    }

    internal object TopModule : SysTopModule() {
        init {
            val clk = clockedSignal("clk", SysWait.Time(1))
            val fifo1 = fifo<SysInteger>(1, "fifo")
            val fifo2 = fifo<SysInteger>(1, "fifo")
            val fifo3 = fifo<SysInteger>(1, "fifo")
            val tester = Tester("tester", this)
            val test1 = Test1("test1", this)
            val test2 = Test2("test2", this)
            val test3 = Test3("test3", this)
            bind(tester.clk to clk, test1.clk to clk, test2.clk to clk, test3.clk to clk)
            bind(test1.result to fifo1, test2.result to fifo2, test3.result to fifo3)
            bind(tester.result[0] to fifo1, tester.result[1] to fifo2, tester.result[2] to fifo3)
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}

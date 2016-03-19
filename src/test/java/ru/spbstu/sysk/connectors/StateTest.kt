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
                val i = iterator(0..3)
                For(i) {
                    // Kotlin BUG: try to pass 'null' as a DEFAULT argument to defaultIterator and then to reference
                    val j = defaultIterator<Int>()
                    For(j, 0..3) {
                        If ({ i.it == 3 }) { Jump("end") }
                        State {
                            switch = !switch
                            println("i: ${i.it} j: ${j()!!.it}")
                            println("1: start loop")
                            put(SysInteger.valueOf(1))
                        }
                        State {
                            println("1: before IF-Else")
                            put(SysInteger.valueOf(2))
                        }
                        If ({ switch }) {
                            State {
                                println("1: If-1")
                                put(SysInteger.valueOf(3))
                            }
                            State {
                                println("1: If-2")
                                put(SysInteger.valueOf(4))
                            }
                            State {
                                println("1: If-3")
                                put(SysInteger.valueOf(5))
                            }
                        }
                        If ({ !switch }) {
                            Sleep(5)
                            Continue()
                            Sleep(101)
                        }
                        State {
                            println("1: after IF-Else")
                            put(SysInteger.valueOf(9))
                        }
                        State {
                            println("1: end loop")
                            put(SysInteger.valueOf(10))
                        }
                    }
                }
                Label("end")
                State() {
                    println("1: end")
                    put(SysInteger.valueOf(11))
                }
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
                For(0..11) {
                    var switch = false
                    State {
                        println("2: start loop")
                        put(SysInteger.valueOf(1))
                    }
                    State {
                        switch = !switch
                        println("2: before IF-Else")
                        put(SysInteger.valueOf(2))
                    }
                    If ({ switch }) {
                        State {
                            println("2: If-1")
                            put(SysInteger.valueOf(3))
                        }
                        State {
                            println("2: If-2")
                            put(SysInteger.valueOf(4))
                        }
                        State {
                            println("2: If-3")
                            put(SysInteger.valueOf(5))
                        }
                    }
                    If ({ !switch }) {
                        Sleep(3)
                    }
                    Else {
                        Sleep(2)
                        Continue()
                        Sleep(101)
                    }
                    State {
                        println("2: after IF-Else")
                        put(SysInteger.valueOf(9))
                    }
                    State {
                        println("2: end loop")
                        put(SysInteger.valueOf(10))
                    }
                }
                State() {
                    println("2: end")
                    put(SysInteger.valueOf(11))
                }
            }
        }
    }

    internal class Tester(name: String, parent: SysModule) : SysModule(name, parent) {

        val clk = bitInput("clk")
        val result = Array(2, { fifoInput<SysInteger>("result") })

        fun check(value: SysInteger) {
            for (i in 0..result.lastIndex) {
                if (!result[i].empty) {
                    assert(result[i].value == value)
                    result[i].pop = SysBit.ONE
                }
            }
        }

        init {
            stateFunction(clk, false) {
                var i = 0
                While({ true }) {
                    If({ i++ > 11 }) { Break() }
                    var switch = false
                    State {
                        println("start loop\n")
                        check(SysInteger.valueOf(1))
                    }
                    State {
                        switch = !switch
                        println("before IF-Else\n")
                        check(SysInteger.valueOf(2))
                    }
                    If ({ switch }) {
                        State {
                            println("If-1\n")
                            check(SysInteger.valueOf(3))
                        }
                        State {
                            println("If-2\n")
                            check(SysInteger.valueOf(4))
                        }
                        State {
                            println("If-3\n")
                            check(SysInteger.valueOf(5))
                        }
                    }
                    Else {
                        State {
                            println("Else-1\n")
                            check(SysInteger.valueOf(6))
                        }
                        State {
                            println("Else-2\n")
                            check(SysInteger.valueOf(7))
                        }
                        State {
                            println("Else-3\n")
                            check(SysInteger.valueOf(8))
                        }
                    }
                    State {
                        println("after IF-Else\n")
                        check(SysInteger.valueOf(9))
                    }
                    State {
                        println("end loop\n")
                        check(SysInteger.valueOf(10))
                    }
                }
                State() {
                    println("end\n")
                    check(SysInteger.valueOf(11))
                    scheduler.stop()
                }
            }
        }
    }

    internal object TopModule : SysTopModule() {
        init {
            val clk = clockedSignal("clk", SysWait.Time(1))
            val fifo1 = fifo<SysInteger>(1, "fifo")
            val fifo2 = fifo<SysInteger>(1, "fifo")
            val tester = Tester("tester", this)
            val test1 = Test1("test", this)
            val test2 = Test2("test", this)
            bind(tester.clk to clk, test1.clk to clk, test2.clk to clk)
            bind(test1.result to fifo1, test2.result to fifo2)
            bind(tester.result[0] to fifo1, tester.result[1] to fifo2)
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}

package ru.spbstu.sysk.core

import org.junit.Test
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysInteger

class SysStateFunctionTest {

    internal class Test1(name: String, parent: SysModule) : SysModule(name, parent) {

        val clk = bitInput("clk")
        val result = fifoOutput<SysInteger>("result")

        fun put(value: SysInteger) {
            result(value)
            result.push = SysBit.ONE
        }

        init {
            stateFunction(clk, true) {
                var switch = false
                val i = iterator(0..3)
                loop(i) {
                    // Kotlin BUG: try to pass 'null' as a DEFAULT argument to defaultIterator and then to reference
                    val j = iterator(0..3)
                    loop(j) {
                        case ({ i.it == 3 }) { jump("end") }
                        state {
                            switch = !switch
                            println("i: ${i.it} j: ${j.it}")
                            println("1: start loop")
                            put(SysInteger.valueOf(1))
                        }
                        state {
                            println("1: before IF-Else")
                            put(SysInteger.valueOf(2))
                        }
                        case ({ switch }) {
                            state {
                                println("1: If-1")
                                put(SysInteger.valueOf(3))
                            }
                            state {
                                println("1: If-2")
                                put(SysInteger.valueOf(4))
                            }
                            state {
                                println("1: If-3")
                                put(SysInteger.valueOf(5))
                            }
                        }
                        case ({ !switch }) {
                            sleep(5)
                            continueLoop()
                            state { throw AssertionError() }
                        }
                        state {
                            println("1: after IF-Else")
                            put(SysInteger.valueOf(9))
                        }
                        state {
                            println("1: end loop")
                            put(SysInteger.valueOf(10))
                        }
                    }
                }
                label("end")
                state() {
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
            result(value)
            result.push = SysBit.ONE
        }

        init {
            stateFunction(clk, true) {
                loop(0..11) {
                    var switch = false
                    state {
                        println("2: start loop")
                        put(SysInteger.valueOf(1))
                    }
                    state {
                        switch = !switch
                        println("2: before IF-Else")
                        put(SysInteger.valueOf(2))
                    }
                    case ({ switch }) {
                        state {
                            println("2: If-1")
                            put(SysInteger.valueOf(3))
                        }
                        state {
                            println("2: If-2")
                            put(SysInteger.valueOf(4))
                        }
                        state {
                            println("2: If-3")
                            put(SysInteger.valueOf(5))
                        }
                    }
                    case ({ !switch }) {
                        sleep(3)
                    }
                    otherwise {
                        sleep(2)
                        continueLoop()
                        state { throw AssertionError() }
                    }
                    state {
                        println("2: after IF-Else")
                        put(SysInteger.valueOf(9))
                    }
                    state {
                        println("2: end loop")
                        put(SysInteger.valueOf(10))
                    }
                }
                state() {
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
                    assert(result[i]() == value)
                    result[i].pop = SysBit.ONE
                }
            }
        }

        init {
            stateFunction(clk, false) {
                var i = 0
                infiniteLoop {
                    case({ i++ > 11 }) { breakLoop() }
                    var switch = false
                    state {
                        println("start loop\n")
                        check(SysInteger.valueOf(1))
                    }
                    state {
                        switch = !switch
                        println("before IF-Else\n")
                        check(SysInteger.valueOf(2))
                    }
                    case { switch }.then {
                        state {
                            println("If-1\n")
                            check(SysInteger.valueOf(3))
                        }
                        state {
                            println("If-2\n")
                            check(SysInteger.valueOf(4))
                        }
                        state {
                            println("If-3\n")
                            check(SysInteger.valueOf(5))
                        }
                    }
                    otherwise {
                        state {
                            println("Else-1\n")
                            check(SysInteger.valueOf(6))
                        }
                        state {
                            println("Else-2\n")
                            check(SysInteger.valueOf(7))
                        }
                        state {
                            println("Else-3\n")
                            check(SysInteger.valueOf(8))
                        }
                    }
                    state {
                        println("after IF-Else\n")
                        check(SysInteger.valueOf(9))
                    }
                    state {
                        println("end loop\n")
                        check(SysInteger.valueOf(10))
                    }
                }
                state {
                    println("end\n")
                    check(SysInteger.valueOf(11))
                    scheduler.stop()
                }
            }
        }
    }

    internal object TopModule : SysTopModule() {
        init {
            val clk = clock("clk", SysWait.Time(1))
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

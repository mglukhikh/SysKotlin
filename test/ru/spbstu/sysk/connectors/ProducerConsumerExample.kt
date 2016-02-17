package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.*

class ProducerConsumerExample {
    internal class Symbol constructor(val value: Char) : SysData {
        constructor() : this('?')
        override fun toString() = value.toString()
    }

    internal class Producer constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {
        val output = fifoOutput<Symbol>("out", null)
        val clk = bitInput("clk", null)

        private val memory = "Visit www.kotlinlang.org and see what Kotlin can do for you today\n!"
        private var index = 0
        val main: () -> Unit = {
            println("I am in producer")
            if (index < memory.length) {
                output.value = Symbol(memory[index])
                output.push = SysBit.ONE
                index++
            }
        }

        init {
            stateFunction(clk, true) {
                infinite(main)
            }
        }
    }

    internal class Consumer constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {
        val input = fifoInput<Symbol>("in", null)
        val clk = bitInput("clk", null)

        val main: () -> Unit = {
            println("I am in consumer")
            val symbol = input.value
            input.pop = SysBit.ONE
            println(symbol)
            if (input.size == 1) println("<1>")
            if (input.size == 9) println("<9>")
            if (symbol.value == '\n') scheduler.stop()
        }

        init {
            stateFunction(clk, true) {
                infinite(main)
            }
        }
    }

    internal object Top : SysTopModule() {

        val consumer: Consumer
        val producer: Producer
        val fifo: SysFifo<Symbol>
        val clk: SysClockedSignal

        init {
            consumer = Consumer("consumer", this)
            producer = Producer("producer", this)
            fifo = fifo(100, "fifo", undefined())
            clk = clockedSignal("clk", SysWait.Time(1), SysBit.ZERO)
            bind(consumer.input to fifo, producer.output to fifo)
            bind(consumer.clk to clk, producer.clk to clk)
        }
    }

    @Test
    fun main() {
        Top.start()
    }
}
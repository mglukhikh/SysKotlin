package ru.spbstu.sysk.channels

import org.junit.Ignore
import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.*

private const val QEMITS: Long = 100000000
private const val CUTOFF: Long = 1000000

class ProducerConsumerExample {
    // BUG if visibility is changed to private
    internal data class Symbol(val value: Char) : SysData {
        companion object : SysDataCompanion<Symbol> {
            override val undefined: Symbol
                get() = Symbol('?')
        }
    }

    private class Producer(
            name: String, parent: SysModule)
    : SysModule(name, parent) {
        val nextCycle = event("nextCycle")
        val output = fifoOutput<Symbol>("out", null)

        private var qEmits: Long = 0

        val main: (SysWait) -> SysWait = {
            while (!output.full) {
                if (qEmits % CUTOFF == 0.toLong()) println("$qEmits: I am in producer")
                if (qEmits < QEMITS) output(Symbol.undefined)
                else output(Symbol('\n'))
                output.push = SysBit.ONE
                qEmits++
            }
            nextCycle.happens()
            nextCycle
        }

        init {
            function(main, nextCycle, true)
        }
    }

    private class Consumer(
            name: String, parent: SysModule)
    : SysModule(name, parent) {
        val nextCycle = event("nextCycle")
        val input = fifoInput<Symbol>("in", null)

        private var qEmits: Long = 0

        val main: (SysWait) -> SysWait = {
            while (!input.empty) {
                val symbol = input()
                if (qEmits % CUTOFF == 0L) println("$qEmits: I am in consumer $symbol")
                input.pop = SysBit.ONE
                if (symbol.value == '\n') scheduler.stop()
                qEmits++
            }
            nextCycle.happens()
            nextCycle
        }

        init {
            function(main, nextCycle, true)
        }
    }

    private object Top : SysTopModule() {

        val consumer: Consumer
        val producer: Producer
        val fifo: SysFifo<Symbol>

        init {
            consumer = Consumer("consumer", this)
            producer = Producer("producer", this)
            fifo = fifo(100, "fifo", undefined())
            bind(consumer.input to fifo, producer.output to fifo)
        }
    }

    @Ignore("Ignored Example")
    @Test
    fun main() {
        Top.start()
        // 8s 149ms
        // 7s 738ms
        // 7s 405ms
        // 7s 378ms
        // 7s 776ms
        // 7s 368ms
        // 7s 760ms
        // 7s 259ms
        // 7s 474ms
        // 7s 499ms
    }
}
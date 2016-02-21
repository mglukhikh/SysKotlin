package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.*

private const val QEMITS : Long = 100000000
private const val CUTOFF : Long = 1000000

class ProducerConsumerExample {
    // BUG if visibility is changed to private
    internal data class Symbol constructor(val value: Char) : SysData {
        companion object : SysDataCompanion<Symbol> {
            override val undefined: Symbol
                get() = Symbol('?')

            fun rand(): Symbol {
                return Symbol((Math.random() * 94 + 32).toChar())
            }
        }
    }

    private class Producer constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {
        val nextCycle = event("nextCycle")
        val output = fifoOutput<Symbol>("out", null)

        private var qEmits: Long = 0

        val main: (SysWait) -> SysWait = {
            while (!output.full) {
                if (qEmits % CUTOFF == 0.toLong()) println("$qEmits: I am in producer")
                if (qEmits < QEMITS) output.value = Symbol.rand()
                else output.value = Symbol('\n')
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

    private class Consumer constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {
        val nextCycle = event("nextCycle")
        val input = fifoInput<Symbol>("in", null)

        private var qEmits: Long = 0

        val main: (SysWait) -> SysWait = {
            while (!input.empty) {
                val symbol = input.value
                if (qEmits % CUTOFF == 0.toLong()) println("$qEmits: I am in consumer $symbol")
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

    @Test
    fun main() {
        Top.start()
        // 16s 278ms
        // 16s 422ms
        // 16s 624ms
        // 16s 833ms
        // 16s 441ms
    }
}
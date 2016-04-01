package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*

class FifoTest : SysTopModule() {

    @Test
    fun bitFifo() {
        val fifo = bitFifo(4, "fifo")
        assert(SysBit.X == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
        fifo.input = ONE
        fifo.push = SysBit.ONE
        assert(fifo.output.one)
        assert(1 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(!fifo.empty)
        fifo.input = ZERO
        fifo.push = SysBit.ONE
        fifo.input = ONE
        fifo.push = SysBit.ONE
        fifo.input = ZERO
        fifo.push = SysBit.ONE
        assert(fifo.output.one)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(fifo.full)
        assert(!fifo.empty)
        fifo.input = ZERO
        fifo.push = SysBit.ONE
        assert(fifo.output.one)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(fifo.full)
        assert(!fifo.empty)
        while (!fifo.empty) fifo.pop = SysBit.ONE
        assert(fifo.output.zero)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(fifo.empty)
        fifo.pop = SysBit.ONE
        assert(fifo.output.zero)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(fifo.empty)
    }

    @Test
    fun synchronousFifo() {
        val fifo = synchronousFifo<SysBit>(4, "fifo", false)
        val clk = clock("clk", 2(FS))
        val input = fifoInput("input", fifo)
        val output = fifoOutput("output", fifo)
        fifo.clk bind clk
        stateFunction(clk) {
            state {
                assert(output.empty)
            }
            val i = iterator(0..5)
            loop(i) {
                state {
                    if (i.it == 0) output.push = ONE
                    if (i.it % 2 == 0) output(ONE)
                    else output(ZERO)
                }
            }
            state {
                assert(output.full)
                output.push = ZERO
            }
            loop(i) {
                state {
                    if (i.it == 0) input.pop = ONE
                    if (i.it < 4) {
                        if (i.it % 2 == 0) assert(input() == ONE)
                        else assert(input() == ZERO)
                    } else assert(input() == ZERO)
                }
            }
            state {
                input.pop = ZERO
                assert(input.empty)
                scheduler.stop()
            }
        }
        start(1(S))
    }

    @Test
    fun synchronousBitFifo() {
        val fifo = synchronousBitFifo(4, "fifo", false)
        val clk = clock("clk", 2(FS))
        val input = fifoInput("input", fifo)
        val output = fifoOutput("output", fifo)
        fifo.clk bind clk
        stateFunction(clk) {
            state {
                assert(output.empty)
            }
            val i = iterator(0..5)
            loop(i) {
                state {
                    if (i.it == 0) output.push = ONE
                    if (i.it % 2 == 0) output(ONE)
                    else output(ZERO)
                }
            }
            state {
                assert(output.full)
                output.push = ZERO
            }
            loop(i) {
                state {
                    if (i.it == 0) input.pop = ONE
                    if (i.it < 4) {
                        if (i.it % 2 == 0) assert(input() == ONE)
                        else assert(input() == ZERO)
                    } else assert(input() == ZERO)
                }
            }
            state {
                input.pop = ZERO
                assert(input.empty)
                scheduler.stop()
            }
        }
        start(1(S))
    }
}
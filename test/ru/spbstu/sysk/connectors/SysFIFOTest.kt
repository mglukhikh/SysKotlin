package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*

class SysFifoTest {

    @Test
    fun sysBitFifo() {
        val fifo = SysBitFifo(4, "Fifo", SysScheduler())
        assert("Fifo" == fifo.name)
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
}
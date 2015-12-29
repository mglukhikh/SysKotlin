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
        fifo.push()
        assert(fifo.output.one)
        assert(1 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(!fifo.empty)
        fifo.input = ZERO
        fifo.push()
        fifo.input = ONE
        fifo.push()
        fifo.input = ZERO
        fifo.push()
        assert(fifo.output.one)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(fifo.full)
        assert(!fifo.empty)
        fifo.input = ZERO
        fifo.push()
        assert(fifo.output.one)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(fifo.full)
        assert(!fifo.empty)
        while (!fifo.empty) fifo.pop()
        assert(fifo.output.zero)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(fifo.empty)
        fifo.pop()
        assert(fifo.output.zero)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(fifo.empty)
    }

    @Test
    fun sysAsynchronousFifo() {
        var fifo = SysAsynchronousFifo(4, "Fifo", SysBit.ZERO, SysScheduler())
        assert("Fifo" == fifo.name)
        assert(fifo.output.zero)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(fifo.empty)
        fifo.push = ZERO;
        assert(fifo.output.zero)
        assert(0 == fifo.size)
        fifo.input = ONE;
        fifo.push = ONE;
        assert(fifo.output.one)
        assert(1 == fifo.size)
        fifo.input = ZERO;
        fifo.push = ONE;
        assert(fifo.output.one)
        assert(1 == fifo.size)
        fifo.push = ZERO;
        assert(fifo.output.one)
        assert(1 == fifo.size)
        fifo.push = ONE;
        assert(fifo.output.one)
        assert(2 == fifo.size)
        fifo.pop = ZERO;
        assert(fifo.output.one)
        assert(2 == fifo.size)
        fifo.pop = ONE;
        assert(fifo.output.zero)
        assert(1 == fifo.size)
        fifo.pop = ZERO;
        assert(fifo.output.zero)
        assert(1 == fifo.size)
        fifo.pop = ONE;
        assert(fifo.output.zero)
        assert(0 == fifo.size)
    }
}
package sysk

import org.junit.Test

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
        fifo.input = SysBit.ONE
        fifo.push()
        assert(SysBit.ONE == fifo.output)
        assert(1 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(false == fifo.empty)
        fifo.input = (SysBit.ZERO)
        fifo.push()
        fifo.input = (SysBit.ONE)
        fifo.push()
        fifo.input = (SysBit.ZERO)
        fifo.push()
        assert(SysBit.ONE == fifo.output)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(true == fifo.full)
        assert(false == fifo.empty)
        fifo.input = (SysBit.ZERO)
        fifo.push()
        assert(SysBit.ONE == fifo.output)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(true == fifo.full)
        assert(false == fifo.empty)
        while (!fifo.empty) fifo.pop()
        assert(SysBit.ZERO == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
        fifo.pop()
        assert(SysBit.ZERO == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
    }

    @Test
    fun sysAsynchronousFifo() {
        var fifo = SysAsynchronousFifo(4, "Fifo", SysBit.ZERO, SysScheduler())
        assert("Fifo" == fifo.name)
        assert(SysBit.ZERO == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(fifo.empty)
        fifo.push = SysBit.ZERO;
        assert(SysBit.ZERO == fifo.output)
        assert(0 == fifo.size)
        fifo.input = SysBit.ONE;
        fifo.push = SysBit.ONE;
        assert(SysBit.ONE == fifo.output)
        assert(1 == fifo.size)
        fifo.input = SysBit.ZERO;
        fifo.push = SysBit.ONE;
        assert(SysBit.ONE == fifo.output)
        assert(1 == fifo.size)
        fifo.push = SysBit.ZERO;
        assert(SysBit.ONE == fifo.output)
        assert(1 == fifo.size)
        fifo.push = SysBit.ONE;
        assert(SysBit.ONE == fifo.output)
        assert(2 == fifo.size)
        fifo.pop = SysBit.ZERO;
        assert(SysBit.ONE == fifo.output)
        assert(2 == fifo.size)
        fifo.pop = SysBit.ONE;
        assert(SysBit.ZERO == fifo.output)
        assert(1 == fifo.size)
        fifo.pop = SysBit.ZERO;
        assert(SysBit.ZERO == fifo.output)
        assert(1 == fifo.size)
        fifo.pop = SysBit.ONE;
        assert(SysBit.ZERO == fifo.output)
        assert(0 == fifo.size)
    }
}
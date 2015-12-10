package sysk

import org.junit.Test

class SysFifoTest {
    @Test
    fun SysFifo() {
        var Fifo = SysFifo(4, "Fifo", false, SysScheduler())
        var check: Boolean = false
        try { SysFifoOutput("_1", null, Fifo) } catch(exc: IllegalStateException) { check = true }
        assert(check == false)
        try { SysFifoOutput("_2", null, Fifo) } catch(exc: IllegalStateException) { check = true }
        assert(check == true)
        assert("Fifo" == Fifo.name)
        assert(false == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
        Fifo.input = true
        Fifo.push()
        assert(true == Fifo.output)
        assert(1 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(false == Fifo.empty)
        Fifo.input = (false)
        Fifo.push()
        Fifo.input = (true)
        Fifo.push()
        Fifo.input = (false)
        Fifo.push()
        assert(true == Fifo.output)
        assert(4 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(true == Fifo.full)
        assert(false == Fifo.empty)
        Fifo.input = (false)
        Fifo.push()
        assert(true == Fifo.output)
        assert(4 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(true == Fifo.full)
        assert(false == Fifo.empty)
        while (!Fifo.empty) Fifo.pop()
        assert(false == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
        Fifo.pop()
        assert(false == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
    }

    @Test
    fun SysWireFifo() {
        var Fifo: SysWireFifo = SysWireFifo(4, "Fifo", SysScheduler())
        assert("Fifo" == Fifo.name)
        assert(SysBit.X == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
        Fifo.input = SysBit.ONE
        Fifo.push()
        assert(SysBit.ONE == Fifo.output)
        assert(1 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(false == Fifo.empty)
        Fifo.input = (SysBit.ZERO)
        Fifo.push()
        Fifo.input = (SysBit.ONE)
        Fifo.push()
        Fifo.input = (SysBit.ZERO)
        Fifo.push()
        assert(SysBit.ONE == Fifo.output)
        assert(4 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(true == Fifo.full)
        assert(false == Fifo.empty)
        Fifo.input = (SysBit.ZERO)
        Fifo.push()
        assert(SysBit.ONE == Fifo.output)
        assert(4 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(true == Fifo.full)
        assert(false == Fifo.empty)
        while (!Fifo.empty) Fifo.pop()
        assert(SysBit.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
        Fifo.pop()
        assert(SysBit.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
    }

    @Test
    fun SysAsynchronousFifo() {
        var Fifo: SysAsynchronousFifo<SysBit> = SysAsynchronousFifo(4, "Fifo", SysBit.ZERO, SysScheduler())
        assert("Fifo" == Fifo.name)
        assert(SysBit.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(!Fifo.full)
        assert(Fifo.empty)
        Fifo.push = SysBit.ZERO;
        assert(SysBit.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        Fifo.input = SysBit.ONE;
        Fifo.push = SysBit.ONE;
        assert(SysBit.ONE == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.input = SysBit.ZERO;
        Fifo.push = SysBit.ONE;
        assert(SysBit.ONE == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.push = SysBit.ZERO;
        assert(SysBit.ONE == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.push = SysBit.ONE;
        assert(SysBit.ONE == Fifo.output)
        assert(2 == Fifo.size)
        Fifo.pop = SysBit.ZERO;
        assert(SysBit.ONE == Fifo.output)
        assert(2 == Fifo.size)
        Fifo.pop = SysBit.ONE;
        assert(SysBit.ZERO == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.pop = SysBit.ZERO;
        assert(SysBit.ZERO == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.pop = SysBit.ONE;
        assert(SysBit.ZERO == Fifo.output)
        assert(0 == Fifo.size)
    }
}
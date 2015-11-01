package sysk

import org.junit.Test

class SysFifoTest {
    @Test
    fun SysFifo() {
        var Fifo = SysFifo(4, "Fifo", false, SysScheduler())
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
        assert(SysWireState.X == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
        Fifo.input = SysWireState.ONE
        Fifo.push()
        assert(SysWireState.ONE == Fifo.output)
        assert(1 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(false == Fifo.empty)
        Fifo.input = (SysWireState.ZERO)
        Fifo.push()
        Fifo.input = (SysWireState.ONE)
        Fifo.push()
        Fifo.input = (SysWireState.ZERO)
        Fifo.push()
        assert(SysWireState.ONE == Fifo.output)
        assert(4 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(true == Fifo.full)
        assert(false == Fifo.empty)
        Fifo.input = (SysWireState.ZERO)
        Fifo.push()
        assert(SysWireState.ONE == Fifo.output)
        assert(4 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(true == Fifo.full)
        assert(false == Fifo.empty)
        while (!Fifo.empty) Fifo.pop()
        assert(SysWireState.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
        Fifo.pop()
        assert(SysWireState.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(false == Fifo.full)
        assert(true == Fifo.empty)
    }

    @Test
    fun SysAsynchronousFifo() {
        var Fifo: SysAsynchronousFifo<SysWireState> = SysAsynchronousFifo(4, "Fifo", SysWireState.ZERO, SysScheduler())
        assert("Fifo" == Fifo.name)
        assert(SysWireState.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        assert(4 == Fifo.capacity)
        assert(!Fifo.full)
        assert(Fifo.empty)
        Fifo.push = SysWireState.ZERO;
        assert(SysWireState.ZERO == Fifo.output)
        assert(0 == Fifo.size)
        Fifo.input = SysWireState.ONE;
        Fifo.push = SysWireState.ONE;
        assert(SysWireState.ONE == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.input = SysWireState.ZERO;
        Fifo.push = SysWireState.ONE;
        assert(SysWireState.ONE == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.push = SysWireState.ZERO;
        assert(SysWireState.ONE == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.push = SysWireState.ONE;
        assert(SysWireState.ONE == Fifo.output)
        assert(2 == Fifo.size)
        Fifo.pop = SysWireState.ZERO;
        assert(SysWireState.ONE == Fifo.output)
        assert(2 == Fifo.size)
        Fifo.pop = SysWireState.ONE;
        assert(SysWireState.ZERO == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.pop = SysWireState.ZERO;
        assert(SysWireState.ZERO == Fifo.output)
        assert(1 == Fifo.size)
        Fifo.pop = SysWireState.ONE;
        assert(SysWireState.ZERO == Fifo.output)
        assert(0 == Fifo.size)
    }


    override fun toString(): String {
        return "SysFifoTest"
    }
}
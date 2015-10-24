package sysk

import org.junit.Test

class SysFIFOTest {
    @Test
    fun SysFIFOInterface() {
        var fifo: SysFIFOInterface<Boolean> = SysFIFOInterface(4, "fifo", false, SysScheduler())
        assert("fifo" == fifo.name)
        assert(false == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
        fifo.input = true
        fifo.push()
        assert(true == fifo.output)
        assert(1 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(false == fifo.empty)
        fifo.input = (false)
        fifo.push()
        fifo.input = (true)
        fifo.push()
        fifo.input = (false)
        fifo.push()
        assert(true == fifo.output)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(true == fifo.full)
        assert(false == fifo.empty)
        fifo.input = (false)
        fifo.push()
        assert(true == fifo.output)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(true == fifo.full)
        assert(false == fifo.empty)
        while (!fifo.empty) fifo.pop()
        assert(false == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
        fifo.pop()
        assert(false == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
    }

    @Test
    fun SysWireFIFO() {
        var fifo: SysWireFIFO = SysWireFIFO(4, "fifo", SysScheduler())
        assert("fifo" == fifo.name)
        assert(SysWireState.X == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
        fifo.input = SysWireState.ONE
        fifo.push()
        assert(SysWireState.ONE == fifo.output)
        assert(1 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(false == fifo.empty)
        fifo.input = (SysWireState.ZERO)
        fifo.push()
        fifo.input = (SysWireState.ONE)
        fifo.push()
        fifo.input = (SysWireState.ZERO)
        fifo.push()
        assert(SysWireState.ONE == fifo.output)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(true == fifo.full)
        assert(false == fifo.empty)
        fifo.input = (SysWireState.ZERO)
        fifo.push()
        assert(SysWireState.ONE == fifo.output)
        assert(4 == fifo.size)
        assert(4 == fifo.capacity)
        assert(true == fifo.full)
        assert(false == fifo.empty)
        while (!fifo.empty) fifo.pop()
        assert(SysWireState.ZERO == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
        fifo.pop()
        assert(SysWireState.ZERO == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(false == fifo.full)
        assert(true == fifo.empty)
    }

    @Test
    fun SysAsynchronousFIFO() {
        var fifo: SysAsynchronousFIFO<SysWireState> = SysAsynchronousFIFO(4, "fifo", SysWireState.ZERO, SysScheduler())
        assert("fifo" == fifo.name)
        assert(SysWireState.ZERO == fifo.output)
        assert(0 == fifo.size)
        assert(4 == fifo.capacity)
        assert(!fifo.full)
        assert(fifo.empty)
        fifo.push = SysWireState.ZERO;
        assert(SysWireState.ZERO == fifo.output)
        assert(0 == fifo.size)
        fifo.input = SysWireState.ONE;
        fifo.push = SysWireState.ONE;
        assert(SysWireState.ONE == fifo.output)
        assert(1 == fifo.size)
        fifo.input = SysWireState.ZERO;
        fifo.push = SysWireState.ONE;
        assert(SysWireState.ONE == fifo.output)
        assert(1 == fifo.size)
        fifo.push = SysWireState.ZERO;
        assert(SysWireState.ONE == fifo.output)
        assert(1 == fifo.size)
        fifo.push = SysWireState.ONE;
        assert(SysWireState.ONE == fifo.output)
        assert(2 == fifo.size)
        fifo.pop = SysWireState.ZERO;
        assert(SysWireState.ONE == fifo.output)
        assert(2 == fifo.size)
        fifo.pop = SysWireState.ONE;
        assert(SysWireState.ZERO == fifo.output)
        assert(1 == fifo.size)
        fifo.pop = SysWireState.ZERO;
        assert(SysWireState.ZERO == fifo.output)
        assert(1 == fifo.size)
        fifo.pop = SysWireState.ONE;
        assert(SysWireState.ZERO == fifo.output)
        assert(0 == fifo.size)
    }


    override fun toString(): String {
        return "SysFIFOTest"
    }
}
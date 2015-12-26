package sysk

import org.junit.Test

class SysPortTest {

    @Test
    fun SysSignalPort() {
        val connector = SysSignal("connector", SysBit.ZERO, SysScheduler())
        val input = SysInput("input", null, connector)
        val output = SysOutput("output", null, connector)
        assert(input.value.zero)
        output.value = SysBit.ONE
        connector.update()
        assert(input.value.one)
    }

    @Test
    fun SysFifoPort() {
        var connector = SysAsynchronousFifo(4, "Fifo", SysBit.ZERO, SysScheduler())
        val input = SysFifoInput("input", null, connector)
        val output = SysFifoOutput("output", null, connector)
        output.push = SysBit.ZERO;
        assert(input.value.zero)
        assert(0 == input.size)
        output.value = SysBit.ONE;
        output.push = SysBit.ONE;
        assert(input.value.one)
        assert(!input.empty)
        assert(!input.full)
        assert(input.size == 1)
        assert(!output.full)
        assert(!output.empty)
        assert(output.size == 1)
        input.pop = SysBit.ZERO
        input.pop = SysBit.ONE
        assert(input.value.one)
        assert(input.size == 0)
        assert(input.empty)
    }

    @Test
    fun SysBusPort() {
        var connector = SysBitBus("connector", SysScheduler())
        connector.addWire()
        connector.addWire()
        connector.addWire()
        connector.addWire()
        val port_1 = SysBusPort("port1", null, connector)
        val port_2 = SysBusPort("port2", null, connector)
        val port_3 = SysBusPort("port3", null, connector)
        assert(port_1[0].x)
        assert(port_2[1].x)
        assert(port_3[2].x)
        port_3.set(SysBit.ZERO, 0)
        port_1.set(SysBit.ONE, 1)
        port_2.set(SysBit.ZERO, 2)
        connector.update()
        assert(port_1[0].zero)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_3.set(SysBit.ZERO, 0)
        port_1.set(SysBit.ONE, 0)
        connector.update()
        assert(port_1[0].x)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_1.set(SysBit.Z, 0)
        connector.update()
        assert(port_1[0].zero)
    }
}
package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.connectors.SysAsynchronousFifo
import ru.spbstu.sysk.connectors.SysBitBus
import ru.spbstu.sysk.connectors.SysFifoInput
import ru.spbstu.sysk.connectors.SysFifoOutput
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.data.SysBit.*

class SysPortTest {

    @Test
    fun SysSignalPort() {
        val connector = SysSignal("connector", ZERO, SysScheduler())
        val input = SysInput("input", null, connector)
        val output = SysOutput("output", null, connector)
        assert(input.value.zero)
        output.value = ONE
        connector.update()
        assert(input.value.one)
    }

    // TODO: move to sysk.connectors
    @Test
    fun SysFifoPort() {
        var connector = SysAsynchronousFifo(4, "Fifo", ZERO, SysScheduler())
        val input = SysFifoInput("input", null, connector)
        val output = SysFifoOutput("output", null, connector)
        output.push = ZERO;
        assert(input.value.zero)
        assert(0 == input.size)
        output.value = ONE;
        output.push = ONE;
        assert(input.value.one)
        assert(!input.empty)
        assert(!input.full)
        assert(input.size == 1)
        assert(!output.full)
        assert(!output.empty)
        assert(output.size == 1)
        input.pop = ZERO
        input.pop = ONE
        assert(input.value.one)
        assert(input.size == 0)
        assert(input.empty)
    }

    // TODO: move to sysk.connectors
    @Test
    fun SysBusPort() {
        var connector = SysBitBus("connector", SysScheduler())
        connector.addWire()
        connector.addWire()
        connector.addWire()
        connector.addWire()
        val port_1 = ru.spbstu.sysk.connectors.SysBusPort("port1", null, connector)
        val port_2 = ru.spbstu.sysk.connectors.SysBusPort("port2", null, connector)
        val port_3 = ru.spbstu.sysk.connectors.SysBusPort("port3", null, connector)
        assert(port_1[0].x)
        assert(port_2[1].x)
        assert(port_3[2].x)
        port_3.set(ZERO, 0)
        port_1.set(ONE, 1)
        port_2.set(ZERO, 2)
        connector.update()
        assert(port_1[0].zero)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_3.set(ZERO, 0)
        port_1.set(ONE, 0)
        connector.update()
        assert(port_1[0].x)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_1.set(Z, 0)
        connector.update()
        assert(port_1[0].zero)
    }
}
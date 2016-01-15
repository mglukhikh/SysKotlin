package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.connectors.SysAsynchronousFifo
import ru.spbstu.sysk.connectors.SysBitBus
import ru.spbstu.sysk.connectors.SysFifoInput
import ru.spbstu.sysk.connectors.SysFifoOutput
import ru.spbstu.sysk.connectors.SysBusPort
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit.*

class SysPortTest {

    @Test
    fun SysSignalPort() {
        val top = SysTopModule()
        val connector = SysSignal("connector", ZERO, top.scheduler)
        val input = SysInput("input", top.scheduler, top, connector)
        val output = SysOutput("output", top.scheduler, top, connector)
        assert(input.value.zero)
        output.value = ONE
        connector.update()
        assert(input.value.one)

        val defaultInput = SysInput("input", top.scheduler, top, null, undefined<SysBit>())
        assert(defaultInput.value == undefined<SysBit>())
        defaultInput.bind(connector)
        assert(defaultInput.value.one)
    }

    // TODO: move to sysk.connectors
    @Test
    fun SysFifoPort() {
        val top = SysTopModule()
        var connector = SysAsynchronousFifo(4, "Fifo", ZERO, top.scheduler)
        val input = SysFifoInput("input", top.scheduler, top, connector)
        val output = SysFifoOutput("output", top.scheduler, top, connector)
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
        val top = SysTopModule()
        var connector = SysBitBus("connector", top.scheduler)
        connector.addWire()
        connector.addWire()
        connector.addWire()
        connector.addWire()
        val port_1 = SysBusPort("port1", top.scheduler, top, connector)
        val port_2 = SysBusPort("port2", top.scheduler, top, connector)
        val port_3 = SysBusPort("port3", top.scheduler, top, connector)
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

        val defaultPort = SysBusPort("port", top.scheduler, top, null, undefined<SysBit>())
        assert(defaultPort[105] == undefined<SysBit>())
        assert(defaultPort[84462] == undefined<SysBit>())
        defaultPort.bind(connector)
        assert(defaultPort[0].zero)
        assert(defaultPort[1].one)
        assert(defaultPort[2].zero)
    }

    @Test
    fun checkBindingPort() {
        val top1 = SysTopModule()
        SysInput("input1", top1.scheduler, top1, null, null)
        var check = true
        try {
            top1.start()
        } catch (error: AssertionError) {
            check = false
        }
        assert(check == false)
        val top2 = SysTopModule()
        val input2 = SysInput("input2", top2.scheduler, top2, null, null)
        input2.seal()
        top2.start()
    }
}
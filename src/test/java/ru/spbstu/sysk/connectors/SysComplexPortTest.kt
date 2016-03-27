package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.bind
import ru.spbstu.sysk.data.undefined

class SysComplexPortTest {

    @Test
    fun fifoPort() {
        val top = SysTopModule()
        var connector = SysFifo(4, "Fifo", SysBit.ZERO, top.scheduler)
        val input = SysFifoInput("input", top.scheduler, top, connector)
        val output = SysFifoOutput("output", top.scheduler, top, connector)
        assert(input().zero)
        assert(0 == input.size)
        output(SysBit.ONE)
        output.push = SysBit.ONE
        assert(input().one)
        assert(!input.empty)
        assert(!input.full)
        assert(input.size == 1)
        assert(!output.full)
        assert(!output.empty)
        assert(output.size == 1)
        input.pop = SysBit.ONE
        assert(input().one)
        assert(input.size == 0)
        assert(input.empty)
    }

    @Test
    fun busPort() {
        val top = SysTopModule()
        var connector = SysBitBus(4, "connector", top.scheduler)
        val port_1 = SysBusPort<SysBit>(4, "port1", top.scheduler, top)
        val port_2 = SysBusPort<SysBit>(4, "port2", top.scheduler, top)
        val port_3 = SysBusPort<SysBit>(4, "port3", top.scheduler, top)
        bind(port_1 to connector, port_2 to connector, port_3 to connector)
        assert(port_1[0].x)
        assert(port_2[1].x)
        assert(port_3[2].x)
        port_3(SysBit.ZERO, 0)
        port_1(SysBit.ONE, 1)
        port_2(SysBit.ZERO, 2)
        connector.update()
        assert(port_1[0].zero)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_3(SysBit.ZERO, 0)
        port_1(SysBit.ONE, 0)
        connector.update()
        assert(port_1[0].x)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_1(SysBit.Z, 0)
        connector.update()
        assert(port_1[0].zero)

        val defaultPort = SysBusPort(4, "port", top.scheduler, top, undefined<SysBit>())
        assert(defaultPort[105] == undefined<SysBit>())
        assert(defaultPort[84462] == undefined<SysBit>())
        defaultPort.bind(connector)
        assert(defaultPort[0].zero)
        assert(defaultPort[1].one)
        assert(defaultPort[2].zero)
    }
}
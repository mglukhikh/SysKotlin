package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*

class SysBusTest {
    @Test
    fun sysBitBus() {
        val top = SysTopModule()
        var connector = SysBitBus(4, "connector", top.scheduler)
        val port_1 = SysBusPort(4, "port1", top.scheduler, top, connector)
        val port_2 = SysBusPort(4, "port2", top.scheduler, top, connector)
        val port_3 = SysBusPort(4, "port3", top.scheduler, top, connector)
        assert(port_1[0].x)
        assert(port_2[1].x)
        assert(port_3[2].x)
        port_3(ZERO, 0)
        port_1(ONE, 1)
        port_2(ZERO, 2)
        connector.update()
        assert(port_1[0].zero)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_3(ZERO, 0)
        port_1(ONE, 0)
        connector.update()
        assert(port_1[0].x)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_1(SysBit.Z, 0)
        connector.update()
        assert(port_1[0].zero)
    }

    @Test
    fun SysFifoBus() {
        val top = SysTopModule()
        var bus = SysFifoBus(4, Array(4, { if (it == 2) ONE else ZERO }), "bus", top.scheduler)
        var port = SysBusPort(4, "port", top.scheduler, top, bus)
        assert(bus[0].zero)
        assert(bus[1].zero)
        assert(bus[2].one)
        assert(bus[3].zero)
        bus.set(SysBit.ONE, 1, port)
        assert(bus[1].zero)
        bus.update()
        assert(bus[1].one)
        bus.set(ONE, 2, port)
        bus.set(ZERO, 2, port)
        bus.update()
        assert(bus[2].one)
        bus.update()
        assert(bus[2].zero)
    }

    @Test
    fun SysPriorityBus() {
        val top = SysTopModule()
        var bus = SysPriorityBus(4, Array(4, { if (it == 2) SysPriorityValue(1, ONE) else SysPriorityValue(1, ZERO) }), "bus", top.scheduler)
        var port = SysBusPort(4, "port", top.scheduler, top, bus)
        assert(bus[0]().zero)
        assert(bus[1]().zero)
        assert(bus[2]().one)
        assert(bus[3]().zero)
        bus.set(SysPriorityValue(10, ONE), 1, port)
        assert(bus[1]().zero)
        bus.set(SysPriorityValue(3, ZERO), 1, port)
        bus.update()
        assert(bus[1]().one)
        bus.set(SysPriorityValue(3, ZERO), 1, port)
        bus.update()
        assert(bus[1]().zero)
    }
}
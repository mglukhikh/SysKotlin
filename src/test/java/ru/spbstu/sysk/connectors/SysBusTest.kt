package ru.spbstu.sysk.connectors

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*

class SysBusTest {
    @Test
    fun sysBitBus() {
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
        port_1.set(SysBit.Z, 0)
        connector.update()
        assert(port_1[0].zero)
    }

    @Test
    fun SysFifoBus() {
        val top = SysTopModule()
        var bus = SysFifoBus<SysBit>("bus", top.scheduler)
        var port = SysBusPort("port", top.scheduler, top, bus)
        bus.addWire(ZERO)
        bus.addWire(ZERO)
        bus.addWire(ONE)
        bus.addWire(ZERO)
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
        var bus = SysPriorityBus<SysBit>("bus", top.scheduler)
        var port = SysBusPort("port", top.scheduler, top, bus)
        bus.addWire(SysPriorityValue(1, ZERO))
        bus.addWire(SysPriorityValue(1, ZERO))
        bus.addWire(SysPriorityValue(1, ONE))
        bus.addWire(SysPriorityValue(1, ZERO))
        assert(bus[0].value.zero)
        assert(bus[1].value.zero)
        assert(bus[2].value.one)
        assert(bus[3].value.zero)
        bus.set(SysPriorityValue(10, ONE), 1, port)
        assert(bus[1].value.zero)
        bus.set(SysPriorityValue(3, ZERO), 1, port)
        bus.update()
        assert(bus[1].value.one)
        bus.set(SysPriorityValue(3, ZERO), 1, port)
        bus.update()
        assert(bus[1].value.zero)
    }
}
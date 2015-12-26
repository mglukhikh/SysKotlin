package sysk

import org.junit.Test

class SysBusTest {
    @Test
    fun sysBitBus() {
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

    @Test
    fun SysFifoBus() {
        var bus = sysk.SysFifoBus<SysBit>("bus", SysScheduler())
        var port = SysBusPort("port", null, bus)
        bus.addWire(SysBit.ZERO)
        bus.addWire(SysBit.ZERO)
        bus.addWire(SysBit.ONE)
        bus.addWire(SysBit.ZERO)
        assert(bus[0].zero)
        assert(bus[1].zero)
        assert(bus[2].one)
        assert(bus[3].zero)
        bus.set(SysBit.ONE, 1, port)
        assert(bus[1].zero)
        bus.update()
        assert(bus[1].one)
        bus.set(SysBit.ONE, 2, port)
        bus.set(SysBit.ZERO, 2, port)
        bus.update()
        assert(bus[2].one)
        bus.update()
        assert(bus[2].zero)
    }

    @Test
    fun SysPriorityBus() {
        var bus = sysk.SysPriorityBus<SysBit>("bus", SysScheduler())
        var port = SysBusPort("port", null, bus)
        bus.addWire(SysPriorityValue(1, SysBit.ZERO))
        bus.addWire(SysPriorityValue(1, SysBit.ZERO))
        bus.addWire(SysPriorityValue(1, SysBit.ONE))
        bus.addWire(SysPriorityValue(1, SysBit.ZERO))
        assert(bus[0].value.zero)
        assert(bus[1].value.zero)
        assert(bus[2].value.one)
        assert(bus[3].value.zero)
        bus.set(SysPriorityValue(10, SysBit.ONE), 1, port)
        assert(bus[1].value.zero)
        bus.set(SysPriorityValue(3, SysBit.ZERO), 1, port)
        bus.update()
        assert(bus[1].value.one)
        bus.set(SysPriorityValue(3, SysBit.ZERO), 1, port)
        bus.update()
        assert(bus[1].value.zero)
    }
}
package sysk

import org.junit.Test

class SysBusTest {
    @Test
    fun SysWireBus() {
        var bus = sysk.SysWireBus("bus", SysScheduler())
        bus.addWire(SysWireState.ZERO)
        bus.addWire(SysWireState.ZERO)
        bus.addWire(SysWireState.ONE)
        bus.addWire(SysWireState.ZERO)
        assert(bus[0].zero)
        assert(bus[1].zero)
        assert(bus[2].one)
        assert(bus[3].zero)
        bus.set(SysWireState.ONE, 1)
        assert(bus[1].zero)
        bus.update()
        assert(bus[1].one)
        bus.set(SysWireState.ONE, 2)
        bus.set(SysWireState.ZERO, 2)
        bus.update()
        assert(bus[2].x)
    }

    @Test
    fun SysFifoBus() {
        var bus = sysk.SysFifoBus<SysWireState>("bus", SysScheduler())
        bus.addWire(SysWireState.ZERO)
        bus.addWire(SysWireState.ZERO)
        bus.addWire(SysWireState.ONE)
        bus.addWire(SysWireState.ZERO)
        assert(bus[0].zero)
        assert(bus[1].zero)
        assert(bus[2].one)
        assert(bus[3].zero)
        bus.set(SysWireState.ONE, 1)
        assert(bus[1].zero)
        bus.update()
        assert(bus[1].one)
        bus.set(SysWireState.ONE, 2)
        bus.set(SysWireState.ZERO, 2)
        bus.update()
        assert(bus[2].one)
        bus.update()
        assert(bus[2].zero)
    }

    @Test
    fun SysPriorityBus() {
        var bus = sysk.SysPriorityBus<SysWireState>("bus", SysScheduler())
        bus.addWire(SysPriorityValue(1, SysWireState.ZERO))
        bus.addWire(SysPriorityValue(1, SysWireState.ZERO))
        bus.addWire(SysPriorityValue(1, SysWireState.ONE))
        bus.addWire(SysPriorityValue(1, SysWireState.ZERO))
        assert(bus[0].value.zero)
        assert(bus[1].value.zero)
        assert(bus[2].value.one)
        assert(bus[3].value.zero)
        bus.set(SysPriorityValue(10, SysWireState.ONE), 1)
        assert(bus[1].value.zero)
        bus.set(SysPriorityValue(3, SysWireState.ZERO), 1)
        bus.update()
        assert(bus[1].value.one)
        bus.set(SysPriorityValue(3, SysWireState.ZERO), 1)
        bus.update()
        assert(bus[1].value.zero)
    }
}
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
}
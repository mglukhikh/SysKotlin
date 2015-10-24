package sysk

import org.junit.Test

class SysPortTest {

    @Test
    fun interestingSysPortTest_1() {
        val connector = SysSignal("connector", false, SysScheduler())
        val input_1 = SysInput("input", null, connector)
        val input_2 = SysInput<Boolean>("output", null, null)
        val output = SysOutput("output", null, connector)

        val _connector = SysSignal("connector", connector, SysScheduler())
        val _input = SysInput("input", null, _connector)
        val _output = SysOutput("output", null, _connector)

        output.value = true;
        _output.value = connector
        _connector.update()
        connector.update()
        input_2.bind(_input.value)
        assert(input_2.value == input_1.value)
    }

    @Test
    fun interestingSysPortTest_2() {
        val connector = SysSignal<SysSignal<*>>("connector", SysSignal("", false, SysScheduler()), SysScheduler())
        val input = SysInput("input", null, connector)
        val output = SysOutput("output", null, connector)

        output.value = connector
        connector.update()
        assert(input.value == connector)
    }

    @Test
    fun SysSignalPort() {
        val connector = SysSignal("connector", false, SysScheduler())
        val input = SysInput("input", null, connector)
        val output = SysOutput("output", null, connector)
        assert(!input.value)
        output.value = true
        connector.update()
        assert(input.value)
    }

    @Test
    fun SysFIFOPort() {
        var connector: SysAsynchronousFIFO<SysWireState> = SysAsynchronousFIFO(4, "fifo", SysWireState.ZERO, SysScheduler())
        val input = SysFIFOInput("input", null, connector)
        val output = SysFIFOOutput("output", null, connector)
        output.push = SysWireState.ZERO;
        assert(input.value.zero)
        assert(0 == input.size)
        output.value = SysWireState.ONE;
        output.push = SysWireState.ONE;
        assert(input.value.one)
        assert(!input.empty)
        assert(!input.full)
        assert(input.size == 1)
        assert(!output.full)
        assert(!output.empty)
        assert(output.size == 1)
        input.pop = SysWireState.ZERO
        input.pop = SysWireState.ONE
        assert(input.value.one)
        assert(input.size == 0)
        assert(input.empty)
    }

    @Test
    fun SysBusPort() {
        var connector = SysWireBus("connector", SysScheduler())
        connector.addWire(SysWireState.ZERO)
        connector.addWire(SysWireState.ZERO)
        connector.addWire(SysWireState.ONE)
        connector.addWire(SysWireState.ZERO)
        val port_1 = SysBusPort("port", null, connector)
        val port_2 = SysBusPort("port", null, connector)
        val port_3 = SysBusPort("port", null, connector)
        assert(port_1[0].zero)
        assert(port_2[1].zero)
        assert(port_3[2].one)
        port_3.set(SysWireState.ZERO, 0)
        port_1.set(SysWireState.ONE, 1)
        port_2.set(SysWireState.ZERO, 2)
        connector.update()
        assert(port_1[0].zero)
        assert(port_2[1].one)
        assert(port_3[2].zero)
        port_3.set(SysWireState.ZERO, 0)
        port_1.set(SysWireState.ONE, 0)
        connector.update()
        assert(port_1[0].x)
        assert(port_2[1].one)
        assert(port_3[2].zero)
    }
}
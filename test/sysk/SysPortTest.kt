package sysk

import org.junit.Test

class SysPortTest {

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
        assert(SysWireState.ZERO == input.value)
        assert(0 == input.size)
        output.value = SysWireState.ONE;
        output.push = SysWireState.ONE;
        assert(SysWireState.ONE == input.value)
        assert(1 == input.size)
    }

    @Test
    fun abnormalBehavior() {
        try {
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
        } finally {
        }

        try {
            val connector = SysSignal<SysSignal<*>>("connector", SysSignal("", false, SysScheduler()), SysScheduler())
            val input = SysInput("input", null, connector)
            val output = SysOutput("output", null, connector)

            output.value = connector
            connector.update()
            assert(input.value == connector)
        } finally {
        }
    }
}
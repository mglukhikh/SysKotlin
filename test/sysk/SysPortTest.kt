package sysk

import org.junit.Test

class SysPortTest {

    @Test
    fun test() {
        val connector = SysSignal("connector", false, SysScheduler())
        val input = SysInput("input", null, connector)
        val output = SysOutput("output", null, connector)
        assert(!input.value)
        output.value = true
        connector.update()
        assert(input.value)
    }
}
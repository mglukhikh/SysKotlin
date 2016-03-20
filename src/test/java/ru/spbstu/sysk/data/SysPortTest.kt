package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit.*

class SysPortTest {

    @Test
    fun signalPortTest() {
        val top = SysTopModule()
        val connector = SysSignal("connector", ZERO, top.scheduler)
        val input = SysInput("input", top.scheduler, top, connector)
        val output = SysOutput("output", top.scheduler, top, connector)
        assert(input().zero)
        output(ONE)
        connector.update()
        assert(input().one)

        val defaultInput = SysInput("input", top.scheduler, top, null, undefined<SysBit>())
        assert(defaultInput() == undefined<SysBit>())
        defaultInput.bind(connector)
        assert(defaultInput().one)
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
package sysk

import org.junit.Test

class SysSignalTest {

    @Test
    fun boolean() {
        val scheduler = SysScheduler()
        val first = SysSignal("first", false, scheduler)
        assert(!first.value)
        first.value = true
        assert(!first.value)
        first.update()
        assert(first.value)
        val second = SysSignal("second", true, scheduler)
        assert(first.value == second.value)
        second.value = false
        assert(first.value == second.value)
        second.update()
        assert(first.value != second.value)
    }

    @Test
    fun wire() {
        val scheduler = SysScheduler()
        val first = SysWireSignal("first", scheduler, SysBit.X)
        assert(first.x)
        first.value = SysBit.ONE
        assert(first.x)
        first.update()
        assert(first.one)
        first.value = SysBit.ZERO
        assert(first.one)
        first.update()
        assert(first.zero)
        val second = SysSignal("second", SysBit.ZERO, scheduler)
        assert(first.value == second.value)
        second.value = SysBit.ONE
        assert(first.value == second.value)
        second.update()
        assert(first.value != second.value)
    }
}
package sysk

import org.junit.Test

class SysSignalTest {

    @Test
    fun boolean() {
        val first = SysSignal("first", false)
        assert(!first.value)
        first.value = true
        assert(!first.value)
        first.update()
        assert(first.value)
        val second = SysSignal("second", true)
        assert(first == second)
        second.value = false
        assert(first == second)
        second.update()
        assert(first != second)
    }

    @Test
    fun wire() {
        val first = SysWireSignal("first", SysWireState.X)
        assert(first.value.x)
        first.value = SysWireState.ONE
        assert(first.value.x)
        first.update()
        assert(first.value.one)
        val second = SysSignal("second", SysWireState.ONE)
        assert(first == second)
        second.value = SysWireState.ZERO
        assert(first == second)
        second.update()
        assert(first != second)

    }
}
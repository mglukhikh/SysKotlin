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
}
package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.data.SysBit.*

class SysSignalTest {

    @Test
    fun bit() {
        val scheduler = SysScheduler()
        val first = SysBitSignal("first", scheduler, X)
        assert(first.x)
        first.value = ONE
        assert(first.x)
        first.update()
        assert(first.one)
        first.value = ZERO
        assert(first.one)
        first.update()
        assert(first.zero)
        val second = SysSignal("second", ZERO, scheduler)
        assert(first.value == second.value)
        second.value = ONE
        assert(first.value == second.value)
        second.update()
        assert(first.value != second.value)
    }
}
package ru.spbstu.sysk.channels

import org.junit.Test
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke

class SysClockTest {

    @Test
    fun test() {
        val scheduler = SysScheduler()
        val clock = SysClock("clock", 20(NS), scheduler)
        assert(clock.zero)
        scheduler.start(55(NS))
        assert(clock.one)
        scheduler.start(115(NS))
        assert(clock.one)
        scheduler.start(145(NS))
        assert(clock.zero)
    }
}
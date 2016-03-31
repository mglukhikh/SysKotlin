package ru.spbstu.sysk.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SysBaseIntegerTest {


    @Test
    fun test() {

        val a = SysInteger.valueOf(32, 2222222)
        val b = SysInteger.valueOf(100, 2222222)
        assertEquals(b + a, SysBigInteger(100, 4444444))
        assertEquals(a + b, SysBigInteger(100, 4444444))
        assertNotEquals(a + b, SysLongInteger(SysLongInteger.MAX_WIDTH, 4444444))


    }

}
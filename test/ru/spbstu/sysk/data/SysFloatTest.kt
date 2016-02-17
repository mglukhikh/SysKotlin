package ru.spbstu.sysk.data

import org.junit.Test

class SysFloatTest {

    @Test
    fun constructionTest() {
        val a = SysFloat.valueOf(12.34567)
        val b = SysFloat.valueOf(127.55555)
        assert(a.toDouble() == 12.34567)
        assert(b.toDouble() == 127.55555)
    }

}
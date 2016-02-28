package ru.spbstu.sysk.data

import org.junit.Test

class SysFloatTest {

    @Test
    fun constructionTest() {

        val x: Double = 12.34567
        val y: Float = 12.34567f
        val a = SysFloat.valueOf(x)
        val b = SysFloat.valueOf(127.55555)
        val c = SysFloat.valueOf(y)
        assert(x.toFloat() == y)

        assert(y.toDouble() != x)

        assert(a.toDouble() == x)
        assert(c.toFloat() == y)
        assert(b.toDouble() == 127.55555)
    }

    @Test
    fun mathTest() {

        val a = SysFloat.valueOf(0.253)
        val b = SysFloat.valueOf(100.7)
        var c = SysFloat.valueOf(100.953)
        assert(a + b == c)

    }

}
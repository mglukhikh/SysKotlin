package ru.spbstu.sysk.data

import org.junit.Test

class SysFloatTest {

    @Test
    fun constructionTest() {

        val y: Float = 12.34567f
        val c = SysFloat.valueOf(y)
        assert(c.toFloat() == y)

    }

    @Test
    fun mathTest() {

        val af = 0.5f
        val bf = 10.0f
        val cf = 10.5f

        val a = SysFloat.valueOf(af)
        val b = SysFloat.valueOf(bf)
        val c = SysFloat.valueOf(cf)

        assert(af + bf == cf) { af + bf }
        assert(a + b == c) { "plus" }
        assert(cf - bf == af) { cf - bf }
        assert(c - b == a) { "minus" }
        assert(cf - af == bf) { cf - af }
        assert(c - a == b) { "minus" }

        val xf = 23.55f
        val yf = 1.2f
        val zf = 28.26f

        val x = SysFloat.valueOf(xf)
        val y = SysFloat.valueOf(yf)
        val z = SysFloat.valueOf(zf)

        assert(xf * yf == zf) { xf * yf }
        assert(x * y == z) { "mult" }
        assert(zf / yf == xf) { zf / yf }
        assert(z / y == x) { "div" }

        val ef = 2.0f
        val nf = 16.16f
        val mf = 261.1456f

        val e = SysFloat.valueOf(ef)
        val n = SysFloat.valueOf(nf)
        val m = SysFloat.valueOf(mf)

        assert(Math.pow(nf.toDouble(), ef.toDouble()).toFloat() == mf)
        assert(n.power(e) == m) { "power" }
    }

}
package ru.spbstu.sysk.data.floating

import org.junit.Test

class SysDoubleTest {
    @Test
    fun constructionTest() {

        val y: Double = 12.34567
        val c = SysDouble.valueOf(y)
        assert(c.toDouble() == y)

    }

    @Test
    fun mathTest() {

        val af = 0.5
        val bf = 10.0
        val cf = 10.5

        val a = SysDouble.valueOf(af)
        val b = SysDouble.valueOf(bf)
        val c = SysDouble.valueOf(cf)

        assert(af + bf == cf) { af + bf }
        assert(a + b == c) { "plus" }
        assert(cf - bf == af) { cf - bf }
        assert(c - b == a) { "minus" }
        assert(cf - af == bf) { cf - af }
        assert(c - a == b) { "minus" }

        val xf = 23.55
        val yf = 1.2
        val zf = 28.26

        val x = SysDouble.valueOf(xf)
        val y = SysDouble.valueOf(yf)
        val z = SysDouble.valueOf(zf)

        assert(xf * yf == zf) { xf * yf }
        assert(x * y == z) { "mult" }
        assert(zf / yf == xf) { zf / yf }
        assert(z / y == x) { "div" }

        val ef = 2.0
        val nf = 16.16
        val mf = 261.1456

        val e = SysDouble.valueOf(ef)
        val n = SysDouble.valueOf(nf)
        val m = SysDouble.valueOf(mf)

        assert(Math.pow(nf, ef) == mf)
        assert(n.power(e) == m) { "power" }
    }
}
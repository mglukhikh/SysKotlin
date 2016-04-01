package ru.spbstu.sysk.data

import org.junit.Test
import ru.spbstu.sysk.data.SysBit.ONE
import ru.spbstu.sysk.data.SysBit.ZERO

class SysUnsignedTest {
    @Test
    fun constructionTest() {
        SysUnsigned.valueOf(7, 127)
        //println(a.toString())
        //println(java.lang.Long.toBinaryString(a.value))
        SysUnsigned.valueOf(7, 127L)
    }

    @Test
    fun testGet() {

        val x = SysUnsigned.valueOf(10, 128);
        val y = SysUnsigned.valueOf(10, 127);
        val z = SysUnsigned.valueOf(10, 1);

        val arrx = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ZERO, ZERO);
        val arry = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO);
        val arrz = arrayOf(ONE, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);


        for (i in 0..9) {
            assert(arrx[i].equals(x[i])) { "x" + i + " " + x[i] }
            assert(arry[i].equals(y[i])) { "y" + i + " " + y[i] }
            assert(arrz[i].equals(z[i])) { "z" + i + " " + z[i] }

        }

        val a = SysUnsigned.valueOf(arrx);
        val b = SysUnsigned.valueOf(arry);
        val c = SysUnsigned.valueOf(arrz);

        for (i in 0..9) {
            assert(arrx[i].equals(a[i])) { "a" + i + " " + a[i] }
            assert(arry[i].equals(b[i])) { "b" + i + " " + b[i] }
            assert(arrz[i].equals(c[i])) { "c" + i + " " + c[i] }

        }

        assert(a.equals(x))
        assert(b.equals(y))
        assert(c.equals(z))

    }
}
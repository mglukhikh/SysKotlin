package ru.spbstu.sysk.data.integer

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.spbstu.sysk.data.SysBit.*

class SysBigIntegerTest {
    @Test
    fun testGet() {


        val x = SysBigInteger.valueOf(SysLongInteger(10, -128))
        val y = SysBigInteger.valueOf(SysLongInteger(10, 127))
        val z = SysBigInteger.valueOf(SysLongInteger(10, -1))

        val arrx = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ONE, ONE)
        val arry = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO)
        val arrz = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE)


        for (i in 0..9) {
            assert(arrx[i].equals(x[i])) { "x" + i + " " + x[i] }
            assert(arry[i].equals(y[i])) { "y" + i + " " + y[i] }
            assert(arrz[i].equals(z[i])) { "z" + i + " " + z[i] }

        }

        val xx = SysBigInteger.valueOf(SysLongInteger(8, -128))
        val yy = SysBigInteger.valueOf(SysLongInteger(8, 127))
        val zz = SysBigInteger.valueOf(SysLongInteger(1, -1))

        val arrxx = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE)
        val arryy = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO)
        val arrzz = arrayOf(ONE)


        assert(arrzz[0].equals(zz[0])) { "z" + 0 + " " + zz[0] }

        for (i in 0..7) {
            assert(arrxx[i].equals(xx[i])) { "x" + i + " " + xx[i] }
            assert(arryy[i].equals(yy[i])) { "y" + i + " " + yy[i] }

        }

    }

    @Test
    fun testSWSOperations() {

        val arrx = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ONE, ONE)
        val arry = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO)
        val arrz = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE)

        val x = SysBigInteger(arrx)
        val y = SysBigInteger(arry)
        val z = SysBigInteger(arrz)

        for (i in 0..9) {
            assert(arrx[i].equals(x[i])) { "x" + i + " " + x[i] }
            assert(arry[i].equals(y[i])) { "y" + i + " " + y[i] }
            assert(arrz[i].equals(z[i])) { "z" + i + " " + z[i] }

        }

        assertEquals(x, (SysBigInteger(10, -128)))
        assertEquals(y, (SysBigInteger(10, 127)))
        assertEquals(z, (SysBigInteger(10, -1)))


    }

    @Test
    fun testMath() {
        val x: SysBigInteger = SysBigInteger(64, 62000L)
        val y: SysBigInteger = SysBigInteger(32, 128000)
        assert((x + y).equals(SysBigInteger(64, 190000))) { x + y }
        assert((x - y).equals(SysBigInteger(64, -66000))) { x - y }
        assert((x * y).equals(SysBigInteger(64, 7936000000)), { "x * y = ${x * y}" })
        assert((x / y).equals(SysBigInteger(64, 0))) { x / y }
        //assert((y / x).equals(SysInteger(64, 2)));
        //assert((x % y).equals(x));
        assert((x - y).equals(x + (-y)))
        assert((x - y).equals((-y) + x))
        val z = SysBigInteger(32, 62000L)
        assert((y / z).equals(SysBigInteger(32, 2)))
        assert((z % y).equals(z)) { 4 }
        assert((-y).equals(SysBigInteger(32, -128000))) { -y }
    }


    @Test
    fun testLogic() {

        val x: SysBigInteger = SysBigInteger(10, 127)
        val y: SysBigInteger = SysBigInteger(8, 64)


        var arr = arrayOf (ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO)
        var z = SysBigInteger(arr)

        assertEquals((x or y), (z))

        arr = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ZERO, ZERO, ZERO)
        z = SysBigInteger(arr)

        assertEquals((x and y), (z))
        assertEquals((x.inv()), (SysBigInteger(10, -128)))

        arr = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO, ZERO)
        z = SysBigInteger(arr)

        assertEquals(z, x xor y)

    }

    @Test
    fun testSet() {

        val src = arrayOf(ZERO, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ONE, ZERO)
        val x = SysBigInteger.valueOf(src)
        Assert.assertArrayEquals(src, x.bits())


        val singleSetOne = arrayOf(ZERO, ZERO, ZERO, ONE, ZERO, ZERO, ZERO, ONE, ZERO)
        val singleSetTwo = arrayOf(ZERO, ZERO, ZERO, ONE, ONE, ONE, ZERO, ONE, ZERO)
        val singleSetThree = arrayOf(ZERO, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ONE, ZERO)


        val first = x.set(5, ZERO)
        val second = x.set(4, ONE)
        val third = x.set(2, ZERO)

        Assert.assertArrayEquals(singleSetOne, first.bits())
        Assert.assertArrayEquals(singleSetTwo, second.bits())
        Assert.assertArrayEquals(singleSetThree, third.bits())


        val manySetOne = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ZERO)
        val manySetTwo = arrayOf(ZERO, ZERO, ONE, ZERO, ZERO, ONE, ONE, ONE, ZERO)
        val manySetThree = arrayOf(ZERO, ZERO, ONE, ZERO, ONE, ONE, ONE, ONE, ZERO)


        val y = x.set(5, 2, arrayOf(ZERO, ZERO, ZERO, ZERO))
        val z = x.set(6, 2, arrayOf(ONE, ZERO, ZERO, ONE, ONE))
        val c = SysBigInteger.valueOf(arrayOf(ONE, ZERO, ONE, ONE, ONE))
        val d = x.set(6, 2, c)

        Assert.assertArrayEquals(manySetOne, y.bits())

        Assert.assertArrayEquals(manySetTwo, z.bits())

        Assert.assertArrayEquals(manySetThree, d.bits())

    }

    @Test
    fun testShifts() {

        val x = SysBigInteger(arrayOf(X, X, ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE))
        val cshrTest = SysBigInteger(arrayOf(ZERO, ONE, X, X, ONE, ONE, ZERO, ZERO, ONE))//OK
        val cshlTest = SysBigInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE, X, X))//OK
        val shlTest = SysBigInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ZERO))//OK
        val shrTest = SysBigInteger(arrayOf(X, X, X, X, ONE, ONE, ZERO, ZERO, ONE))//OK
        val ushrTest = SysBigInteger(arrayOf(ZERO, ZERO, X, X, ONE, ONE, ZERO, ZERO, ONE)) //OK

        assertEquals(cshrTest, (x cshr 2))
        assertEquals(cshlTest, (x cshl 2))
        assertEquals(shrTest, (x shr 2))
        assertEquals(shlTest, (x shl 2))
        assertEquals(ushrTest, (x ushr 2))
    }


}
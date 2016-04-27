package ru.spbstu.sysk.data.integer

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import ru.spbstu.sysk.data.SysBit.*
import java.util.*

class SysIntegerTest {

    @Test
    fun first() {
        val x: @Width(4) SysLongInteger = SysLongInteger.valueOf(5)
        val yy: @Width(6) SysLongInteger = SysLongInteger.valueOf(9)
        var y = yy.extend(6)
        assert(y.width == 6, { y })
        var z: @Width(6) SysInteger = (x + y).toSysLongInteger()
        assert(z.width == 6)
        assert(z.equals(SysLongInteger(6, 14)), { z })
        assert(z[2] == ONE, { z[2] })
        assertEquals(SysLongInteger(4, 7), z[4, 1])
        y = y.extend(12)
        z = z.extend(12)
        val v: @Width(12) SysInteger = y * z
        assert(v.width == 12)
        assert(v.equals(SysInteger(12, 126)), { v })
        val m = SysLongInteger(0, 0);
        val n = SysLongInteger(32, 0)
        //assert(n[10] == X);
        assert((m + n).equals(SysLongInteger(32, 0)));
        //assert((m + n)[10] == X)
        // println(x.toBitString())
    }

    @Test
    fun testGet() {

        val x = SysLongInteger(10, -128);
        val y = SysLongInteger(10, 127);
        val z = SysLongInteger(10, -1);

        val arrx = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ONE, ONE);
        val arry = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO);
        val arrz = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ONE);


        for (i in 0..9) {
            assert(arrx[i].equals(x[i])) { "x " + i + " " + arrx[i] + " " + x[i] }
            assert(arry[i].equals(y[i])) { "y " + i + " " + arry[i] + " " + y[i] }
            assert(arrz[i].equals(z[i])) { "z " + i + " " + arrz[i] + " " + z[i] }


        }

        val a = SysLongInteger(arrx);
        val b = SysLongInteger(arry);
        val c = SysLongInteger(arrz);

        for (i in 0..9) {
            assert(arrx[i].equals(a[i])) { "a" + i + " " + a[i] }
            assert(arry[i].equals(b[i])) { "b" + i + " " + b[i] }
            assert(arrz[i].equals(c[i])) { "c" + i + " " + c[i] }

        }
        assertEquals(a, x)
        assertEquals(b, y)
        assertEquals(c, z)

    }

    @Test
    fun testMath() {
        val x: SysLongInteger = SysLongInteger(64, 62000L);
        val y: SysLongInteger = SysLongInteger(32, 128000);
        assert((x + y).equals(SysLongInteger(64, 190000)), { x + y });
        assert((x - y).equals(SysLongInteger(64, -66000)));
        assert((x * y).equals(SysLongInteger(64, 7936000000)));
        assert((x / y).equals(SysLongInteger(64, 0)));
        //assert((y / x).equals(SysInteger(64, 2)));
        //assert((x % y).equals(x));
        assert((x - y).equals(x + (-y)))
        assert((x - y).equals((-y) + x))
        val z = SysLongInteger(32, 62000L);
        assert((y / z).equals(SysLongInteger(32, 2)));
        assert((z % y).equals(z));
        assert((-y).equals(SysLongInteger(32, -128000)))
    }

    @Test
    fun testLogic() {

        val x: SysLongInteger = SysLongInteger(10, 127)
        val y: SysLongInteger = SysLongInteger(8, 64)


        var arr = arrayOf (ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO);
        var z = SysLongInteger(arr);

        assertEquals(z, x or y)

        arr = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ZERO, ZERO, ZERO);
        z = SysLongInteger(arr);

        assertEquals(z, x and y)

        assertEquals(SysLongInteger(10, -128), x.inv())

        arr = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO, ZERO, ZERO);
        z = SysLongInteger(arr);

        assertEquals(z, x xor y);
    }

    @Test
    fun testShifts() {
        val x = SysLongInteger(arrayOf(X, X, ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE));
        val cshrTest = SysLongInteger(arrayOf(ZERO, ONE, X, X, ONE, ONE, ZERO, ZERO, ONE));//OK
        val cshlTest = SysLongInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE, X, X));//OK
        val shlTest = SysLongInteger(arrayOf(ONE, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ZERO, ZERO));//OK
        val shrTest = SysLongInteger(arrayOf(X, X, X, X, ONE, ONE, ZERO, ZERO, ONE));//OK
        val ushrTest = SysLongInteger(arrayOf(ZERO, ZERO, X, X, ONE, ONE, ZERO, ZERO, ONE)); //OK

        assertEquals(cshrTest, (x cshr 2))
        assertEquals(cshlTest, (x cshl 2))
        assertEquals(shrTest, (x shr 2))
        assertEquals(shlTest, (x shl 3))
        assertEquals(ushrTest, (x ushr 2))

    }

    @Test
    fun testMinMaxValues() {
        val rcv = SysLongInteger(8, 0)

        val minValue = rcv.negativeMask
        assertEquals(-128L, minValue)

        val maxValue = rcv.positiveMask
        assertEquals(127L, maxValue)

    }


    @Test
    fun testSet() {

        val src = arrayOf(ZERO, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ONE, ZERO)
        val x = SysLongInteger(src)
        assertArrayEquals(src, x.bits())

        val singleSetOne = arrayOf(ZERO, ZERO, ZERO, ONE, ZERO, ZERO, ZERO, ONE, ZERO)
        val singleSetTwo = arrayOf(ZERO, ZERO, ZERO, ONE, ONE, ONE, ZERO, ONE, ZERO)
        val singleSetThree = arrayOf(ZERO, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ONE, ZERO)


        val first = x.set(5, ZERO)
        val second = x.set(4, ONE)
        val third = x.set(2, ZERO)

        assertArrayEquals(singleSetOne, first.bits())
        assertArrayEquals(singleSetTwo, second.bits())
        assertArrayEquals(singleSetThree, third.bits())


        val manySetOne = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ZERO)
        val manySetTwo = arrayOf(ZERO, ZERO, ONE, ZERO, ZERO, ONE, ONE, ONE, ZERO)
        val manySetThree = arrayOf(ZERO, ZERO, ONE, ZERO, ONE, ONE, ONE, ONE, ZERO)


        val y = x.set(5, 2, arrayOf(ZERO, ZERO, ZERO, ZERO))
        val z = x.set(6, 2, arrayOf(ONE, ZERO, ZERO, ONE, ONE))
        val c = SysLongInteger(arrayOf(ONE, ZERO, ONE, ONE, ONE))
        val d = x.set(6, 2, c)

        assertArrayEquals(manySetOne, y.bits())
        assertArrayEquals(manySetTwo, z.bits())
        assertArrayEquals(manySetThree, d.bits())

    }

    @Ignore("Integer bound test need rework")
    @Test
    fun boundTest() {

        var x = SysLongInteger.valueOf(Long.MAX_VALUE)
        var y = SysLongInteger.valueOf(Long.MIN_VALUE)
        assertEquals(x, y.inv())

        assertEquals((x + y).value, Long.MAX_VALUE + Long.MIN_VALUE)
        assertEquals((x - x).value, 0L)

        val width = 52
        var a = 1L shl width - 2
        var b = 1L shl width - 2
        x = SysLongInteger(width, a)
        y = SysLongInteger(width, b)

        val mask = -1L ushr ((64 - width) + 1)
        //        println(
        //                "\n${java.lang.Long.toBinaryString(a)}\n" +
        //                        "${java.lang.Long.toBinaryString(b)}\n" +
        //                        "${java.lang.Long.toBinaryString((a + b))}\n" +
        //                        "${java.lang.Long.toBinaryString((x).value)}\n" +
        //                        "${java.lang.Long.toBinaryString((y).value)}\n" +
        //                        "${java.lang.Long.toBinaryString((x + y).value)}\n"
        //        )
        assert(allBitsEquals(a + b, (x + y).toSysLongInteger(), width - 1)) {
            "\n${java.lang.Long.toBinaryString(a + b)}\n" +
                    "${java.lang.Long.toBinaryString((x + y).value.toLong())}"
        }

        for (i in 0..99) {

            a = Random().nextLong()and mask
            b = Random().nextLong()and mask

            x = SysLongInteger(SysLongInteger.Companion.MAX_WIDTH, a)
            y = SysLongInteger(SysLongInteger.Companion.MAX_WIDTH, b)
            //println("iteration $i test 1")
            assert(allBitsEquals(a * b, x * y, SysLongInteger.MAX_WIDTH)) {
                "\n${java.lang.Long.toBinaryString(a * b)}\n" +
                        "${java.lang.Long.toBinaryString((x * y).value.toLong())}"
            }
            //println("iteration $i test 2")
            x = SysLongInteger(width, a)
            y = SysLongInteger(width, b)
            assert(allBitsEquals(a * b, x * y, width - 1)) {
                "\n${java.lang.Long.toBinaryString(a * b)}\n" +
                        "${java.lang.Long.toBinaryString((x * y).value.toLong())}"
            }

        }
    }

    fun allBitsEquals(left: Long, rightInt: SysInteger, number: Int): Boolean {
        val right = rightInt.value.toLong()
        var mask: Long
        for (i in 0..number - 1) {
            mask = 1L shl i
            if ((left and mask) != (right and mask))
                return false
        }
        return true
    }


}

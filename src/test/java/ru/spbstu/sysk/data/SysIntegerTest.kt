package ru.spbstu.sysk.data

import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import ru.spbstu.sysk.data.SysBit.*
import java.util.*
import kotlin.reflect.declaredFunctions
import kotlin.reflect.jvm.isAccessible

class SysIntegerTest {

    @Test
    fun first() {
        val x: @Width(4) SysInteger = SysInteger.valueOf(5)
        val yy: @Width(6) SysInteger = SysInteger.valueOf(9)
        var y = yy.extend(6)
        assert(y.width == 6, { y })
        var z: @Width(6) SysInteger = x + y
        assert(z.width == 6)
        assert(z.equals(SysInteger(6, 14)), { z })
        assert(z[2] == ONE, { z[2] })
        assert(z[4, 1].equals(SysInteger(4, 7)));
        y = y.extend(12)
        z = z.extend(12)
        val v: @Width(12) SysInteger = y * z
        assert(v.width == 12)
        assert(v.equals(SysInteger(12, 126)), { v })
        val m = SysInteger(0, 0);
        val n = SysInteger(32, 0)
        assert(n[10] == X);
        assert((m + n).equals(SysInteger(32, 0)));
        assert((m + n)[10] == X)
    }

    @Test
    fun testGet() {

        val x = SysInteger(10, -128);
        val y = SysInteger(10, 127);
        val z = SysInteger(10, -1);

        val arrx = arrayOf(X, X, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE);
        val arry = arrayOf(X, X, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO);
        val arrz = arrayOf(X, X, X, X, X, X, X, X, X, ONE);


        for (i in 0..9) {
            assert(arrx[i].equals(x[i])) { "x" + i + " " + x[i] }
            assert(arry[i].equals(y[i])) { "y" + i + " " + y[i] }
            assert(arrz[i].equals(z[i])) { "z" + i + " " + z[i] }

        }

        val a = SysInteger(arrx);
        val b = SysInteger(arry);
        val c = SysInteger(arrz);

        for (i in 0..9) {
            assert(arrx[i].equals(a[i])) { "a" + i + " " + a[i] }
            assert(arry[i].equals(b[i])) { "b" + i + " " + b[i] }
            assert(arrz[i].equals(c[i])) { "c" + i + " " + c[i] }

        }

        assert(a.equals(x))
        assert(b.equals(y))
        assert(c.equals(z))

    }

    @Test
    fun testMath() {
        val x: SysInteger = SysInteger(64, 62000L);
        val y: SysInteger = SysInteger(32, 128000);
        assert((x + y).equals(SysInteger(64, 190000)), { x + y });
        assert((x - y).equals(SysInteger(64, -66000)));
        assert((x * y).equals(SysInteger(64, 7936000000)));
        assert((x / y).equals(SysInteger(64, 0)));
        //assert((y / x).equals(SysInteger(64, 2)));
        //assert((x % y).equals(x));
        assert((x - y).equals(x + (-y)))
        assert((x - y).equals((-y) + x))
        val z = SysInteger(32, 62000L);
        assert((y / z).equals(SysInteger(32, 2)));
        assert((z % y).equals(z));
        assert((-y).equals(SysInteger(32, -128000)))
    }

    @Test
    fun testLogic() {

        val x: SysInteger = SysInteger(10, 127)
        val y: SysInteger = SysInteger(8, 64)


        var arr = arrayOf (X, X, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO);
        var z = SysInteger(arr);

        assert((x or y).equals(z), { x or y });

        arr = arrayOf(X, X, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ZERO);
        z = SysInteger(arr);

        assert((x and y).equals(z));

        assert((x.inv()).equals(SysInteger(10, -128)));

        arr = arrayOf(X, X, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO);
        z = SysInteger(arr);

        assert((x xor y).equals(z));
    }

    @Test
    fun testShifts() {
        val x = SysInteger(arrayOf(X, X, ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE));
        val cshrTest = SysInteger(arrayOf(ZERO, ONE, X, X, ONE, ONE, ZERO, ZERO, ONE));//OK
        val cshlTest = SysInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE, X, X));//OK
        val shlTest = SysInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ZERO));//OK
        val shrTest = SysInteger(arrayOf(ZERO, ZERO, X, X, ONE, ONE, ZERO, ZERO, ONE));//OK
        val ushlTest = SysInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE)); //OK
        val ushrTest = SysInteger(arrayOf(X, X, ONE, ONE, ZERO, ZERO, ONE)); //OK

        assert((x cshr 2).equals(cshrTest))//OK
        assert((x cshl 2).equals(cshlTest))//OK
        assert((x shr 2).equals(shrTest))//OK
        assert((x shl 2).equals(shlTest))//OK
        assert((x ushl 2).equals(ushlTest)) //OK
        assert((x ushr 2).equals(ushrTest)) //OK

    }

    @Ignore("Ignored because of refactoring")
    @Test
    fun testMinMaxValues() {
        val rcv = SysInteger(8, 0)

        val methods = SysInteger::class.declaredFunctions.groupBy { it.name }

        val minValue_ = methods.get("minValue")?.get(0)
        minValue_?.isAccessible = true

        val minValue = minValue_?.call(rcv)
        assertEquals(-128L, minValue)
        val maxValue_ = methods.get("maxValue")?.get(0)
        maxValue_?.isAccessible = true

        val maxValue = maxValue_?.call(rcv)
        assertEquals(127L, maxValue)

    }

    @Test
    fun boundTest() {

        var x = SysInteger.valueOf(Long.MAX_VALUE)
        var y = SysInteger.valueOf(Long.MIN_VALUE)
        assertEquals(x, y.inv())

        assertEquals((x + y).value, Long.MAX_VALUE + Long.MIN_VALUE)
        assertEquals((x - x).value, 0L)

        val width = 52
        var a = 1L shl width - 2
        var b = 1L shl width - 2
        x = SysInteger(width, a)
        y = SysInteger(width, b)

        val mask = -1L ushr ((64 - width) + 1)
        //        println(
        //                "\n${java.lang.Long.toBinaryString(a)}\n" +
        //                        "${java.lang.Long.toBinaryString(b)}\n" +
        //                        "${java.lang.Long.toBinaryString((a + b))}\n" +
        //                        "${java.lang.Long.toBinaryString((x).value)}\n" +
        //                        "${java.lang.Long.toBinaryString((y).value)}\n" +
        //                        "${java.lang.Long.toBinaryString((x + y).value)}\n"
        //        )
        assert(allBitsEquals(a + b, x + y, width - 1)) {
            "\n${java.lang.Long.toBinaryString(a + b)}\n" +
                    "${java.lang.Long.toBinaryString((x + y).value)}"
        }

        for (i in 0..99) {

            a = Random().nextLong()and mask
            b = Random().nextLong()and mask

            x = SysInteger(SysInteger.MAX_WIDTH, a)
            y = SysInteger(SysInteger.MAX_WIDTH, b)
            //println("iteration $i test 1")
            assert(allBitsEquals(a * b, x * y, SysInteger.MAX_WIDTH)) {
                "\n${java.lang.Long.toBinaryString(a * b)}\n" +
                        "${java.lang.Long.toBinaryString((x * y).value)}"
            }
            //println("iteration $i test 2")
            x = SysInteger(width, a)
            y = SysInteger(width, b)
            assert(allBitsEquals(a * b, x * y, width - 1)) {
                "\n${java.lang.Long.toBinaryString(a * b)}\n" +
                        "${java.lang.Long.toBinaryString((x * y).value)}"
            }

        }
    }

    fun allBitsEquals(left: Long, right: SysInteger, number: Int): Boolean {
        var mask = 1L
        for (i in 0..number - 1) {
            mask = 1L shl i
            if ((left and mask) != (right.value and mask))
                return false
        }
        return true
    }


}
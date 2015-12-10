package sysk

import org.junit.Test
import sysk.SysBit.*


public class SysIntegerTest {

    @Test
    fun first() {
        val x: @Width(4) SysInteger = SysInteger.valueOf(5)
        val yy: @Width(6) SysInteger = SysInteger.valueOf(9)
        val y = yy.extend(6)
        assert(y.width == 6, { y })
        val z: @Width(6) SysInteger = x + y
        assert(z.width == 6)
        assert(z.equals(SysInteger(6, 14)), { z })
        assert(z[2] == SysBit.ONE, { z[2] })
        assert(z[4, 1].equals(SysInteger(4, 7)));
        val v: @Width(12) SysInteger = y * z
        assert(v.width == 12)
        assert(v.equals(SysInteger(12, 126)), { v })
        val m = SysInteger(0, 0);
        val n = SysInteger(32, 0)
        assert(n[10] == SysBit.X);
        assert((m + n).equals(SysInteger(32, 0)));
        assert((m + n)[10] == SysBit.X)
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
}
package sysk.data

import org.junit.Test
import sysk.data.SysBit.*

class SysBigIntegerTest {
    @Test
    fun testGet() {


        val x = SysBigInteger.valueOf(SysInteger(10, -128));
        val y = SysBigInteger.valueOf(SysInteger(10, 127));
        val z = SysBigInteger.valueOf(SysInteger(10, -1));

        val arrx = arrayOf(X, X, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE);
        val arry = arrayOf(X, X, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO);
        val arrz = arrayOf(X, X, X, X, X, X, X, X, X, ONE);


        for (i in 0..9) {
            assert(arrx[i].equals(x[i])) { "x" + i + " " + x[i] }
            assert(arry[i].equals(y[i])) { "y" + i + " " + y[i] }
            assert(arrz[i].equals(z[i])) { "z" + i + " " + z[i] }

        }

        val xx = SysBigInteger.valueOf(SysInteger(8, -128));
        val yy = SysBigInteger.valueOf(SysInteger(8, 127));
        val zz = SysBigInteger.valueOf(SysInteger(1, -1));

        val arrxx = arrayOf(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE);
        val arryy = arrayOf(ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO);
        val arrzz = arrayOf(ONE);


        assert(arrzz[0].equals(zz[0])) { "z" + 0 + " " + zz[0] }

        for (i in 0..7) {
            assert(arrxx[i].equals(xx[i])) { "x" + i + " " + xx[i] }
            assert(arryy[i].equals(yy[i])) { "y" + i + " " + yy[i] }

        }

    }

    @Test
    fun testSWSOperations() {

        val arrx = arrayOf(X, X, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE);
        val arry = arrayOf(X, X, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO);
        val arrz = arrayOf(X, X, X, X, X, X, X, X, X, ONE);

        val x = SysBigInteger(arrx);
        val y = SysBigInteger(arry);
        val z = SysBigInteger(arrz);

        for (i in 0..9) {
            assert(arrx[i].equals(x[i])) { "x" + i + " " + x[i] }
            assert(arry[i].equals(y[i])) { "y" + i + " " + y[i] }
            assert(arrz[i].equals(z[i])) { "z" + i + " " + z[i] }

        }

        assert(x.equals(SysBigInteger(10, -128))) { x }
        assert(y.equals(SysBigInteger(10, 127))) { y }
        assert(z.equals(SysBigInteger(10, -1))) { z }


    }

    @Test
    fun testMath() {
        val x: SysBigInteger = SysBigInteger(64, 62000L);
        val y: SysBigInteger = SysBigInteger(32, 128000);
        assert((x + y).equals(SysBigInteger(64, 190000)), { x + y });
        assert((x - y).equals(SysBigInteger(64, -66000)));
        assert((x * y).equals(SysBigInteger(96, 7936000000)));
        assert((x / y).equals(SysBigInteger(64, 0)));
        //assert((y / x).equals(SysInteger(64, 2)));
        //assert((x % y).equals(x));
        assert((x - y).equals(x + (-y)))
        assert((x - y).equals((-y) + x))
        val z = SysBigInteger(32, 62000L);
        assert((y / z).equals(SysBigInteger(32, 2)));
        assert((z % y).equals(z));
        assert((-y).equals(SysBigInteger(32, -128000)))
    }


    @Test
    fun testLogic() {

        val x: SysBigInteger = SysBigInteger(10, 127)
        val y: SysBigInteger = SysBigInteger(8, 64)


        var arr = arrayOf (X, X, ONE, ONE, ONE, ONE, ONE, ONE, ONE, ZERO);
        var z = SysBigInteger(arr);

        assert((x or y).equals(z), { x or y });

        arr = arrayOf(X, X, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ONE, ZERO);
        z = SysBigInteger(arr);

        assert((x and y).equals(z));

        assert((x.inv()).equals(SysBigInteger(10, -128)));

        arr = arrayOf(X, X, ONE, ONE, ONE, ONE, ONE, ONE, ZERO, ZERO);
        z = SysBigInteger(arr);

        assert((x xor y).equals(z));
    }

    @Test
    fun testShifts() {

        val x = SysBigInteger(arrayOf(X, X, ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE));
        val cshrTest = SysBigInteger(arrayOf(ZERO, ONE, X, X, ONE, ONE, ZERO, ZERO, ONE));//OK
        val cshlTest = SysBigInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE, X, X));//OK
        val shlTest = SysBigInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE, ZERO, ZERO));//OK
        val shrTest = SysBigInteger(arrayOf(ZERO, ZERO, X, X, ONE, ONE, ZERO, ZERO, ONE));//OK
        val ushlTest = SysBigInteger(arrayOf(ONE, ONE, ZERO, ZERO, ONE, ZERO, ONE)); //OK
        val ushrTest = SysBigInteger(arrayOf(X, X, ONE, ONE, ZERO, ZERO, ONE)); //OK

        assert((x cshr 2).equals(cshrTest))//OK
        assert((x cshl 2).equals(cshlTest))//OK
        assert((x shr 2).equals(shrTest))//OK
        assert((x shl 2).equals(shlTest))//OK
        assert((x ushl 2).equals(ushlTest)) //OK
        assert((x ushr 2).equals(ushrTest)) //OK

    }
}
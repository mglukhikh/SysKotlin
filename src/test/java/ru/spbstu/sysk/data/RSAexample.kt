package ru.spbstu.sysk.data

import org.junit.Ignore
import org.junit.Test
import ru.spbstu.sysk.data.integer.SysBigInteger
import java.math.BigInteger
import java.util.*

//This example is copy of SystemC example
class RSAexample {
    val NBITS = 250
    val HALF_NBITS = NBITS / 2
    val HALF_STR_SIZE = HALF_NBITS

    fun flip(p: Double) = Random().nextDouble() < p

    // Randomly generate a bit string with nbits bits.  str has a length
    // of nbits + 1. This function is used to generate random messages to
    // process.
    fun randBitStr(str: CharArray, nbits: Int) {
        assert(nbits >= 4);

        str[0] = '0';// Sign for positive numbers.
        str[1] = '1';
        str[2] = '0';

        for (i in  3..str.lastIndex) {
            if (flip(0.5)) {
                str[i] = '1'
            } else {
                str[i] = '0'
            }
        }
    }


    // Return a positive remainder.
    fun positiveRemainder(x: SysBigInteger, n: SysBigInteger): SysBigInteger {
        if ( x.value.signum() < 0 )
            return x + n;
        return x;
    }


    // Compute d, x, and y such that d = gcd( a, b ) = ax + by. x and y can
    // be zero or negative. This algorithm is also Euclid's algorithm but
    // it is extended to also find x and y. Recall that the existence of x
    // and y is guaranteed by Euclid's algorithm.

    //TODO fix this
    data class EuclidData(var a: SysBigInteger,
                          var b: SysBigInteger,
                          var d: SysBigInteger,
                          var x: SysBigInteger,
                          var y: SysBigInteger)

    fun euclid(arg: EuclidData): EuclidData {
        // println("arg $arg"
        if ( arg.b.value != BigInteger.ZERO ) {
            var aa: SysBigInteger = arg.b
            var bb: SysBigInteger = arg.a % arg.b
            var tempResult = euclid(EuclidData(aa, bb, arg.d, arg.x, arg.y))
            // println("temp $tempResult")
            //var tmp = x;
            //x = y
            //y = tmp - ( a / b ) * y;
            return EuclidData(arg.a, arg.b, tempResult.d, tempResult.y, tempResult.x - (arg.a / arg.b) * tempResult.y)
        } else {
            // d = a;
            //x = SysBigInteger.valueOf(1);
            //y = SysBigInteger.valueOf(0);
            return EuclidData(arg.a, arg.b, arg.a, SysBigInteger.valueOf(1), SysBigInteger.valueOf(0))
        }
    }

    // Return the multiplicative inverse of a, modulo n, when a and n are
    // relatively prime. Recall that x is a multiplicative inverse of a,
    // modulo n, if a * x = 1 ( mod n ).
    fun inverse(a: SysBigInteger, n: SysBigInteger): SysBigInteger {
        var d = SysBigInteger(NBITS, BigInteger.ZERO)
        var x = SysBigInteger(NBITS, BigInteger.ZERO)
        var y = SysBigInteger(NBITS, BigInteger.ZERO)
        var result = euclid(EuclidData(a, n, d, x, y))
        assert(result.d.value == BigInteger.ONE) { d };
        //check this!
        var temp = result.x % n;

        return positiveRemainder(temp, n);
    }

    // Find a small odd integer a that is relatively prime to n. I do not
    // know an efficient algorithm to do that but the loop below seems to
    // work; it usually iterates a few times. Recall that a is relatively
    // prime to n if their only common divisor is 1, i.e., gcd( a, n ) ==
    // 1.
    fun findRelPrime(n: SysBigInteger): SysBigInteger {
        var a = BigInteger.valueOf(3);
        val two = BigInteger.valueOf(2)
        while ( true ) {
            if ( a.gcd(n.value) == BigInteger.ONE )
                break;
            a += two
        }
        if (a > n.value)
            throw ArithmeticException("n is not prime")

        return SysBigInteger(NBITS, a);
    }

    // Return d = a^b % n, where ^ represents exponentiation.
    fun modularExp(a: SysBigInteger, b: SysBigInteger, n: SysBigInteger) =
            SysBigInteger(NBITS, a.value.modPow(b.value, n.value))

    fun cipher(msg: SysBigInteger, e: SysBigInteger, n: SysBigInteger) =
            modularExp(msg, e, n);

    // Dencode or decipher the message in msg using the RSA secret key S=( d, n ).
    fun decipher(msg: SysBigInteger, d: SysBigInteger, n: SysBigInteger) =
            modularExp(msg, d, n);

    fun rsa(seed: Long) {

        // val r = SysBigInteger(NBITS, BigInteger.probablePrime(HALF_NBITS, Random(seed)))

        // Find two large primes p and q.
        val p = SysBigInteger(NBITS, BigInteger.probablePrime(HALF_NBITS, Random(seed)));
        val q = SysBigInteger(NBITS, p.value.nextProbablePrime());

        //println("p = $p \nq = $q")

        // Compute n and ( p - 1 ) * ( q - 1 ) = m.
        val n = p * q;
        val m = (p - 1) * ( q - 1 );

        //println("m = $m")

        // Find a small odd integer e that is relatively prime to m.
        val e = findRelPrime(m);

        // Find the multiplicative inverse d of e, modulo m.
        val d = inverse(e, m);

        // Output public and secret keys.
        println("RSA public key P: P=( e, n )")
        println("e = " + e)
        println("n = " + n)

        println("RSA secret key S: S=( d, n )")
        println("d = " + d)
        println("n = " + n)


        // Cipher and decipher a randomly generated message msg.
        var msgStr = CharArray(HALF_STR_SIZE)
        randBitStr(msgStr, HALF_STR_SIZE);
        var msg = SysBigInteger(NBITS, BigInteger(String(msgStr), 2))

        msg %= n; // Make sure msg is smaller than n. If larger, this part
        // will be a block of the input message.


        println("Message to be ciphered = ")
        println(msg)

        var msg2 = cipher(msg, e, n);
        println("\nCiphered message = ")
        println(msg2)

        msg2 = decipher(msg2, d, n);
        println("\nDeciphered message = ")
        println(msg2)

        // Make sure that the original message is recovered.
        if ( msg == msg2 ) {
            println("\nNote that the original message == the deciphered message, ")
            println("showing that this algorithm and implementation work correctly.\n")
        } else {
            // This case is unlikely.
            println("\nNote that the original message != the deciphered message, ")
            println("showing that this implementation works incorrectly.\n")
        }
    }

    @Ignore("Ignored Example")
    @Test
    fun start() {
        rsa(-1)
    }
}


package ru.spbstu.sysk.samples.examples

import org.junit.Ignore
import org.junit.Test
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.integer
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
        assert(nbits >= 4)

        str[0] = '0'// Sign for positive numbers.
        str[1] = '1'
        str[2] = '0'

        for (i in 3..str.lastIndex) {
            if (flip(0.5)) {
                str[i] = '1'
            } else {
                str[i] = '0'
            }
        }
    }


    // Return a positive remainder.
    fun positiveRemainder(x: SysInteger, n: SysInteger): SysInteger {
        if (x.value < 0)
            return x + n
        return x
    }


    // Compute d, x, and y such that d = gcd( a, b ) = ax + by. x and y can
    // be zero or negative. This algorithm is also Euclid's algorithm but
    // it is extended to also find x and y. Recall that the existence of x
    // and y is guaranteed by Euclid's algorithm.

    //TODO fix this
    data class EuclidData(var a: SysInteger,
                          var b: SysInteger,
                          var d: SysInteger,
                          var x: SysInteger,
                          var y: SysInteger)

    fun euclid(arg: EuclidData): EuclidData {
        // println("arg $arg"
        if (arg.b.value != BigInteger.ZERO) {
            val aa: SysInteger = arg.b
            val bb: SysInteger = arg.a % arg.b
            val tempResult = euclid(EuclidData(aa, bb, arg.d, arg.x, arg.y))
            // println("temp $tempResult")
            //var tmp = x;
            //x = y
            //y = tmp - ( a / b ) * y;
            return EuclidData(arg.a, arg.b, tempResult.d, tempResult.y, tempResult.x - (arg.a / arg.b) * tempResult.y)
        } else {
            // d = a;
            //x = SysInteger.valueOf(1);
            //y = SysInteger.valueOf(0);
            return EuclidData(arg.a, arg.b, arg.a, integer(1), integer(0))
        }
    }

    // Return the multiplicative inverse of a, modulo n, when a and n are
    // relatively prime. Recall that x is a multiplicative inverse of a,
    // modulo n, if a * x = 1 ( mod n ).
    fun inverse(a: SysInteger, n: SysInteger): SysInteger {
        val d = SysInteger.valueOf(NBITS, BigInteger.ZERO)
        val x = SysInteger.valueOf(NBITS, BigInteger.ZERO)
        val y = SysInteger.valueOf(NBITS, BigInteger.ZERO)
        val result = euclid(EuclidData(a, n, d, x, y))
        assert(result.d.value == BigInteger.ONE) { d }
        //check this!
        val temp = result.x % n

        return positiveRemainder(temp, n)
    }

    // Find a small odd integer a that is relatively prime to n. I do not
    // know an efficient algorithm to do that but the loop below seems to
    // work; it usually iterates a few times. Recall that a is relatively
    // prime to n if their only common divisor is 1, i.e., gcd( a, n ) ==
    // 1.
    fun findRelPrime(n: SysInteger): SysInteger {
        var a = BigInteger.valueOf(3)
        val two = BigInteger.valueOf(2)
        while (true) {
            if (a.gcd(n.value.toBInt().value) == BigInteger.ONE)
                break
            a += two
        }
        if (a > n.value.toBInt().value)
            throw ArithmeticException("n is not prime")

        return SysInteger.valueOf(NBITS, a)
    }

    // Return d = a^b % n, where ^ represents exponentiation.
    fun modularExp(a: SysInteger, b: SysInteger, n: SysInteger) =
            SysInteger.valueOf(NBITS, a.value.toBInt().value.modPow(
                    b.value.toBInt().value,
                    n.value.toBInt().value))

    fun cipher(msg: SysInteger, e: SysInteger, n: SysInteger) =
            modularExp(msg, e, n)

    // Dencode or decipher the message in msg using the RSA secret key S=( d, n ).
    fun decipher(msg: SysInteger, d: SysInteger, n: SysInteger) =
            modularExp(msg, d, n)

    fun rsa(seed: Long) {

        // val r = SysInteger(NBITS, BigInteger.probablePrime(HALF_NBITS, Random(seed)))

        // Find two large primes p and q.
        val prime = BigInteger.probablePrime(HALF_NBITS, Random(seed))
        val p = SysInteger.valueOf(NBITS, prime)
        val q = SysInteger.valueOf(NBITS, prime.nextProbablePrime())

        //println("p = $p \nq = $q")

        // Compute n and ( p - 1 ) * ( q - 1 ) = m.
        val n = p * q
        val m = (p - 1) * (q - 1)

        //println("m = $m")

        // Find a small odd integer e that is relatively prime to m.
        val e = findRelPrime(m)

        // Find the multiplicative inverse d of e, modulo m.
        val d = inverse(e, m)

        // Output public and secret keys.
        println("RSA public key P: P=( e, n )")
        println("e = " + e)
        println("n = " + n)

        println("RSA secret key S: S=( d, n )")
        println("d = " + d)
        println("n = " + n)


        // Cipher and decipher a randomly generated message msg.
        val msgStr = CharArray(HALF_STR_SIZE)
        randBitStr(msgStr, HALF_STR_SIZE)
        var msg = SysInteger.valueOf(NBITS, BigInteger(String(msgStr), 2))

        msg %= n // Make sure msg is smaller than n. If larger, this part
        // will be a block of the input message.


        println("Message to be ciphered = ")
        println(msg)

        var msg2 = cipher(msg, e, n)
        println("\nCiphered message = ")
        println(msg2)

        msg2 = decipher(msg2, d, n)
        println("\nDeciphered message = ")
        println(msg2)

        // Make sure that the original message is recovered.
        if (msg == msg2) {
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


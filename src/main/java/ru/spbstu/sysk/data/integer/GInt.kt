package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import java.math.BigInteger

sealed class GInt : Comparable<GInt> {
    abstract val value: Number
    abstract operator fun plus(arg: GInt): GInt
    abstract operator fun minus(arg: GInt): GInt
    abstract operator fun times(arg: GInt): GInt
    abstract operator fun div(arg: GInt): GInt
    abstract operator fun mod(arg: GInt): GInt
    abstract operator fun unaryMinus(): GInt

    abstract operator fun plus(arg: Int): GInt
    abstract operator fun minus(arg: Int): GInt
    abstract operator fun times(arg: Int): GInt
    abstract operator fun div(arg: Int): GInt
    abstract operator fun mod(arg: Int): GInt

    abstract operator fun plus(arg: Long): GInt
    abstract operator fun minus(arg: Long): GInt
    abstract operator fun times(arg: Long): GInt
    abstract operator fun div(arg: Long): GInt
    abstract operator fun mod(arg: Long): GInt

    abstract operator fun inc(): GInt
    abstract operator fun dec(): GInt

    abstract infix fun shl(shift: Int): GInt
    abstract infix fun shr(shift: Int): GInt
    abstract infix fun ushr(shift: Int): GInt
    abstract infix fun cshl(shift: Int): GInt
    abstract infix fun cshr(shift: Int): GInt

    abstract infix fun and(arg: GInt): GInt
    abstract infix fun or(arg: GInt): GInt
    abstract infix fun xor(arg: GInt): GInt
    abstract fun inv(): GInt

    abstract fun testBit(pos: Int): Boolean
    abstract fun setBit(bit: SysBit, i: Int): GInt

    abstract infix fun eq(arg: Long): Boolean
    abstract infix fun greater(arg: Long): Boolean

    abstract fun toLInt(): LInt
    abstract fun toBInt(): BInt

    protected fun Long.toBInt() = BigInteger.valueOf(this)
    protected fun Int.toBInt() = BigInteger.valueOf(this.toLong())

    protected fun BigInteger.customUSHR(shift: Int): BigInteger {
        if (this >= BigInteger.ZERO)
            return this.shiftRight(shift)
        var mask = BigInteger.ZERO
        for (i in this.bitLength() - shift downTo 0)
            mask = mask.setBit(i)
        return this.shiftRight(shift).and(mask)
    }

    class BInt(override val value: BigInteger) : GInt() {
        override fun plus(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value + arg.value.toBInt())
            is GInt.BInt -> BInt(value + arg.value)
            is GInt.UInt -> BInt(value + arg.value.toBInt())
        }

        override fun minus(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value - arg.value.toBInt())
            is GInt.BInt -> BInt(value - arg.value)
            is GInt.UInt -> BInt(value - arg.value.toBInt())
        }

        override fun times(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value * arg.value.toBInt())
            is GInt.BInt -> BInt(value * arg.value)
            is GInt.UInt -> BInt(value * arg.value.toBInt())
        }

        override fun div(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value / arg.value.toBInt())
            is GInt.BInt -> BInt(value / arg.value)
            is GInt.UInt -> BInt(value / arg.value.toBInt())
        }

        override fun mod(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value % arg.value.toBInt())
            is GInt.BInt -> BInt(value % arg.value)
            is GInt.UInt -> BInt(value % arg.value.toBInt())
        }

        override fun unaryMinus() = BInt(value.unaryMinus())

        override fun plus(arg: Int) = BInt(value + arg.toBInt())
        override fun minus(arg: Int) = BInt(value - arg.toBInt())
        override fun times(arg: Int) = BInt(value * arg.toBInt())
        override fun div(arg: Int) = BInt(value / arg.toBInt())
        override fun mod(arg: Int) = BInt(value % arg.toBInt())

        override fun plus(arg: Long) = BInt(value + arg.toBInt())
        override fun minus(arg: Long) = BInt(value - arg.toBInt())
        override fun times(arg: Long) = BInt(value * arg.toBInt())
        override fun div(arg: Long) = BInt(value / arg.toBInt())
        override fun mod(arg: Long) = BInt(value % arg.toBInt())

        override fun inc() = BInt(value + BigInteger.ONE)
        override fun dec() = BInt(value - BigInteger.ONE)

        override fun shl(shift: Int) = BInt(value.shiftLeft(shift))
        override fun shr(shift: Int) = BInt(value.shiftRight(shift))
        override fun ushr(shift: Int) = BInt(value.customUSHR(shift))
        override fun cshl(shift: Int) = (this shl shift) or (this ushr -shift)
        override fun cshr(shift: Int) = (this ushr (shift)) or (this shl -shift)

        override fun and(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value.and(arg.value.toBInt()))
            is GInt.BInt -> BInt(value.and(arg.value))
            is GInt.UInt -> BInt(value.and(arg.value.toBInt()))
        }

        override fun or(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value.or(arg.value.toBInt()))
            is GInt.BInt -> BInt(value.or(arg.value))
            is GInt.UInt -> BInt(value.or(arg.value.toBInt()))
        }

        override fun xor(arg: GInt) = when (arg) {
            is GInt.LInt -> BInt(value.xor(arg.value.toBInt()))
            is GInt.BInt -> BInt(value.xor(arg.value))
            is GInt.UInt -> BInt(value.xor(arg.value.toBInt()))
        }

        override fun inv() = BInt(value.not())

        override fun testBit(pos: Int) = value.testBit(pos)

        override fun setBit(bit: SysBit, i: Int)
                = BInt(if (bit == SysBit.ONE) value.setBit(i) else value.clearBit(i))

        override fun compareTo(other: GInt) = when (other) {
            is BInt -> value.compareTo(other.value)
            is LInt -> value.compareTo(other.value.toBInt())
            is UInt -> value.compareTo(other.value.toBInt())
        }

        override fun toLInt() = LInt(value.longValueExact())
        override fun toBInt() = this

        override fun eq(arg: Long) = value == arg.toBInt()
        override fun greater(arg: Long) = value > arg.toBInt()
    }

    class LInt(override val value: Long) : GInt() {

        override fun plus(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value + arg.value)
            is GInt.BInt -> BInt(value.toBInt() + arg.value)
            is GInt.UInt -> LInt(value + arg.value)
        }

        override fun minus(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value - arg.value)
            is GInt.BInt -> BInt(value.toBInt() - arg.value)
            is GInt.UInt -> LInt(value - arg.value)
        }

        override fun times(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value * arg.value)
            is GInt.BInt -> BInt(value.toBInt() * arg.value)
            is GInt.UInt -> LInt(value * arg.value)
        }

        override fun div(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value / arg.value)
            is GInt.BInt -> BInt(value.toBInt() / arg.value)
        //  is GInt.UInt -> LInt(java.lang.Long.divideUnsigned(value, arg.value))
            is GInt.UInt -> LInt(value / arg.value)
        }

        override fun mod(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value % arg.value)
            is GInt.BInt -> BInt(value.toBInt() % arg.value)
        //  is GInt.UInt -> LInt(java.lang.Long.remainderUnsigned(value, arg.value))
            is GInt.UInt -> LInt(value % arg.value)
        }

        override fun unaryMinus() = LInt(value.unaryMinus())

        override fun plus(arg: Int) = LInt(value + arg)
        override fun minus(arg: Int) = LInt(value - arg)
        override fun times(arg: Int) = LInt(value * arg)
        override fun div(arg: Int) = LInt(value / arg)
        override fun mod(arg: Int) = LInt(value % arg)

        override fun plus(arg: Long) = LInt(value + arg)
        override fun minus(arg: Long) = LInt(value - arg)
        override fun times(arg: Long) = LInt(value * arg)
        override fun div(arg: Long) = LInt(value / arg)
        override fun mod(arg: Long) = LInt(value % arg)

        override fun inc() = LInt(value.inc())
        override fun dec() = LInt(value.dec())

        override fun shl(shift: Int) = LInt(value shl shift)
        override fun shr(shift: Int) = LInt(value shr shift)
        override fun ushr(shift: Int) = LInt(value ushr shift)
        override fun cshl(shift: Int) = LInt(java.lang.Long.rotateLeft(value, shift))
        override fun cshr(shift: Int) = LInt(java.lang.Long.rotateRight(value, shift))

        override fun and(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value and arg.value)
            is GInt.BInt -> BInt(value.toBInt().and(arg.value))
            is GInt.UInt -> LInt(value and arg.value)
        }

        override fun or(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value or arg.value)
            is GInt.BInt -> BInt(value.toBInt().or(arg.value))
            is GInt.UInt -> LInt(value and arg.value)
        }

        override fun xor(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value xor arg.value)
            is GInt.BInt -> BInt(value.toBInt().xor(arg.value))
            is GInt.UInt -> LInt(value and arg.value)
        }

        override fun inv() = LInt(value.inv())

        override fun testBit(pos: Int) = ((value and (1L shl pos)) != 0L)

        override fun setBit(bit: SysBit, i: Int)
                = LInt(if (bit == SysBit.ONE) value or (1L shl i) else value and ((1L shl i).inv()))

        override fun toLInt() = this
        override fun toBInt() = BInt(value.toBInt())

        override fun eq(arg: Long) = value == arg
        override fun greater(arg: Long) = value > arg

        override fun compareTo(other: GInt) = when (other) {
            is BInt -> value.toBInt().compareTo(other.value)
            is LInt -> value.compareTo(other.value)
            is UInt -> value.compareTo(other.value)
        }

    }

    class UInt(override val value: Long) : GInt() {

        infix fun and(arg: UInt) = UInt(value and arg.value)
        infix fun or(arg: UInt) = UInt(value or arg.value)
        infix fun xor(arg: UInt) = UInt(value xor arg.value)

        operator fun plus(arg: UInt) = UInt(value + arg.value)
        operator fun minus(arg: UInt) = UInt(value - arg.value)
        operator fun times(arg: UInt) = UInt(value * arg.value)
        operator fun div(arg: UInt) = UInt(java.lang.Long.divideUnsigned(value, arg.value))
        operator fun mod(arg: UInt) = UInt(java.lang.Long.remainderUnsigned(value, arg.value))


        override fun plus(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value + arg.value)
            is GInt.BInt -> BInt(value.toBInt() + arg.value)
            is GInt.UInt -> LInt(value + arg.value)
        }

        override fun minus(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value - arg.value)
            is GInt.BInt -> BInt(value.toBInt() - arg.value)
            is GInt.UInt -> LInt(value - arg.value)
        }

        override fun times(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value * arg.value)
            is GInt.BInt -> BInt(value.toBInt() * arg.value)
            is GInt.UInt -> LInt(value * arg.value)
        }

        override fun div(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value / arg.value)
            is GInt.BInt -> BInt(value.toBInt() / arg.value)
            is GInt.UInt -> UInt(java.lang.Long.divideUnsigned(value, arg.value))
        }

        override fun mod(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value % arg.value)
            is GInt.BInt -> BInt(value.toBInt() % arg.value)
            is GInt.UInt -> UInt(java.lang.Long.remainderUnsigned(value, arg.value))
        }

        override fun unaryMinus() = LInt(value.unaryMinus())

        override fun plus(arg: Int) = UInt(value + arg)
        override fun minus(arg: Int) = UInt(value - arg)
        override fun times(arg: Int) = UInt(value * arg)
        override fun div(arg: Int) = UInt(java.lang.Long.divideUnsigned(value, arg.toLong()))
        override fun mod(arg: Int) = UInt(java.lang.Long.remainderUnsigned(value, arg.toLong()))

        override fun plus(arg: Long) = UInt(value + arg)
        override fun minus(arg: Long) = UInt(value - arg)
        override fun times(arg: Long) = UInt(value * arg)
        override fun div(arg: Long) = UInt(java.lang.Long.divideUnsigned(value, arg))
        override fun mod(arg: Long) = UInt(java.lang.Long.remainderUnsigned(value, arg))

        override fun inc() = UInt(value.inc())
        override fun dec() = UInt(value.dec())

        override fun shl(shift: Int) = UInt(value shl shift)
        override fun shr(shift: Int) = UInt(value shr shift)
        override fun ushr(shift: Int) = UInt(value ushr shift)
        override fun cshl(shift: Int) = UInt(java.lang.Long.rotateLeft(value, shift))
        override fun cshr(shift: Int) = UInt(java.lang.Long.rotateRight(value, shift))

        override fun and(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value and arg.value)
            is GInt.BInt -> BInt(value.toBInt().and(arg.value))
            is GInt.UInt -> UInt(value and arg.value)
        }

        override fun or(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value or arg.value)
            is GInt.BInt -> BInt(value.toBInt().or(arg.value))
            is GInt.UInt -> UInt(value and arg.value)
        }

        override fun xor(arg: GInt) = when (arg) {
            is GInt.LInt -> LInt(value xor arg.value)
            is GInt.BInt -> BInt(value.toBInt().xor(arg.value))
            is GInt.UInt -> UInt(value and arg.value)
        }

        override fun inv() = UInt(value.inv())

        override fun testBit(pos: Int) = ((value and (1L shl pos)) != 0L)

        override fun setBit(bit: SysBit, i: Int)
                = UInt(if (bit == SysBit.ONE) value or (1L shl i) else value and ((1L shl i).inv()))

        override fun toLInt() = LInt(value)
        override fun toBInt() = BInt(value.toBInt())

        override fun eq(arg: Long) = value == arg
        override fun greater(arg: Long) = value > arg

        override fun compareTo(other: GInt) = when (other) {
            is BInt -> value.toBInt().compareTo(other.value)
            is LInt -> value.compareTo(other.value)
            is UInt -> value.compareTo(other.value)
        }


    }

}


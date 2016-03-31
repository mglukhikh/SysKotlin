package ru.spbstu.sysk.data

import java.math.BigInteger
import java.util.*

abstract class SysInteger protected constructor(
        val width: Int,
        open val value: Number,
        protected val bitsState: Array<Boolean>,
        protected open val positiveMask: Number,
        protected open val negativeMask: Number
) : SysData, Comparable<SysInteger> {

    abstract fun extend(width: Int): SysInteger
    abstract fun truncate(width: Int): SysInteger

    abstract operator fun plus(arg: SysInteger): SysInteger
    abstract operator fun minus(arg: SysInteger): SysInteger
    abstract operator fun times(arg: SysInteger): SysInteger
    abstract operator fun div(arg: SysInteger): SysInteger
    abstract operator fun mod(arg: SysInteger): SysInteger
    abstract operator fun unaryMinus(): SysInteger

    abstract operator fun plus(arg: Int): SysInteger
    abstract operator fun plus(arg: Long): SysInteger
    abstract operator fun minus(arg: Int): SysInteger
    abstract operator fun minus(arg: Long): SysInteger
    abstract operator fun times(arg: Int): SysInteger
    abstract operator fun times(arg: Long): SysInteger
    abstract operator fun div(arg: Int): SysInteger
    abstract operator fun div(arg: Long): SysInteger
    abstract operator fun mod(arg: Int): SysInteger
    abstract operator fun mod(arg: Long): SysInteger
    abstract operator fun inc(): SysInteger
    abstract operator fun dec(): SysInteger

    abstract fun power(exp: Int): SysInteger
    abstract fun abs(): SysInteger

    abstract operator fun get(i: Int): SysBit
    abstract operator fun get(j: Int, i: Int): SysInteger

    abstract fun inv(): SysInteger
    abstract infix fun and(arg: SysInteger): SysInteger
    abstract infix fun or(arg: SysInteger): SysInteger
    abstract infix fun xor(arg: SysInteger): SysInteger

    abstract infix fun shl(shift: Int): SysInteger
    abstract infix fun shr(shift: Int): SysInteger
    abstract infix fun ushl(shift: Int): SysInteger
    abstract infix fun ushr(shift: Int): SysInteger
    abstract infix fun cshl(shift: Int): SysInteger
    abstract infix fun cshr(shift: Int): SysInteger

    abstract fun toSysInteger(): SysLongInteger
    abstract fun toSysBigInteger(): SysBigInteger
    abstract fun toInt(): Int
    abstract fun toLong(): Long

    override abstract fun compareTo(other: SysInteger): Int

    companion object : SysDataCompanion<SysInteger> {

        fun uninitialized(width: Int): SysInteger {
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger.uninitialized(width)
            else
                return SysBigInteger.uninitialized(width)
        }

        operator fun invoke(width: Int, value: Number) = valueOf(width, value)
        operator fun invoke(arr: Array<SysBit>) = valueOf(arr)

        fun valueOf(value: Number): SysInteger {
            if (value is BigInteger)
                return SysBigInteger.valueOf(value)
            else
                return SysLongInteger.valueOf(value.toLong())
        }


        fun valueOf(width: Int, value: Number): SysInteger {
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(width, value.toLong())
            else
                if (value is BigInteger)
                    return SysBigInteger(width, value)
                else
                    return SysBigInteger(width, value.toLong())
        }

        fun valueOf(array: Array<SysBit>): SysInteger {
            if (array.size <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(array)
            else
                return SysBigInteger(array)
        }

        override val undefined: SysInteger
            get() = SysInteger.valueOf(0)

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SysInteger) return false

        if (width != other.width) return false
        if (value != other.value) return false
        if (!Arrays.equals(bitsState, other.bitsState)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result += 31 * result + value.hashCode()
        result += 31 * result + Arrays.hashCode(bitsState)
        return result
    }

    override fun toString(): String {
        return "$value [$width]"
    }


}


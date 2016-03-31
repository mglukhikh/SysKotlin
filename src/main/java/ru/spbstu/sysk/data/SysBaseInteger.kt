package ru.spbstu.sysk.data

import java.math.BigInteger
import java.util.*

abstract class SysBaseInteger protected constructor(
        val width: Int,
        open val value: Number,
        protected val bitsState: Array<Boolean>,
        protected open val positiveMask: Number,
        protected open val negativeMask: Number
) : SysData, Comparable<SysBaseInteger> {


    abstract fun extend(width: Int): SysBaseInteger
    abstract fun truncate(width: Int): SysBaseInteger

    abstract operator fun plus(arg: SysBaseInteger): SysBaseInteger
    abstract operator fun minus(arg: SysBaseInteger): SysBaseInteger
    abstract operator fun times(arg: SysBaseInteger): SysBaseInteger
    abstract operator fun div(arg: SysBaseInteger): SysBaseInteger
    abstract operator fun mod(arg: SysBaseInteger): SysBaseInteger
    abstract operator fun unaryMinus(): SysBaseInteger

    abstract operator fun plus(arg: Int): SysBaseInteger
    abstract operator fun plus(arg: Long): SysBaseInteger
    abstract operator fun minus(arg: Int): SysBaseInteger
    abstract operator fun minus(arg: Long): SysBaseInteger
    abstract operator fun times(arg: Int): SysBaseInteger
    abstract operator fun times(arg: Long): SysBaseInteger
    abstract operator fun div(arg: Int): SysBaseInteger
    abstract operator fun div(arg: Long): SysBaseInteger
    abstract operator fun mod(arg: Int): SysBaseInteger
    abstract operator fun mod(arg: Long): SysBaseInteger
    abstract operator fun inc(): SysBaseInteger
    abstract operator fun dec(): SysBaseInteger

    abstract fun power(exp: Int): SysBaseInteger
    abstract fun abs(): SysBaseInteger

    abstract operator fun get(i: Int): SysBit
    abstract operator fun get(j: Int, i: Int): SysBaseInteger

    abstract fun inv(): SysBaseInteger
    abstract infix fun and(arg: SysBaseInteger): SysBaseInteger
    abstract infix fun or(arg: SysBaseInteger): SysBaseInteger
    abstract infix fun xor(arg: SysBaseInteger): SysBaseInteger

    abstract infix fun shl(shift: Int): SysBaseInteger
    abstract infix fun shr(shift: Int): SysBaseInteger
    abstract infix fun ushl(shift: Int): SysBaseInteger
    abstract infix fun ushr(shift: Int): SysBaseInteger
    abstract infix fun cshl(shift: Int): SysBaseInteger
    abstract infix fun cshr(shift: Int): SysBaseInteger

    abstract fun toSysInteger(): SysLongInteger
    abstract fun toSysBigInteger(): SysBigInteger
    abstract fun toInt(): Int
    abstract fun toLong(): Long

    override abstract fun compareTo(other: SysBaseInteger): Int

    companion object : SysDataCompanion<SysBaseInteger> {

        fun uninitialized(width: Int): SysBaseInteger {
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger.uninitialized(width)
            else
                return SysBigInteger.uninitialized(width)
        }

        fun valueOf(value: Number): SysBaseInteger {
            if (value is BigInteger)
                return SysBigInteger.valueOf(value)
            else
                return SysLongInteger.valueOf(value.toLong())
        }

        fun valueOf(width: Int, value: Number): SysBaseInteger {
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(width, value.toLong())
            else
                if (value is BigInteger)
                    return SysBigInteger(width, value)
                else
                    return SysBigInteger(width, value.toLong())
        }

        fun valueOf(array: Array<SysBit>): SysBaseInteger {
            if (array.size <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(array)
            else
                return SysBigInteger(array)
        }

        override val undefined: SysBaseInteger
            get() = SysBaseInteger.valueOf(0)

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SysBaseInteger) return false

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


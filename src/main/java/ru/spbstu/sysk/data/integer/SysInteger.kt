package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysDataCompanion
import java.math.BigInteger
import java.util.*

abstract class SysInteger protected constructor(
        val width: Int,
        open val value: Number,
        internal val hasUndefined: Boolean,
        internal open val positiveMask: Number,
        internal open val negativeMask: Number
) : SysData, Comparable<SysInteger> {

    internal abstract var bitsState: Array<SysBit>

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
    abstract operator fun set(i: Int, bit: SysBit): SysInteger
    abstract operator fun set(j: Int, i: Int, bits: Array<SysBit>): SysInteger
    abstract operator fun set(j: Int, i: Int, arg: SysInteger): SysInteger

    abstract fun inv(): SysInteger
    abstract infix fun and(arg: SysInteger): SysInteger
    abstract infix fun or(arg: SysInteger): SysInteger
    abstract infix fun xor(arg: SysInteger): SysInteger

    abstract infix fun shl(shift: Int): SysInteger
    abstract infix fun shr(shift: Int): SysInteger
    abstract infix fun ushr(shift: Int): SysInteger
    abstract infix fun cshl(shift: Int): SysInteger
    abstract infix fun cshr(shift: Int): SysInteger

    abstract fun toSysLongInteger(): SysLongInteger
    abstract fun toSysBigInteger(): SysBigInteger
    abstract fun toInt(): Int
    abstract fun toLong(): Long

    abstract fun bits(): Array<SysBit>

    fun toBitString(): String {
        val builder = StringBuilder()
        val bits = bits()
        for (i in bits.indices) {
            when (bits[i]) {
                SysBit.X -> builder.append("X")
                SysBit.Z -> builder.append("Z")
                SysBit.ONE -> builder.append("1")
                SysBit.ZERO -> builder.append("0")
            }
        }
        return builder.toString()
    }

    override abstract fun compareTo(other: SysInteger): Int

    companion object : SysDataCompanion<SysInteger> {

        fun uninitialized(width: Int): SysInteger {
            val initializer = Array(width, { i -> SysBit.X })
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(initializer)
            else
                return SysBigInteger(initializer)
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
            get() = Companion.valueOf(0)

    }


    override fun toString(): String {
        return "$value [$width] ${if (hasUndefined) Arrays.toString(bitsState) else ""}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SysInteger) return false

        if (width != other.width) return false
        if (value != other.value) return false
        if (hasUndefined != other.hasUndefined) return false
        if (positiveMask != other.positiveMask) return false
        if (negativeMask != other.negativeMask) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result += 31 * result + value.hashCode()
        result += 31 * result + hasUndefined.hashCode()
        result += 31 * result + positiveMask.hashCode()
        result += 31 * result + negativeMask.hashCode()
        return result
    }
}

fun unsigned(width: Int, value: Int) = SysUnsigned.valueOf(width, value)

fun unsigned(width: Int, value: Long) = SysUnsigned.valueOf(width, value)

fun integer(value: Int) = SysInteger.valueOf(value.toLong())

fun integer(value: Long) = SysInteger.valueOf(value)

fun integer(width: Int, value: Int) = SysInteger.valueOf(width, value)

fun integer(width: Int, value: Long) = SysInteger.valueOf(width, value)

fun integer(width: Int, value: Number) = SysInteger.valueOf(width, value.toLong())

fun integer(bits: Array<SysBit>) = SysInteger.valueOf(bits)
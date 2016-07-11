package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysDataCompanion
import java.math.BigInteger

abstract class SysInteger(
        val width: Int,
        open val value: GInt,
        internal val hasUndefined: Boolean,
        internal open val positiveMask: GInt,
        internal open val negativeMask: GInt
) : SysData, Comparable<SysInteger> {
    internal abstract var bitsState: Array<SysBit>
    abstract fun extend(width: Int): SysInteger
    abstract fun truncate(width: Int): SysInteger

    protected fun truncate(width: Int, value: GInt, positiveMask: GInt, negativeMask: GInt): SysInteger {
        if (value > positiveMask)
            return construct(width, value or negativeMask, positiveMask, negativeMask)
        if (value < negativeMask)
            return construct(width, positiveMask + (value + positiveMask), positiveMask, negativeMask)
        return construct(width, value, positiveMask, negativeMask)
    }

    operator fun plus(arg: SysInteger) = calc(arg, GInt::plus)
    operator fun minus(arg: SysInteger) = calc(arg, GInt::minus)
    operator fun times(arg: SysInteger) = calc(arg, GInt::times)
    operator fun div(arg: SysInteger) = calc(arg, GInt::div)
    operator fun mod(arg: SysInteger) = calc(arg, GInt::mod)


    private fun calc(result: GInt) = truncate(width, result, positiveMask, negativeMask)

    private fun calc(arg: SysInteger, func: (GInt, GInt) -> GInt): SysInteger {
        if (hasUndefined || arg.hasUndefined)
            return uninitialized(Math.max(width, arg.width))
        return if (width >= arg.width) truncate(width, func(value, arg.value), positiveMask, negativeMask)
        else truncate(arg.width, func(value, arg.value), arg.positiveMask, arg.negativeMask)
    }

    open operator fun plus(arg: Int) = calc(value + arg)
    open operator fun minus(arg: Int) = calc(value - arg)
    open operator fun times(arg: Int) = calc(value * arg)
    open operator fun div(arg: Int) = calc(value / arg)
    open operator fun mod(arg: Int) = calc(value % arg)

    open operator fun plus(arg: Long) = calc(value + arg)
    open operator fun minus(arg: Long) = calc(value - arg)
    open operator fun times(arg: Long) = calc(value * arg)
    open operator fun div(arg: Long) = calc(value / arg)
    open operator fun mod(arg: Long) = calc(value % arg)

    open operator fun inc() = calc(value.inc())
    open operator fun dec() = calc(value.dec())

    operator fun get(i: Int): SysBit {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined) return bitsState[i]

        return if (value.testBit(i))
            SysBit.ONE
        else
            SysBit.ZERO
    }

    open operator fun get(j: Int, i: Int): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        val result = value shr i
        val resWidth = j - i + 1
        return truncate(resWidth, result,
                getPositiveValue(resWidth), getNegativeValue(resWidth))
    }

    open operator fun set(i: Int, bit: SysBit): SysInteger {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined) {
            val newState = bitsState.copyOf()
            newState[i] = bit
            return valueOf(newState)
        }
        if (bit == SysBit.X) {
            val newState = Array(width, { i -> get(i) })
            newState[i] = bit
            return valueOf(newState)
        }

        return construct(width, value.setBit(bit, i),
                positiveMask, negativeMask)
    }

    open operator fun set(j: Int, i: Int, bits: Array<SysBit>): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        if ((j - i + 1) != bits.size) throw IllegalArgumentException()

        if (hasUndefined) {
            val newState = bitsState.copyOf()
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return valueOf(newState)
        }
        if (bits.contains(SysBit.X)) {
            val newState = Array(width, { i -> get(i) })
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return valueOf(newState)
        }
        var newValue = value
        for (a in 0..j - i)
            newValue = newValue.setBit(bits[a], i + a)

        return construct(width, newValue,
                positiveMask, negativeMask)
    }

    open operator fun set(j: Int, i: Int, arg: SysInteger): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        if ((j - i + 1) != arg.width) throw IllegalArgumentException()
        return set(j, i, arg.bits())
    }


    open fun inv(): SysInteger {
        if (hasUndefined) {
            val resultState = Array(bitsState.size, { i -> bitsState[i].not() })
            return valueOf(resultState)
        }
        return construct(width, value.inv(),
                positiveMask, negativeMask)
    }

    open infix fun and(arg: SysInteger): SysInteger {
        if (arg.width > width) return arg and this
        return bitwiseOperation(arg, SysBit::and, GInt::and)
    }

    open infix fun or(arg: SysInteger): SysInteger {
        if (arg.width > width) return arg or this
        return bitwiseOperation(arg, SysBit::or, GInt::or)
    }

    open infix fun xor(arg: SysInteger): SysInteger {
        if (arg.width > width) return arg xor this
        return bitwiseOperation(arg, SysBit::xor, GInt::xor)
    }

    private fun bitwiseOperation(
            arg: SysInteger,
            arrBitOperation: (SysBit, SysBit) -> SysBit,
            bitOperation: (GInt, GInt) -> GInt
    ): SysInteger {
        if (hasUndefined || arg.hasUndefined) {

            val resultState = if (hasUndefined) bitsState else Array(width, { i -> get(i) })
            val argState = if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg[i] })

            for (i in argState.indices)
                resultState[i] = arrBitOperation(resultState[i], argState[i])

            return valueOf(resultState)
        }
        return construct(width, bitOperation(value, arg.value),
                positiveMask, negativeMask)
    }

    open infix fun shl(shift: Int): SysInteger {
        if (shift == 0) return this
        if (shift > width) throw IllegalArgumentException()

        val realShift = if (shift >= 0) shift else width + shift

        if (hasUndefined) {
            val newBitsState = bitsState.copyOf()
            for (i in 0..newBitsState.lastIndex - realShift)
                newBitsState[i] = newBitsState[i + realShift]

            for (i in newBitsState.size - realShift..newBitsState.lastIndex)
                newBitsState[i] = SysBit.ZERO

            return valueOf(newBitsState)
        }
        return construct(width, value shl realShift,
                positiveMask, negativeMask)
    }

    open infix fun shr(shift: Int): SysInteger {
        if (shift == 0) return this
        if (shift > width || shift < 0) throw IllegalArgumentException()

        if (hasUndefined) {
            val newBitsState = bitsState.copyOf()
            for (i in newBitsState.lastIndex downTo shift)
                newBitsState[i] = newBitsState[i - shift]

            for (i in 0..shift)
                newBitsState[i] = bitsState[0]

            return valueOf(newBitsState)
        }
        return construct(width, value shr shift,
                positiveMask, negativeMask)
    }

    open infix fun ushr(shift: Int): SysInteger {
        if (shift == 0) return this
        if (shift > width) throw IllegalArgumentException()

        val realShift = if (shift > 0) shift else width + shift

        if (hasUndefined) {
            val newBitsState = bitsState.copyOf()
            for (i in newBitsState.lastIndex downTo realShift)
                newBitsState[i] = newBitsState[i - realShift]

            for (i in 0..realShift - 1)
                newBitsState[i] = SysBit.ZERO

            return valueOf(newBitsState)
        }
        if (value greater 0L || value eq 0L)
            return construct(width, value shr realShift,
                    positiveMask, negativeMask)
        else {
            val nosign = value and positiveMask
            return construct(width, nosign shr realShift,
                    positiveMask, negativeMask)
        }
    }

    open infix fun cshl(shift: Int): SysInteger {
        if (shift < 0) return (this cshr -shift)
        val realShift = shift % width
        if (realShift == 0) return this
        return ((this shl realShift) or (this ushr -realShift))
    }

    open infix fun cshr(shift: Int): SysInteger {
        if (shift < 0) return (this cshl -shift)
        val realShift = shift % width
        if (realShift == 0) return this
        return ((this ushr realShift) or (this shl -realShift))
    }

    fun bits() = if (hasUndefined) bitsState else Array(width, { i -> get(i) })
    fun toBitString(): String {
        val builder = StringBuilder()
        bits().forEach {
            when (it) {
                SysBit.X -> builder.append("X")
                SysBit.Z -> builder.append("Z")
                SysBit.ONE -> builder.append("1")
                SysBit.ZERO -> builder.append("0")
            }
        }
        return builder.toString()
    }

    fun toLong() = value.toLong()
    fun toInt() = value.toInt()

    override fun compareTo(other: SysInteger): Int {
        if (width != other.width) throw IllegalArgumentException("Non comparable. Width not equal.")
        return value.compareTo(other.value)
    }

    protected abstract fun getPositiveValue(width: Int): GInt
    protected abstract fun getNegativeValue(width: Int): GInt

    companion object : SysDataCompanion<SysInteger> {

        fun uninitialized(width: Int) = valueOf(Array(width, { i -> SysBit.X }))

        fun construct(width: Int, value: GInt, positiveMask: GInt, negativeMask: GInt): SysInteger {
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(width, value.toLInt(), positiveMask.toLInt(), negativeMask.toLInt())
            return SysBigInteger(width, value.toBInt(), positiveMask.toBInt(), negativeMask.toBInt())
        }

        fun construct(width: Int, value: GInt): SysInteger {
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(width, value.toLInt(),
                        SysLongInteger.getMaxValue(width), SysLongInteger.getMinValue(width))
            return SysBigInteger(width, value.toBInt(),
                    SysBigInteger.getMaxValue(width), SysBigInteger.getMinValue(width))
        }

        fun valueOf(arr: Array<SysBit>): SysInteger {
            if (arr.size <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger(arr)
            return SysBigInteger(arr)
        }

        fun valueOf(width: Int, value: Number): SysInteger {
            if (width <= SysLongInteger.MAX_WIDTH)
                return SysLongInteger.valueOf(width, value.toLong())
            if (value is BigInteger)
                return SysBigInteger.valueOf(width, value)
            return SysBigInteger.valueOf(width, value.toLong())
        }

        operator fun invoke(arr: Array<SysBit>) = valueOf(arr)
        operator fun invoke(width: Int, value: Long): SysInteger = SysLongInteger.valueOf(width, value)
        override val undefined: SysInteger
            get() = uninitialized(1)

    }
}

fun unsigned(width: Int, value: Int) = SysUnsigned.valueOf(width, value)

fun unsigned(width: Int, value: Long) = SysUnsigned.valueOf(width, value)

fun integer(value: Int) = SysLongInteger.valueOf(value.toLong())

fun integer(value: Long) = SysLongInteger.valueOf(value)

fun integer(width: Int, value: Int) = SysLongInteger.valueOf(width, value.toLong())

fun integer(width: Int, value: Long) = SysLongInteger.valueOf(width, value)

fun integer(width: Int, value: Number) = SysInteger.valueOf(width, value.toLong())

fun integer(bits: Array<SysBit>) = SysInteger.valueOf(bits)
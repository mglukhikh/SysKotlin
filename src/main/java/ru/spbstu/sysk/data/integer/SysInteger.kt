package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData

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

    operator fun get(i: Int): SysBit {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined) return bitsState[i]

        return if (value.testBit(i))
            SysBit.ONE
        else
            SysBit.ZERO
    }

    operator fun get(j: Int, i: Int): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        val result = value shr i
        val resWidth = j - i + 1
        return truncate(resWidth, result,
                getPositiveValue(resWidth), getNegativeValue(resWidth))
    }

    operator fun set(i: Int, bit: SysBit): SysInteger {
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

    operator fun set(j: Int, i: Int, bits: Array<SysBit>): SysInteger {
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

    operator fun set(j: Int, i: Int, arg: SysInteger): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        if ((j - i + 1) != arg.width) throw IllegalArgumentException()
        return set(j, i, arg.bits())
    }


    fun inv(): SysInteger {
        if (hasUndefined) {
            val resultState = Array(bitsState.size, { i -> bitsState[i].not() })
            return valueOf(resultState)
        }
        return construct(width, value.inv(),
                positiveMask, negativeMask)
    }

    infix fun and(arg: SysInteger): SysInteger {
        if (arg.width > width) return arg and this
        return bitwiseOperation(arg, SysBit::and, GInt::and)
    }

    infix fun or(arg: SysInteger): SysInteger {
        if (arg.width > width) return arg or this
        return bitwiseOperation(arg, SysBit::or, GInt::or)
    }

    infix fun xor(arg: SysInteger): SysInteger {
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

    infix fun shl(shift: Int): SysInteger {
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

    infix fun shr(shift: Int): SysInteger {
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

    infix fun ushr(shift: Int): SysInteger {
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

    infix fun cshl(shift: Int): SysInteger {
        if (shift < 0) return (this cshr -shift)
        val realShift = shift % width
        if (realShift == 0) return this
        return ((this shl realShift) or (this ushr -realShift))
    }

    infix fun cshr(shift: Int): SysInteger {
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

    override fun compareTo(other: SysInteger): Int {
        if (width != other.width) throw IllegalArgumentException("Non comparable. Width not equal.")
        return value.compareTo(other.value)
    }

    protected abstract fun getPositiveValue(width: Int): GInt
    protected abstract fun getNegativeValue(width: Int): GInt

    companion object {

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

    }
}


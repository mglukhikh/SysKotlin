package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysDataCompanion

class SysUnsigned private constructor(
        width: Int,
        override val value: GInt.UInt,
        hasUndefined: Boolean,
        override val positiveMask: GInt.UInt,
        override val negativeMask: GInt.UInt
) : SysInteger(width, value, hasUndefined, positiveMask, negativeMask) {

    override lateinit var bitsState: Array<SysBit>


    constructor(arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr), arr.contains(SysBit.X),
            positiveMask = getMaxValue(arr.size),
            negativeMask = getMinValue(arr.size)) {
        bitsState = arr
    }

    private constructor(value: GInt.UInt, width: Int = widthByValue(value.value)) :
    this(width, value, false,
            positiveMask = getMaxValue(width),
            negativeMask = getMinValue(width))

    internal constructor(width: Int, value: GInt.UInt, positiveMask: GInt.UInt, negativeMask: GInt.UInt = GInt.UInt(0L)) :
    this(width, value, false, positiveMask = positiveMask,
            negativeMask = negativeMask)

    override fun extend(width: Int): SysInteger {
        if (width < this.width) throw IllegalArgumentException("extended width smaller then current width")
        if (width > MAX_WIDTH)
            return construct(width, value)
        return SysUnsigned(value, width)
    }

    override fun truncate(width: Int): SysInteger {
        if (width < 0 || width >= this.width) throw IllegalArgumentException()
        return truncate(width, value, getPositiveValue(width), getNegativeValue(width))
    }


    operator fun plus(arg: SysUnsigned) = calc(arg, GInt.UInt::plus)
    operator fun minus(arg: SysUnsigned) = calc(arg, GInt.UInt::minus)
    operator fun times(arg: SysUnsigned) = calc(arg, GInt.UInt::times)
    operator fun div(arg: SysUnsigned) = calc(arg, GInt.UInt::div)
    operator fun mod(arg: SysUnsigned) = calc(arg, GInt.UInt::mod)

    override operator fun plus(arg: Int) = calc(value + arg)
    override operator fun minus(arg: Int) = calc(value - arg)
    override operator fun times(arg: Int) = calc(value * arg)
    override operator fun div(arg: Int) = calc(value / arg)
    override operator fun mod(arg: Int) = calc(value % arg)

    override operator fun plus(arg: Long) = calc(value + arg)
    override operator fun minus(arg: Long) = calc(value - arg)
    override operator fun times(arg: Long) = calc(value * arg)
    override operator fun div(arg: Long) = calc(value / arg)
    override operator fun mod(arg: Long) = calc(value % arg)

    override fun inc() = calc(value.inc())
    override fun dec() = calc(value.dec())

    private fun trunc(width: Int, value: GInt.UInt, positiveMask: GInt.UInt)
            = SysUnsigned(width, value and positiveMask, positiveMask)

    private fun undefined(width: Int) = SysUnsigned(Array(width, { i -> SysBit.X }))

    private fun calc(arg: SysUnsigned, func: (GInt.UInt, GInt.UInt) -> GInt.UInt): SysUnsigned {
        if (hasUndefined || arg.hasUndefined)
            return undefined(Math.max(width, arg.width))
        return if (width >= arg.width) trunc(width, func(value, arg.value), positiveMask)
        else trunc(arg.width, func(value, arg.value), arg.positiveMask)
    }

    private fun calc(result: GInt.UInt) = trunc(width, result, positiveMask)


    infix fun and(arg: SysUnsigned): SysUnsigned {
        if (arg.width > width) return arg and this
        return bitwiseOperation(arg, SysBit::and, GInt.UInt::and)
    }

    infix fun or(arg: SysUnsigned): SysUnsigned {
        if (arg.width > width) return arg or this
        return bitwiseOperation(arg, SysBit::or, GInt.UInt::or)
    }

    infix fun xor(arg: SysUnsigned): SysUnsigned {
        if (arg.width > width) return arg xor this
        return bitwiseOperation(arg, SysBit::xor, GInt.UInt::xor)
    }

    private fun bitwiseOperation(
            arg: SysUnsigned,
            arrBitOperation: (SysBit, SysBit) -> SysBit,
            bitOperation: (GInt.UInt, GInt.UInt) -> GInt.UInt
    ): SysUnsigned {
        if (hasUndefined || arg.hasUndefined) {

            val resultState = if (hasUndefined) bitsState else Array(width, { i -> get(i) })
            val argState = if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg[i] })

            for (i in argState.indices)
                resultState[i] = arrBitOperation(resultState[i], argState[i])

            return valueOf(resultState)
        }
        return SysUnsigned(width, bitOperation(value, arg.value),
                positiveMask, negativeMask)
    }


    override infix fun shl(shift: Int): SysUnsigned {
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
        return SysUnsigned(width, value shl realShift,
                positiveMask, negativeMask)
    }

    override infix fun shr(shift: Int): SysUnsigned {
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
        return SysUnsigned(width, value shr shift,
                positiveMask, negativeMask)
    }

    override infix fun ushr(shift: Int) = shr(shift)

    override infix fun cshl(shift: Int): SysUnsigned {
        if (shift < 0) return (this cshr -shift)
        val realShift = shift % width
        if (realShift == 0) return this
        return ((this shl realShift) or (this ushr -realShift))
    }

    override infix fun cshr(shift: Int): SysUnsigned {
        if (shift < 0) return (this cshl -shift)
        val realShift = shift % width
        if (realShift == 0) return this
        return ((this ushr realShift) or (this shl -realShift))
    }

    override operator fun get(j: Int, i: Int): SysUnsigned {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        val result = value shr i
        val resWidth = j - i + 1
        return trunc(resWidth, result, getPositiveValue(resWidth))
    }

    override operator fun set(i: Int, bit: SysBit): SysUnsigned {
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

        return SysUnsigned(width, value.setBit(bit, i),
                positiveMask, negativeMask)
    }

    override operator fun set(j: Int, i: Int, bits: Array<SysBit>): SysUnsigned {
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

        return SysUnsigned(width, newValue,
                positiveMask, negativeMask)
    }


    override operator fun set(j: Int, i: Int, arg: SysInteger): SysUnsigned {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        if ((j - i + 1) != arg.width) throw IllegalArgumentException()
        return set(j, i, arg.bits())
    }


    override fun inv(): SysUnsigned {
        if (hasUndefined) {
            val resultState = Array(bitsState.size, { i -> bitsState[i].not() })
            return valueOf(resultState)
        }
        return SysUnsigned(width, value.inv(),
                positiveMask, negativeMask)
    }


    override fun getPositiveValue(width: Int) = getMaxValue(width)
    override fun getNegativeValue(width: Int) = getMinValue(width)


    companion object : SysDataCompanion<SysUnsigned> {

        const val MAX_WIDTH: Int = 64

        fun getMaxValue(width: Int) = GInt.UInt(positiveValues[width])

        private val positiveValues = Array(MAX_WIDTH + 1, { i -> maxValue(i) })

        fun valueOf(arr: Array<SysBit>) = SysUnsigned(arr)

        fun valueOf(width: Int, value: Int): SysUnsigned {
            val lValue = Integer.toUnsignedLong(value)
            val realWidth = widthByValue(lValue)
            if (realWidth > width)
                throw IllegalArgumentException("Minimal possible width is $realWidth")
            return SysUnsigned(GInt.UInt(lValue), width)
        }

        fun valueOf(width: Int, value: Long): SysUnsigned {
            val realWidth = widthByValue(value)
            if (realWidth > width)
                throw IllegalArgumentException("Minimal possible width is $realWidth")
            return SysUnsigned(GInt.UInt(value), width)
        }

        private fun widthByValue(value: Long): Int {
            return java.lang.Long.SIZE - java.lang.Long.numberOfLeadingZeros(value)
        }

        private fun valueBySWSArray(arr: Array<SysBit>): GInt.UInt {
            val value = CharArray(64, { i -> '0' })
            var counter: Int = 0
            while (counter < arr.size && arr[counter] == SysBit.X)
                counter++
            var shift = 0
            while (counter < arr.size && arr[counter] != SysBit.X) {
                if (arr[counter].one)
                    value[value.lastIndex - shift] = '1'
                shift++
                counter++
            }
            return GInt.UInt(java.lang.Long.parseUnsignedLong(String(value), 2))
        }

        protected fun getMinValue(width: Int) = GInt.UInt(0L)

        private fun maxValue(width: Int): Long {
            if (width == 0) return 0
            return (-1L ushr (MAX_WIDTH - width))
        }

        override val undefined: SysUnsigned
            get() = valueOf(arrayOf(SysBit.X))
    }
}
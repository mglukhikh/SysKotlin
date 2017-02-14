package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysDataCompanion
import java.math.BigInteger

class SysBigInteger private constructor(
        width: Int,
        override val value: BigInteger,
        hasUndefined: Boolean = false,
        override val positiveMask: BigInteger,
        override val negativeMask: BigInteger
) : SysInteger (width, value, hasUndefined, positiveMask, negativeMask) {

    override lateinit var bitsState: Array<SysBit>

    constructor(width: Int, value: BigInteger) : this(width, value, false,
            positiveMask = getMaxValue(width), negativeMask = getMinValue(width)) {
        if (value > positiveMask || value < negativeMask)
            throw IllegalArgumentException("Width $width is short for value $value")
    }

    //    private constructor(width: Int, value: BigInteger, bitsState: Array<Boolean>) :
    //    this(width, value, bitsState = maskByValue(value, width),
    //            positiveMask = getMaxValue(width), negativeMask = getMinValue(width))

    private constructor(width: Int, value: BigInteger, positiveMask: BigInteger, negativeMask: BigInteger) :
    this(width, value, false,
            positiveMask = positiveMask, negativeMask = negativeMask)

    constructor(width: Int, value: Int) : this(width, value.toLong())

    constructor(width: Int, value: Long) : this(width, BigInteger.valueOf(value))

    private constructor(value: BigInteger, width: Int = value.bitLength() + 1) : this(width, value,
            false, positiveMask = getMaxValue(width), negativeMask = getMinValue(width))

    constructor(arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr),
            arr.contains(SysBit.X), positiveMask = getMaxValue(arr.size), negativeMask = getMinValue(arr.size)) {
        bitsState = arr
    }

    private constructor(width: Short) : this(Array(width.toInt(), { i -> SysBit.X }))

    /** Increase width to the given */
    override fun extend(width: Int): SysInteger {
        if (width < this.width)
            return truncate(width)
        return SysBigInteger(width, value)
    }

    /** Decrease width to the given with value truncation */
    override fun truncate(width: Int): SysInteger {
        if (width < 0) throw IllegalArgumentException()

        if (width <= SysLongInteger.MAX_WIDTH)
            return truncate(width, value, getMaxValue(width), getMinValue(width)).toSysLongInteger()

        if (width > this.width)
            return extend(width)

        return truncate(width, value, getMaxValue(width), getMinValue(width))
    }

    private fun truncate(width: Int, value: BigInteger, positiveMask: BigInteger, negativeMask: BigInteger): SysBigInteger {
        if (value >= BigInteger.ZERO)
            if (value > positiveMask)
                return SysBigInteger(width, value.or(negativeMask), positiveMask, negativeMask)
            else
                return SysBigInteger(width, value, positiveMask, negativeMask)
        else
            if (value < negativeMask)
                return SysBigInteger(width, positiveMask + (value + positiveMask), positiveMask, negativeMask)
            else
                return SysBigInteger(width, value, positiveMask, negativeMask)
    }


    override operator fun plus(arg: Long) = this + valueOf(arg)
    override operator fun minus(arg: Long) = this - valueOf(arg)
    override operator fun times(arg: Long) = this * valueOf(arg)
    override operator fun div(arg: Long) = this / valueOf(arg)
    override operator fun rem(arg: Long) = this % valueOf(arg)
    override operator fun plus(arg: Int) = this + valueOf(arg)
    override operator fun minus(arg: Int) = this - valueOf(arg)
    override operator fun times(arg: Int) = this * valueOf(arg)
    override operator fun div(arg: Int) = this / valueOf(arg)
    override operator fun rem(arg: Int) = this % valueOf(arg)

    override operator fun inc() = truncate(width, value + BigInteger.ONE, positiveMask, negativeMask)
    override operator fun dec() = truncate(width, value - BigInteger.ONE, positiveMask, negativeMask)

    override fun power(exp: Int) = truncate(width, value.pow(exp), positiveMask, negativeMask)


    /** Adds arg to this integer, with result width is maximum of argument's widths */
    override operator fun plus(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined)
            unknown(arg)
        val argv = arg.toSysBigInteger()
        if (argv.width > width)
            return truncate(argv.width, value + argv.value, argv.positiveMask, argv.negativeMask)

        return truncate(width, value + argv.value, positiveMask, negativeMask)
    }

    private fun unknown(arg: SysInteger) = unknown(Math.max(width, arg.width))
    /**Unary minus*/
    override operator fun unaryMinus(): SysBigInteger {
        return SysBigInteger(width, value.negate())
    }

    /** Subtract arg from this integer*/
    override operator fun minus(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined)
            unknown(arg)
        val argv = arg.toSysBigInteger()
        if (argv.width > width)
            return truncate(argv.width, value - argv.value, argv.positiveMask, argv.negativeMask)

        return truncate(width, value - argv.value, positiveMask, negativeMask)
    }

    /** Integer division by divisor*/
    override operator fun div(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined)
            unknown(arg)
        val argv = arg.toSysBigInteger()
        if (arg.value == BigInteger.ZERO) throw IllegalArgumentException("Division by zero")
        if (argv.width > width)
            return truncate(argv.width, value / argv.value, argv.positiveMask, argv.negativeMask)

        return truncate(width, value / argv.value, positiveMask, negativeMask)
    }

    /** Remainder of integer division*/
    override operator fun rem(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined)
            unknown(arg)
        val argv = arg.toSysBigInteger()
        if (arg.value == BigInteger.ZERO) throw IllegalArgumentException("Division by zero")
        if (argv.width > width)
            return truncate(argv.width, value % argv.value, argv.positiveMask, argv.negativeMask)

        return truncate(width, value % argv.value, positiveMask, negativeMask)
    }

    /** Multiplies arg to this integer, with result width is sum of argument's width */
    override operator fun times(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined)
            unknown(arg)
        val argv = arg.toSysBigInteger()
        if (argv.width > width)
            return truncate(argv.width, value * argv.value, argv.positiveMask, argv.negativeMask)

        return truncate(width, value * argv.value, positiveMask, negativeMask)
    }

    /**
     * Bitwise and
     * */
    override infix fun and(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined) {

            val state = (if (hasUndefined) bitsState else Array(width, { i -> get(i) }))
            val argState = (if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg[i] }))
            val resultState: Array<SysBit>
            if (state.size > argState.size) {
                resultState = state
                for (i in argState.indices)
                    resultState[i] = resultState[i]and argState[i]
            } else {
                resultState = argState
                for (i in state.indices)
                    resultState[i] = resultState[i]and state[i]
            }

            return SysBigInteger(resultState)
        }
        val argv = arg.toSysBigInteger()
        if (argv.width > width)
            return SysBigInteger(argv.width, argv.value.and(value),
                    argv.positiveMask, argv.negativeMask)

        return SysBigInteger(width, value.and(argv.value),
                positiveMask, negativeMask)

    }


    /** Bitwise or*/
    override infix fun or(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined) {

            val state = (if (hasUndefined) bitsState else Array(width, { i -> get(i) }))
            val argState = (if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg[i] }))
            val resultState: Array<SysBit>
            if (state.size > argState.size) {
                resultState = state
                for (i in argState.indices)
                    resultState[i] = resultState[i]or argState[i]
            } else {
                resultState = argState
                for (i in state.indices)
                    resultState[i] = resultState[i]or state[i]
            }

            return SysBigInteger(resultState)
        }
        val argv = arg.toSysBigInteger()
        if (argv.width > width)
            return SysBigInteger(argv.width, argv.value.or(value),
                    argv.positiveMask, argv.negativeMask)

        return SysBigInteger(width, value.or(argv.value),
                positiveMask, negativeMask)

    }

    /** Bitwise xor*/
    override infix fun xor(arg: SysInteger): SysBigInteger {
        if (hasUndefined || arg.hasUndefined) {

            val state = (if (hasUndefined) bitsState else Array(width, { i -> get(i) }))
            val argState = (if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg[i] }))
            val resultState: Array<SysBit>
            if (state.size > argState.size) {
                resultState = state
                for (i in argState.indices)
                    resultState[i] = resultState[i]xor argState[i]
            } else {
                resultState = argState
                for (i in state.indices)
                    resultState[i] = resultState[i]xor state[i]
            }

            return SysBigInteger(resultState)
        }
        val argv = arg.toSysBigInteger()
        if (argv.width > width)
            return SysBigInteger(argv.width, argv.value.xor(value),
                    argv.positiveMask, argv.negativeMask)

        return SysBigInteger(width, value.xor(argv.value),
                positiveMask, negativeMask)

    }

    /**Bitwise inversion (not)*/
    override fun inv(): SysBigInteger {
        if (hasUndefined) {
            return SysBigInteger(Array(bitsState.size, { i -> bitsState[i].not() }))
        }
        return SysBigInteger(width, value.not(), positiveMask, negativeMask)
    }

    /** Extracts a single bit, accessible as [i] */
    override operator fun get(i: Int): SysBit {

        if (i < 0 || i >= width) throw IndexOutOfBoundsException()

        if (hasUndefined)
            return bitsState[i]
        if (value.testBit(i))
            return SysBit.ONE
        else
            return SysBit.ZERO

    }

    /** Extracts a range of bits, accessible as [j,i] */
    override operator fun get(j: Int, i: Int): SysBigInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        val result = value.shiftRight(i)
        val resWidth = j - i + 1
        return truncate(resWidth, result, getMaxValue(resWidth), getMinValue(resWidth))
    }

    override fun bits(): Array<SysBit> {
        if (hasUndefined)
            return bitsState
        return Array(width, { i -> get(i) })
    }

    override fun set(j: Int, i: Int, arg: SysInteger): SysBigInteger {
        return set(j, i, arg.bits())
    }

    override fun set(i: Int, bit: SysBit): SysBigInteger {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined) {
            val newState = bitsState.copyOf()
            newState[i] = bit
            return SysBigInteger(newState)
        }
        if (bit == SysBit.X) {
            val newState = Array(width, { i -> get(i) })
            newState[i] = bit
            return SysBigInteger(newState)
        }
        var newValue = value
        if (bit == SysBit.ONE) {
            newValue = newValue.setBit(i)
        } else {
            newValue = newValue.clearBit(i)
        }
        return SysBigInteger(width, newValue, positiveMask, negativeMask)
    }

    override fun set(j: Int, i: Int, bits: Array<SysBit>): SysBigInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        if ((j - i + 1) != bits.size) throw IllegalArgumentException()

        if (hasUndefined) {
            val newState = bitsState.copyOf()
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return SysBigInteger(newState)
        }
        if (bits.contains(SysBit.X)) {
            val newState = Array(width, { i -> get(i) })
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return SysBigInteger(newState)
        }
        var newValue = value
        for (a in 0..j - i)
            if (bits[a] == SysBit.ONE) {
                newValue = newValue.setBit(i + a)
            } else {
                newValue = newValue.clearBit(i + a)
            }
        return SysBigInteger(width, newValue, positiveMask, negativeMask)
    }

    /** Bitwise logical shift right*/
    override infix fun ushr(shift: Int): SysBigInteger {
        if (shift == 0)
            return this
        if (shift > width)
            throw IllegalArgumentException()
        var realShift = shift
        if (shift < 0)
            realShift = width + shift
        if (hasUndefined) {
            val newBitsState = bitsState.copyOf()
            for (i in newBitsState.lastIndex downTo realShift) {
                newBitsState[i] = newBitsState[i - realShift]
            }
            for (i in 0..realShift - 1) {
                newBitsState[i] = SysBit.ZERO
            }
            return SysBigInteger(newBitsState)
        }
        if (value >= BigInteger.ZERO)
            return SysBigInteger(width, value.shiftRight(realShift), positiveMask, negativeMask)
        else {
            val result = (value.and(positiveMask)).shiftRight(realShift)
            return SysBigInteger(width, result, positiveMask, negativeMask)
        }
    }


    /** Arithmetic shift right*/
    override infix fun shr(shift: Int): SysBigInteger {
        if (shift == 0)
            return this
        if (shift > width || shift < 0)
            throw IllegalArgumentException()
        if (hasUndefined) {
            val newBitsState = bitsState.copyOf()
            for (i in newBitsState.lastIndex downTo shift) {
                newBitsState[i] = newBitsState[i - shift]
            }
            for (i in 0..shift) {
                newBitsState[i] = bitsState[0]
            }
            return SysBigInteger(newBitsState)
        }
        return SysBigInteger(width, value.shiftRight(shift), positiveMask, negativeMask)
    }

    /** Arithmetic shift left*/
    override infix fun shl(shift: Int): SysBigInteger {
        if (shift == 0)
            return this
        if (shift > width)
            throw IllegalArgumentException()
        var realShift = shift
        if (shift < 0)
            realShift = width + shift
        if (hasUndefined) {
            val newBitsState = bitsState.copyOf()
            for (i in 0..newBitsState.lastIndex - realShift) {
                newBitsState[i] = newBitsState[i + realShift]
            }
            for (i in newBitsState.size - realShift..newBitsState.lastIndex) {
                newBitsState[i] = SysBit.ZERO
            }
            return SysBigInteger(newBitsState)
        }
        return SysBigInteger(width, value.shiftLeft(realShift), positiveMask, negativeMask)
    }


    /** Cyclic shift right*/
    override infix fun cshr(shift: Int): SysBigInteger {

        if (shift < 0)
            return this cshl -shift

        val realShift = shift % width

        if (realShift == 0)
            return this

        return ((this ushr realShift)or(this shl -realShift))
    }

    /** Cyclic shift left*/
    override infix fun cshl(shift: Int): SysBigInteger {

        if (shift < 0)
            return this cshr -shift
        val realShift = shift % width

        if (realShift == 0)
            return this

        return ((this shl realShift)or(this ushr -realShift))
    }

    override fun toSysBigInteger() = this

    override fun toSysLongInteger() = SysLongInteger(width, value.toLong())

    override fun toInt() = value.toInt()

    override fun toLong() = value.toLong()

    override fun abs(): SysBigInteger {
        return SysBigInteger(width, value.abs())
    }

    override fun compareTo(other: SysInteger): Int {
        if (width != other.width) {
            throw IllegalArgumentException("Non comparable. Width not equal.")
        }
        if (other.value is BigInteger)
            return value.compareTo(other.value as BigInteger)
        else
            return value.compareTo(BigInteger.valueOf(other.value as Long))
    }


    companion object : SysDataCompanion<SysBigInteger> {

        //fun uninitialized(width: Int) = SysBigInteger(width.toShort())

        fun valueOf(arg: Array<SysBit>) = SysBigInteger(arg)

        fun valueOf(value: Long) = SysBigInteger(BigInteger.valueOf(value))

        fun valueOf(value: BigInteger) = SysBigInteger(value)

        fun valueOf(value: SysInteger) = value.toSysBigInteger()

        fun unknown(width: Int) = SysBigInteger(width.toShort())

        //        private fun maskByValue(value: BigInteger, width: Int): Array<Boolean> {
        //
        //                        if (width == 0)
        //                            return BooleanArray(0).toTypedArray();
        //
        //                        val widthByValue = value.bitLength() + 1
        //                        val mask = BooleanArray(width);
        //
        //                        if (width < widthByValue) {
        //                            throw IllegalArgumentException("Width $width is too small for this value \n$value \nwith width $widthByValue")
        //                        } else {
        //                            mask.fill(true, mask.lastIndex + 1 - widthByValue, mask.lastIndex + 1)
        //
        //                        }
        //                        return mask.toTypedArray();
        //            return Array(width, { i -> true })
        //        }


        private fun valueBySWSArray(arr: Array<SysBit>): BigInteger {

            var leftShift = 0
            var rightShift = 0
            while (leftShift < arr.size && arr[leftShift] == SysBit.X)
                leftShift++

            while (arr.size - rightShift > leftShift && arr[arr.size - 1 - rightShift] == SysBit.X)
                rightShift++


            if (leftShift + rightShift == arr.size)
                return BigInteger.ZERO

            var result = BigInteger.ZERO


            val tempArray = arr.copyOfRange(leftShift, arr.size - rightShift)
            if (!tempArray.last().one) {
                for (i in tempArray.indices) {
                    if (tempArray[i].one) {
                        result = result.setBit(i)
                    }
                }
                return result

            } else {

                val inverseArray = inverseSWSArray(tempArray)

                for (i in inverseArray.indices) {
                    if (inverseArray[i].one) {
                        result = result.setBit(i)
                    }
                }
                result = result.negate()
                return result
            }

        }

        private fun inverseSWSArray(arr: Array<SysBit>): Array<SysBit> {

            val result = arr
            for (i in result.indices) {
                if (result[i] == SysBit.ZERO) {
                    result[i] = SysBit.ONE
                } else {
                    result[i] = SysBit.ZERO
                }
            }

            var breaker = true
            var i = 0
            while (breaker && i < result.size) {

                if (result[i] == SysBit.ZERO) {
                    result[i] = SysBit.ONE
                    breaker = false
                } else {

                    result[i] = SysBit.ZERO

                }
                i++
            }
            return result
        }

        private val positiveValues = Array(129, { i -> maxValue(i) })
        private val negativeValues = Array(129, { i -> minValue(i) })

        private fun getMaxValue(width: Int): BigInteger = (if (128 >= width) positiveValues[width] else maxValue(width))
        private fun getMinValue(width: Int): BigInteger = (if (128 >= width) negativeValues[width] else minValue(width))

        private fun minValue(width: Int): BigInteger {
            if (width == 0) return BigInteger.ZERO
            return (((-BigInteger.ONE).shiftLeft (width - 1)))
        }

        private fun maxValue(width: Int): BigInteger {
            if (width == 0) return BigInteger.ZERO
            return (BigInteger.ONE.shiftLeft (width - 1)) - BigInteger.ONE
        }

        //        private fun maskBySWSArray(arr: Array<SysBit>): Array<Boolean> {
        //
        //
        //            val mask = BooleanArray(arr.size)
        //
        //
        //            for (i in 0..mask.size - 1)
        //                if (arr[i] != SysBit.X)
        //                    mask[i] = true;
        //
        //            return mask.toTypedArray();
        //        }


        override val undefined: SysBigInteger
            get() = SysBigInteger(arrayOf(SysBit.X))
    }
}
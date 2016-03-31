package ru.spbstu.sysk.data

import java.math.BigInteger

class SysBigInteger private constructor(
        width: Int,
        override val value: BigInteger,
        defaultBitState: Boolean = false,
        bitsState: Array<Boolean> = Array(width, { i -> defaultBitState }),
        override val positiveMask: BigInteger,
        override val negativeMask: BigInteger
) : SysBaseInteger (width, value, bitsState, positiveMask, negativeMask) {

    constructor(width: Int, value: BigInteger) : this(width, value, bitsState = maskByValue(value, width),
            positiveMask = getMaxValue(width), negativeMask = getMinValue(width))

    private constructor(width: Int, value: BigInteger, bitsState: Array<Boolean>) :
    this(width, value, bitsState = maskByValue(value, width),
            positiveMask = getMaxValue(width), negativeMask = getMinValue(width))

    private constructor(width: Int, value: BigInteger, positiveMask: BigInteger, negativeMask: BigInteger) :
    this(width, value, bitsState = maskByValue(value, width),
            positiveMask = positiveMask, negativeMask = negativeMask)

    constructor(width: Int, value: Int) : this(width, value.toLong())

    constructor(width: Int, value: Long) : this(width, BigInteger.valueOf(value),
            bitsState = maskByValue(BigInteger.valueOf(value), width),
            positiveMask = getMaxValue(width), negativeMask = getMinValue(width))

    private constructor(value: BigInteger, width: Int = value.bitLength() + 1) : this(width, value,
            bitsState = maskByValue(value, value.bitLength() + 1),
            positiveMask = getMaxValue(width), negativeMask = getMinValue(width))

    constructor(arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr),
            bitsState = maskBySWSArray(arr), positiveMask = getMaxValue(arr.size), negativeMask = getMinValue(arr.size))

    private constructor(width: Short) : this(width.toInt(), BigInteger.ZERO, false,
            positiveMask = getMaxValue(width.toInt()), negativeMask = getMinValue(width.toInt()))

    /** Increase width to the given */
    override fun extend(width: Int): SysBigInteger {
        if (width < value.bitLength())
            throw IllegalArgumentException()
        return SysBigInteger(width, value)
    }

    /** Decrease width to the given with value truncation */
    override fun truncate(width: Int): SysBigInteger {
        val widthByValue = value.bitLength() + 1
        //println("trunc [$width] [$widthByValue]")
        if (width >= widthByValue) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        val truncated = value.shiftRight(widthByValue - width)
        return SysBigInteger(width, truncated)
    }

    private fun truncate(width: Int, value: BigInteger, positiveMask: BigInteger, negativeMask: BigInteger): SysBigInteger {
        if (value >= BigInteger.ZERO)
            return SysBigInteger(width, value.and(positiveMask), positiveMask, negativeMask)
        else
            return SysBigInteger(width, value.or(negativeMask), positiveMask, negativeMask)
    }

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    override operator fun plus(arg: SysBaseInteger): SysBigInteger {
        val arg = arg.toSysBigInteger()
        var resWidth: Int = width
        var posMask: BigInteger = positiveMask
        var negMask: BigInteger = negativeMask
        if (arg.width > width) {
            resWidth = arg.width
            posMask = arg.positiveMask
            negMask = arg.negativeMask
        }
        return truncate(resWidth, value + arg.value, posMask, negMask)
    }

    override operator fun plus(arg: Long) = this + valueOf(arg)
    override operator fun minus(arg: Long) = this - valueOf(arg)
    override operator fun times(arg: Long) = this * valueOf(arg)
    override operator fun div(arg: Long) = this / valueOf(arg)
    override operator fun mod(arg: Long) = this % valueOf(arg)
    override operator fun plus(arg: Int) = this + valueOf(arg)
    override operator fun minus(arg: Int) = this - valueOf(arg)
    override operator fun times(arg: Int) = this * valueOf(arg)
    override operator fun div(arg: Int) = this / valueOf(arg)
    override operator fun mod(arg: Int) = this % valueOf(arg)

    override operator fun inc() = truncate(width, value + BigInteger.ONE, positiveMask, negativeMask)
    override operator fun dec() = truncate(width, value - BigInteger.ONE, positiveMask, negativeMask)

    override fun power(exp: Int) = truncate(width, value.pow(exp), positiveMask, negativeMask)

    /**Unary minus*/
    override operator fun unaryMinus(): SysBigInteger {
        return SysBigInteger(width, value.negate())
    }

    /** Subtract arg from this integer*/
    override operator fun minus(arg: SysBaseInteger): SysBigInteger {
        val arg = arg.toSysBigInteger()
        var resWidth: Int = width
        var posMask: BigInteger = positiveMask
        var negMask: BigInteger = negativeMask
        if (arg.width > width) {
            resWidth = arg.width
            posMask = arg.positiveMask
            negMask = arg.negativeMask
        }
        return truncate(resWidth, value - arg.value, posMask, negMask)
    }

    /** Integer division by divisor*/
    override operator fun div(arg: SysBaseInteger): SysBigInteger {
        if (arg.value == BigInteger.ZERO) throw IllegalArgumentException("Division by zero")
        val arg = arg.toSysBigInteger()
        var resWidth: Int = width
        var posMask: BigInteger = positiveMask
        var negMask: BigInteger = negativeMask
        if (arg.width > width) {
            resWidth = arg.width
            posMask = arg.positiveMask
            negMask = arg.negativeMask
        }
        return truncate(resWidth, value / arg.value, posMask, negMask)
    }

    /** Remainder of integer division*/
    override operator fun mod(arg: SysBaseInteger): SysBigInteger {
        if (arg.value == BigInteger.ZERO) throw IllegalArgumentException("Division by zero")
        val arg = arg.toSysBigInteger()
        var resWidth: Int = width
        var posMask: BigInteger = positiveMask
        var negMask: BigInteger = negativeMask
        if (arg.width > width) {
            resWidth = arg.width
            posMask = arg.positiveMask
            negMask = arg.negativeMask
        }
        return truncate(resWidth, value % arg.value, posMask, negMask)
    }

    /** Multiplies arg to this integer, with result width is sum of argument's width */
    override operator fun times(arg: SysBaseInteger): SysBigInteger {
        val arg = arg.toSysBigInteger()
        var resWidth: Int = width
        var posMask: BigInteger = positiveMask
        var negMask: BigInteger = negativeMask
        if (arg.width > width) {
            resWidth = arg.width
            posMask = arg.positiveMask
            negMask = arg.negativeMask
        }
        return truncate(resWidth, value * arg.value, posMask, negMask)
    }

    /**
     * Bitwise and
     * */
    override infix fun and(arg: SysBaseInteger): SysBigInteger {
        val arg = arg.toSysBigInteger()
        var temp = arg.bitsState;

        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysBigInteger(Math.max(width, arg.width), value.and(arg.value), bitsState = temp)

    }


    /** Bitwise or*/
    override infix fun or(arg: SysBaseInteger): SysBigInteger {
        val arg = arg.toSysBigInteger()
        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysBigInteger(Math.max(width, arg.width), value.or(arg.value), bitsState = temp)

    }

    /** Bitwise xor*/
    override infix fun xor(arg: SysBaseInteger): SysBigInteger {
        val arg = arg.toSysBigInteger()
        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and  bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysBigInteger(Math.max(width, arg.width), value.xor(arg.value), bitsState = temp)
    }

    /**Bitwise inversion (not)*/
    override fun inv(): SysBigInteger {
        return SysBigInteger(width, value.not(), bitsState = bitsState);
    }

    /** Extracts a single bit, accessible as [i] */
    override operator fun get(i: Int): SysBit {

        if (i < 0 || i >= width) throw IndexOutOfBoundsException()

        if (!bitsState[i])
            return SysBit.X
        val shift = bitsState.indexOf(true)
        if (value.testBit(i - shift))
            return SysBit.ONE
        else
            return SysBit.ZERO

    }

    /** Extracts a range of bits, accessible as [j,i] */
    override operator fun get(j: Int, i: Int): SysBigInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        var result = value.shiftRight(i)
        return SysBigInteger(result).truncate(j - i + 1)
    }


    /** Bitwise logical shift right*/
    override infix fun ushr(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysBitExpression = Array(width - shift) { i: Int -> this[i] }

        return SysBigInteger(sysBitExpression);
    }

    /** Bitwise logical shift left*/
    override infix fun ushl(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysBitExpression = Array(width - shift) { i: Int -> this[i + shift] }

        return SysBigInteger(sysBitExpression)
    }

    /** Arithmetic shift right*/
    override infix fun shr(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()
        val sysBitExpression = Array(width) { i -> this[i] }
        var i = width - 1
        while (i >= shift) {
            sysBitExpression[i] = sysBitExpression[i - shift]
            i--
        }
        while (i >= 0) {
            sysBitExpression[i] = SysBit.ZERO
            i--
        }
        return SysBigInteger(sysBitExpression)
    }

    /** Arithmetic shift left*/
    override infix fun shl(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()
        val sysBitExpression = Array(width) { i -> this[i] }
        var i = 0
        while (i < sysBitExpression.size - shift) {
            sysBitExpression[i] = sysBitExpression[i + shift]
            i++
        }
        while (i < sysBitExpression.size) {
            sysBitExpression[i] = SysBit.ZERO
            i++
        }
        return SysBigInteger(sysBitExpression)

    }


    /** Cyclic shift right*/
    override infix fun cshr(shift: Int): SysBigInteger {

        if (shift < 0)
            return this cshl -shift

        val realShift = shift % width

        if (realShift == 0)
            return this;
        val sysBitExpression = Array(width) { i -> this[i] }

        val tempArray = Array(realShift, { SysBit.X })

        for (i in 0..realShift - 1) {
            tempArray[i] = sysBitExpression[sysBitExpression.size - realShift + i]
        }
        var i = sysBitExpression.size - 1
        while (i >= realShift) {
            sysBitExpression[i] = sysBitExpression[i - realShift]
            i--
        }
        while (i >= 0) {
            sysBitExpression[i] = tempArray[i]
            i--
        }
        return SysBigInteger(sysBitExpression)
    }

    /** Cyclic shift left*/
    override infix fun cshl(shift: Int): SysBigInteger {

        if (shift < 0)
            return this cshr -shift
        val realShift = shift % width

        if (realShift == 0)
            return this;
        val sysBitExpression = Array(width) { i -> this[i] }

        val tempArray = Array(realShift, { SysBit.X })

        for (i in 0..realShift - 1) {
            tempArray[i] = sysBitExpression[i]
        }

        var i = 0
        while (i < sysBitExpression.size - shift) {
            sysBitExpression[i] = sysBitExpression[i + shift]
            i++
        }
        while (i < sysBitExpression.size) {
            sysBitExpression[i] = tempArray[i - sysBitExpression.size + realShift]
            i++
        }
        return SysBigInteger(sysBitExpression)
    }

    override fun toSysBigInteger() = this

    override fun toSysInteger() = SysLongInteger(width, value.toLong())

    override fun toInt() = value.toInt()

    override fun toLong() = value.toLong()

    override fun abs(): SysBigInteger {
        return SysBigInteger(width, value.abs(), bitsState = bitsState)
    }

    override fun compareTo(other: SysBaseInteger): Int {
        if (width != other.width) {
            throw IllegalArgumentException("Non comparable. Width not equal.")
        }
        if (other.value is BigInteger)
            return value.compareTo(other.value as BigInteger)
        else
            return value.compareTo(BigInteger.valueOf(other.value as Long))
    }


    companion object : SysDataCompanion<SysBigInteger> {

        fun uninitialized(width: Int) = SysBigInteger(width.toShort())

        fun valueOf(value: Long) = SysBigInteger(BigInteger.valueOf(value))

        fun valueOf(value: BigInteger) = SysBigInteger(value)

        fun valueOf(value: SysBaseInteger) = value.toSysBigInteger()

        private fun maskByValue(value: BigInteger, width: Int): Array<Boolean> {

            if (width == 0)
                return BooleanArray(0).toTypedArray();

            val widthByValue = value.bitLength() + 1
            val mask = BooleanArray(width);

            if (width < widthByValue) {
                throw IllegalArgumentException("Width $width is too small for this value \n$value \nwith width $widthByValue")
            } else {
                mask.fill(true, mask.lastIndex + 1 - widthByValue, mask.lastIndex + 1)

            }
            return mask.toTypedArray();
        }


        private fun valueBySWSArray(arr: Array<SysBit>): BigInteger {

            var leftShift = 0;
            var rightShift = 0;
            while (leftShift < arr.size && arr[leftShift] == SysBit.X )
                leftShift++;

            while (arr.size - rightShift > leftShift && arr[arr.size - 1 - rightShift] == SysBit.X )
                rightShift++;


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

            val result = arr;
            for (i in result.indices) {
                if (result[i] == SysBit.ZERO) {
                    result[i] = SysBit.ONE
                } else {
                    result[i] = SysBit.ZERO
                }
            }

            var breaker = true;
            var i = 0;
            while (breaker && i < result.size ) {

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

        private val positiveValues = Array<BigInteger>(129, { i -> maxValue(i) })
        private val negativeValues = Array<BigInteger>(129, { i -> minValue(i) })

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

        private fun maskBySWSArray(arr: Array<SysBit>): Array<Boolean> {


            val mask = BooleanArray(arr.size)


            for (i in 0..mask.size - 1)
                if (arr[i] != SysBit.X)
                    mask[i] = true;

            return mask.toTypedArray();
        }

        override val undefined: SysBigInteger
            get() = SysBigInteger(arrayOf(SysBit.X))
    }
}
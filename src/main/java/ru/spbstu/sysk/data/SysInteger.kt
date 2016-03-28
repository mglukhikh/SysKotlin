package ru.spbstu.sysk.data

/** Width of integer / unsigned / ... */
@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class Width(val value: Int)

/**
 * Immutable class for a fixed-width integer, width can be from 0 to 64, inclusively.
 * Value is stored in a long integer.
 * Width can be checked statically (in future) if type usage is annotated by @width annotation
 */
class SysInteger private constructor(
        width: Int,
        override val value: Long,
        defaultBitState: Boolean = false,
        bitsState: Array<Boolean> = Array(width, { i -> defaultBitState }),
        override val positiveMask: Long,
        override val negativeMask: Long
) : SysBaseInteger(width, value, bitsState, positiveMask, negativeMask) {

    /** Construct from given long value setting minimal possible width */
    private constructor(value: Long, width: Int = widthByValue(value)) :
    this(width, value, true, positiveMask = maxValue(width), negativeMask = minValue(width))

    /**Construct uninitialized sys integer with given width.
     *  Short is used only to male a difference between constructor from Int value*/
    private constructor(width: Short) : this(width.toInt(), 0, false,
            positiveMask = maxValue(width.toInt()),
            negativeMask = minValue(width.toInt()))

    /** Construct from given value and width*/
    constructor(width: Int, value: Long) : this(width, value, bitsState = maskByValue(value, width),
            positiveMask = maxValue(width),
            negativeMask = minValue(width))

    private constructor(width: Int, value: Long, positiveMask: Long, negativeMask: Long) :
    this(width, value, bitsState = maskByValue(value, width),
            positiveMask = positiveMask,
            negativeMask = negativeMask)

    constructor(width: Int, value: Int) : this(width, value.toLong(),
            bitsState = maskByValue(value.toLong(), width),
            positiveMask = maxValue(width),
            negativeMask = minValue(width))

    /**Construct from given SysBit Array*/
    constructor (arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr),
            bitsState = maskBySWSArray(arr),
            positiveMask = maxValue(arr.size),
            negativeMask = minValue(arr.size))

    /** Increase width to the given */
    override fun extend(width: Int): SysInteger {
        if (width < widthByValue(value))
            throw IllegalArgumentException()
        return SysInteger(width, value)
    }

    /** Decrease width to the given with value truncation */
    override fun truncate(width: Int): SysInteger {
        val widthByValue = widthByValue(value)
        if (width >= widthByValue) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        val truncated = value shr (widthByValue - width)
        return SysInteger(width, truncated)
    }

    private fun truncate(width: Int, value: Long, positiveMask: Long, negativeMask: Long): SysInteger {
        if (width == MAX_WIDTH)
        //println(java.lang.Long.toBinaryString(value))
            return SysInteger(width, value, positiveMask, negativeMask)
        if (value >= 0L)
            return SysInteger(width, value and positiveMask, positiveMask, negativeMask)
        else
            return SysInteger(width, value or negativeMask, positiveMask, negativeMask)
    }


    override operator fun plus(arg: Int) = truncate(this.width, this.value + arg, positiveMask, negativeMask)
    override operator fun plus(arg: Long) = truncate(this.width, this.value + arg, positiveMask, negativeMask)
    override operator fun minus(arg: Int) = truncate(this.width, this.value - arg, positiveMask, negativeMask)
    override operator fun minus(arg: Long) = truncate(this.width, this.value - arg, positiveMask, negativeMask)
    override operator fun times(arg: Int) = truncate(this.width, this.value * arg, positiveMask, negativeMask)
    override operator fun times(arg: Long) = truncate(this.width, this.value * arg, positiveMask, negativeMask)
    override operator fun div(arg: Int) = truncate(this.width, this.value / arg, positiveMask, negativeMask)
    override operator fun div(arg: Long) = truncate(this.width, this.value / arg, positiveMask, negativeMask)
    override operator fun mod(arg: Int) = truncate(this.width, this.value / arg, positiveMask, negativeMask)
    override operator fun mod(arg: Long) = truncate(this.width, this.value / arg, positiveMask, negativeMask)
    override operator fun inc() = truncate(this.width, this.value + 1, positiveMask, negativeMask)
    override operator fun dec() = truncate(this.width, this.value - 1, positiveMask, negativeMask)

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    override operator fun plus(arg: SysBaseInteger): SysInteger {
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }
        return truncate(resWidth, value + arg.value.toLong(), posMask, negMask)
    }

    /**Unary minus*/
    override operator fun unaryMinus(): SysInteger {
        return truncate(width, -value, positiveMask, negativeMask)
    }

    /** Subtract arg from this integer*/
    override operator fun minus(arg: SysBaseInteger): SysInteger {
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }
        return truncate(resWidth, value - arg.value.toLong(), posMask, negMask)
    }

    /** Integer division by divisor*/
    override operator fun div(arg: SysBaseInteger): SysInteger {
        if (arg.value == 0L) throw IllegalArgumentException("Division by zero")
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }
        return truncate(resWidth, value / arg.value.toLong(), posMask, negMask)
    }

    /** Remainder of integer division*/
    override operator fun mod(arg: SysBaseInteger): SysInteger {
        if (arg.value == 0L) throw IllegalArgumentException("Division by zero")
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }

        return truncate(resWidth, value % arg.value.toLong(), posMask, negMask)
    }

    /** Multiplies arg to this integer, with result width is sum of argument's width */
    override operator fun times(arg: SysBaseInteger): SysInteger {
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }
        return truncate(resWidth, value * arg.value.toLong(), posMask, negMask)
    }

    override fun power(exp: Int) = truncate(width, Math.pow(value.toDouble(), exp.toDouble()).toLong()
            , positiveMask, negativeMask)

    override fun abs() = SysInteger(width, (if ( value >= 0L) value else -value), bitsState = bitsState,
            positiveMask = positiveMask, negativeMask = negativeMask)

    /** Bitwise logical shift right*/
    override infix fun ushr(shift: Int): SysInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysBitExpression = Array(width - shift) { i: Int -> this[i] }

        return SysInteger(sysBitExpression);
    }

    /** Bitwise logical shift left*/
    override infix fun ushl(shift: Int): SysInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysBitExpression = Array(width - shift) { i: Int -> this[i + shift] }

        return SysInteger(sysBitExpression)
    }

    /** Arithmetic shift right*/
    override infix fun shr(shift: Int): SysInteger {
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
        return SysInteger(sysBitExpression)
    }

    /** Arithmetic shift left*/
    override infix fun shl(shift: Int): SysInteger {
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
        return SysInteger(sysBitExpression)

    }

    /** Cyclic shift right*/
    override infix fun cshr(shift: Int): SysInteger {

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
        return SysInteger(sysBitExpression)
    }

    /** Cyclic shift left*/
    override infix fun cshl(shift: Int): SysInteger {

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
        return SysInteger(sysBitExpression)
    }

    /** Bitwise and*/
    override infix fun and(arg: SysBaseInteger): SysInteger {
        var temp = arg.bitsState;

        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }
        return SysInteger(resWidth, value and arg.value.toLong(), bitsState = temp,
                positiveMask = posMask, negativeMask = negMask)
    }

    /** Bitwise or*/
    override infix fun or(arg: SysBaseInteger): SysInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size))
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }
        return SysInteger(resWidth, value or arg.value.toLong(), bitsState = temp,
                positiveMask = posMask, negativeMask = negMask)

    }

    /** Bitwise xor*/
    override infix fun xor(arg: SysBaseInteger): SysInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and  bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        var resWidth: Int
        var posMask: Long
        var negMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
            negMask = arg.negativeMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
            negMask = negativeMask
        }
        return SysInteger(resWidth, value xor arg.value.toLong(), bitsState = temp,
                positiveMask = posMask, negativeMask = negMask)
    }

    /**Bitwise inversion (not)*/
    override fun inv(): SysInteger {
        return SysInteger(width, value.inv(), bitsState = bitsState,
                positiveMask = positiveMask, negativeMask = negativeMask);
    }

    /** Extracts a single bit, accessible as [i] */
    override operator fun get(i: Int): SysBit {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (!bitsState[i])
            return SysBit.X
        val shift = bitsState.indexOf(true)
        if ((value and (1L shl i - shift)) != 0L)
            return SysBit.ONE
        else
            return SysBit.ZERO
    }

    /** Extracts a range of bits, accessible as [j,i] */
    override operator fun get(j: Int, i: Int): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        var result = value shr i
        return valueOf(result).truncate(j - i + 1)
    }

    override fun toSysInteger() = this

    override fun toSysBigInteger() = SysBigInteger(width, value)

    override fun toInt() = value.toInt()

    override fun toLong() = value

    override fun compareTo(other: SysBaseInteger): Int {
        if (width != other.width) {
            throw IllegalArgumentException("Non comparable. Width not equal.")
        }
        return value.compareTo(other.value.toLong())
    }


    companion object : SysDataCompanion<SysInteger> {

        val MAX_WIDTH: Int = 64

        fun valueOf(value: Long) = SysInteger(value)

        fun uninitialized(width: Int) = SysInteger(width.toShort())

        private fun valueBySWSArray(arr: Array<SysBit>): Long {
            var value: Long = 0L
            var counter: Int = 0;
            while (counter < arr.size && arr[counter] == SysBit.X )
                counter++;
            var shift = 0
            while (counter < arr.size && arr[counter] != SysBit.X) {
                if (arr[counter].one)
                    value = value  or (1L shl shift );
                shift++;
                counter++;
            }
            if (arr[counter - 1].one)
                for (i in 0..64 - shift) {
                    value = value or (1L shl (63 - i))
                }
            return value
        }


        private fun maskBySWSArray(arr: Array<SysBit>): Array<Boolean> {
            val mask = BooleanArray(arr.size)
            for (i in 0..mask.size - 1)
                if (arr[i] != SysBit.X)
                    mask[i] = true;
            return mask.toTypedArray();
        }

        private fun maskByValue(value: Long, width: Int): Array<Boolean> {

            if (width == 0)
                return BooleanArray(0).toTypedArray();

            val widthByValue = widthByValue(value)


            val mask = BooleanArray(width);

            if (width < widthByValue) {
                throw IllegalArgumentException("Width $width is too small for this value \n$value \nwith width $widthByValue")
            } else {
                mask.fill(true, mask.lastIndex + 1 - widthByValue, mask.lastIndex + 1)

            }
            return mask.toTypedArray();
        }


        private fun widthByValue(value: Long): Int {
            var current = value;
            if (current == 0L)
                return 1
            if (current < 0)
                current = current.inv()
            return java.lang.Long.SIZE - java.lang.Long.numberOfLeadingZeros(current) + 1

        }

        private fun minValue(width: Int): Long {
            if (width == 0) return 0
            if (width == MAX_WIDTH) return Long.MIN_VALUE;
            return ((-1L shl (width - 1)))
        }

        private fun maxValue(width: Int): Long {
            if (width == 0) return 0
            if (width == MAX_WIDTH) return Long.MAX_VALUE
            return (1L shl (width - 1)) - 1
        }

        private fun checkWidth(width: Int): Boolean {
            if (width < 0 || width > MAX_WIDTH) return false
            return true
        }

        override val undefined: SysInteger
            get() = SysInteger(arrayOf(SysBit.X))
    }
}

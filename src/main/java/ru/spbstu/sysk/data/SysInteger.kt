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
        bitsState: Array<Boolean> = Array(width, { i -> defaultBitState })
) : SysBaseInteger(width, value, bitsState) {

    /** Construct from given long value setting minimal possible width */
    private constructor(value: Long) : this(widthByValue(value), value, true)

    /**Construct uninitialized sys integer with given width.
     *  Short is used only to male a difference between constructor from Int value*/
    private constructor(width: Short) : this(width.toInt(), 0, false)

    /** Construct from given value and width*/
    constructor(width: Int, value: Long) : this(width, value, bitsState = maskByValue(value, width))
    constructor(width: Int, value: Int) : this(width, value.toLong(), bitsState = maskByValue(value.toLong(), width))

    /**Construct from given SysBit Array*/
    constructor (arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr), bitsState = maskBySWSArray(arr))

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

    private fun truncate(value: Long, width: Int): Long {
        val size = widthByValue(value)
        if (size > width) {
            return value shr (size - width)
        }
        return value
    }


    override operator fun plus(arg: Int) = SysInteger(this.width, truncate(this.value + arg, width))
    override operator fun plus(arg: Long) = SysInteger(this.width, truncate(this.value + arg, width))
    override operator fun minus(arg: Int) = SysInteger(this.width, truncate(this.value - arg, width))
    override operator fun minus(arg: Long) = SysInteger(this.width, truncate(this.value - arg, width))
    override operator fun times(arg: Int) = SysInteger(this.width, truncate(this.value * arg, width))
    override operator fun times(arg: Long) = SysInteger(this.width, truncate(this.value * arg, width))
    override operator fun div(arg: Int) = SysInteger(this.width, truncate(this.value / arg, width))
    override operator fun div(arg: Long) = SysInteger(this.width, truncate(this.value / arg, width))
    override operator fun mod(arg: Int) = SysInteger(this.width, truncate(this.value / arg, width))
    override operator fun mod(arg: Long) = SysInteger(this.width, truncate(this.value / arg, width))
    override operator fun inc() = SysInteger(this.width, this.value + 1)
    override operator fun dec() = SysInteger(this.width, this.value - 1)

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    override operator fun plus(arg: SysBaseInteger): SysInteger {
        val resWidth = Math.min(Math.max(width, arg.width), MAX_WIDTH)
        return SysInteger(resWidth, truncate(value + arg.value.toLong(), resWidth))
    }

    /**Unary minus*/
    override operator fun unaryMinus(): SysInteger {
        return SysInteger(width, -value)
    }

    /** Subtract arg from this integer*/
    override operator fun minus(arg: SysBaseInteger): SysInteger {
        val resWidth = Math.max(width, arg.width)
        return SysInteger(resWidth, truncate(value - arg.value.toLong(), resWidth))
    }

    /** Integer division by divisor*/
    override operator fun div(arg: SysBaseInteger): SysInteger {
        if (arg.value == 0L) throw IllegalArgumentException("Division by zero")
        if (arg.width > width) arg.truncate(width)
        return SysInteger(width, value / arg.value.toLong())
    }

    /** Remainder of integer division*/
    override operator fun mod(arg: SysBaseInteger): SysInteger {
        if (arg.value == 0L) throw IllegalArgumentException("Division by zero")
        return SysInteger(width, value % arg.value.toLong())
    }

    /** Multiplies arg to this integer, with result width is sum of argument's width */
    override operator fun times(arg: SysBaseInteger): SysInteger {
        val resWidth = Math.min(Math.max(width, arg.width), MAX_WIDTH)
        return SysInteger(resWidth, truncate(value * arg.value.toLong(), resWidth))
    }

    override fun power(exp: Int) = SysInteger(width, Math.pow(value.toDouble(), exp.toDouble()).toLong())

    override fun abs() = SysInteger(width, if ( value >= 0L) value else -value, bitsState = bitsState)

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
        return SysInteger(Math.max(width, arg.width), value and arg.value.toLong(), bitsState = temp)
    }

    /** Bitwise or*/
    override infix fun or(arg: SysBaseInteger): SysInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysInteger(Math.max(width, arg.width), value or arg.value.toLong(), bitsState = temp)

    }

    /** Bitwise xor*/
    override infix fun xor(arg: SysBaseInteger): SysInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and  bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysInteger(Math.max(width, arg.width), value xor arg.value.toLong(), bitsState = temp)
    }

    /**Bitwise inversion (not)*/
    override fun inv(): SysInteger {
        return SysInteger(width, value.inv(), bitsState = bitsState);
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

    override fun toSysInteger(): SysInteger {
        return this
    }

    override fun toSysBigInteger(): SysBigInteger {
        return SysBigInteger(width, value)
    }

    override fun compareTo(other: SysBaseInteger): Int {
        if (width != other.width) {
            throw IllegalArgumentException("Non comparable. Width not equal.")
        }
        return value.compareTo(other.value.toLong())
    }

    private fun minValue(): Long {
        if (width == 0) return 0
        if (width == MAX_WIDTH) return Long.MIN_VALUE;
        return -(1L shl (width ))
    }

    private fun maxValue(): Long {
        if (width == 0) return 0
        if (width == MAX_WIDTH) return Long.MAX_VALUE
        return (1L shl (width )) - 1
    }

    private fun checkWidth(): Boolean {
        if (width < 0 || width > MAX_WIDTH) return false
        return true
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

        override val undefined: SysInteger
            get() = SysInteger(arrayOf(SysBit.X))
    }
}

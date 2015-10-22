package sysk

/** Width of integer / unsigned / ... */
@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
public annotation class Width(val value: Int)

/**
 * Immutable class for a fixed-width integer, width can be from 0 to 64, inclusively.
 * Value is stored in a long integer.
 * Width can be checked statically (in future) if type usage is annotated by @width annotation
 */
class SysInteger(
        val width: Int,
        val value: Long,
        defaultBitState: Boolean = false,
        private val bitsState: Array<Boolean> = Array(width, { i -> defaultBitState })
) {
    init {
        if (!checkWidth()) {
            throw IllegalArgumentException()
        }

        if (minValue() > value || value > maxValue()) {
            throw IllegalArgumentException()
        }


        if (bitsState.size() != width) {
            throw IllegalArgumentException()
        }
    }

    /** Construct from given long value setting minimal possible width */
    constructor(value: Long) : this(widthByValue(value), value, true)

    /** Construct from given int value setting maximal possible width */
    constructor(value: Int) : this(widthByValue(value.toLong()), value.toLong(), true)

    /**Construct uninitialized sysinteger with given width.
     *  Short is used only to male a difference between constructor from Int value*/
    private constructor(width: Short) : this(width.toInt(), 0, false)

    /** Construct from given value and width*/
    constructor(width: Int, value: Long) : this(width, value, bitsState = maskByValue(value, width))

    /** Construct from given value and width*/
    constructor(width: Int, value: Int) : this(width, value.toLong(), bitsState = maskByValue(value.toLong(), width))

    /**Construct from given SysWireState Array*/
    constructor (arr: Array<SysWireState>) : this(arr.size(), valueBySWSArray(arr), bitsState = maskBySWSArray(arr))

    /** Increase width to the given */
    fun extend(width: Int): SysInteger {
        if (width < widthByValue(value))
            throw IllegalArgumentException()
        return SysInteger(width, value)
    }

    /** Decrease width to the given with value truncation */
    fun truncate(width: Int): SysInteger {
        val widthByValue = widthByValue(value)
        if (width >= widthByValue) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        val truncated = value shr (widthByValue - width)
        return SysInteger(width, truncated)
    }

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    operator fun plus(arg: SysInteger): SysInteger {
        val resWidth = Math.max(width, arg.width)
        return SysInteger(Math.min(resWidth + 1, MAX_WIDTH), value + arg.value).truncate(resWidth)
    }

    /** Subtract arg from this integer*/
    operator fun minus(arg: SysInteger): SysInteger {
        if (arg.width < width)
            return SysInteger(width, value - arg.value);
        else
            return SysInteger(Math.max(width, arg.width), value - arg.value)
    }

    /** Integer division by divisor*/
    operator fun div(arg: SysInteger): SysInteger {
        if (arg.value == 0L)
            throw IllegalArgumentException("Division by zero");
        if (arg.width > width)
            arg.truncate(width);
        return SysInteger(width, value / arg.value)
    }

    /** Remainder of integer division*/
    operator fun mod(arg: SysInteger): SysInteger {
        if (arg.value == 0L)
            throw IllegalArgumentException("Division by zero");
        if (arg.width > width)
            return this;
        return SysInteger(arg.width, value % arg.value)
    }

    /** Multiplies arg to this integer, with result width is sum of argument's width */
    operator fun times(arg: SysInteger): SysInteger {
        val resWidth = Math.min(width + arg.width, MAX_WIDTH);
        return SysInteger(resWidth, value * arg.value).truncate(resWidth);
    }

//TODO Remake all shifts
    /** Bitwise logical shift right*/
    fun ushr(shift: Int): SysInteger {
        if (shift == 0)
            return this;

        return SysInteger(width, value ushr shift, bitsState = bitsState);
    }

    /** Bitwise logical shift left*/
    fun ushl(shift: Int): SysInteger {
        if (shift == 0)
            return this;
        val tempState = bitsState;
        val start = tempState.lastIndexOf(false)
        for (i in 0..shift)
            tempState[start - i] = true;
        return SysInteger(width, value shl shift, bitsState = tempState);
    }

    /** Arithmetic shift right*/
    fun shr(shift: Int): SysInteger {
        if (shift == 0)
            return this;
        return SysInteger(width, value shr shift, bitsState = bitsState);

    }

    /** Arithmetic shift left*/
    fun shl(shift: Int): SysInteger {
        if (shift == 0)
            return this;
        val tempState = bitsState;
        val start = tempState.lastIndexOf(false)
        for (i in 0..shift)
            tempState[start - i] = true;
        return SysInteger(width, value shl shift, bitsState = tempState);
    }


    //TODO  Remake
    /*
    /** Cyclic shift right*/
    fun cshr(shift: Int): SysInteger {
        if (shift == 0)
            return this;

        val tempState = bitsState;
        val bitShift = bitsState.copyOfRange(bitsState.size() - shift, bitsState.size());
        for (i in tempState.size() - 1..shift) {
            tempState[i] = tempState[i - shift];
        }
        for (i in 0..shift - 1) {
            tempState[i] = bitShift[i]
        }

        val mask = 1L;
        var buff = value;
        var temp = 0L;

        for (i in 0..shift) {
            if (mask and buff == mask)
                temp = temp or (1L shl i);
            buff = buff shr 1;
        }

        temp = temp shl (width - shift);

        buff = buff or temp;

        return SysInteger(width, buff, bitsState = tempState);

    }
*/
    /** Bitwise and*/
    fun and(arg: SysInteger): SysInteger {
        var temp = arg.bitsState;

        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size() < bitsState.size())
            temp = temp.plus(bitsState.copyOfRange(temp.size(), bitsState.size()));
        return SysInteger(Math.max(width, arg.width), value and arg.value, bitsState = temp)
    }

    /** Bitwise or*/
    fun or(arg: SysInteger): SysInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size() < bitsState.size())
            temp = temp.plus(bitsState.copyOfRange(temp.size(), bitsState.size()));
        return SysInteger(Math.max(width, arg.width), value or arg.value, bitsState = temp)

    }

    /** Bitwise xor*/
    fun xor(arg: SysInteger): SysInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and  bitsState[i];
        }
        if (temp.size() < bitsState.size())
            temp = temp.plus(bitsState.copyOfRange(temp.size(), bitsState.size()));
        return SysInteger(Math.max(width, arg.width), value xor arg.value, bitsState = temp)
    }

    /**Bitwise inversion (not)*/
    fun inv(): SysInteger {
        return SysInteger(width, value.inv(), bitsState = bitsState);
    }

    /** Extracts a single bit, accessible as [i] */
    operator fun get(i: Int): SysWireState {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (!bitsState[i])
            return SysWireState.X
        if ((value and (1L shl i)) != 0L)
            return SysWireState.ONE
        else
            return SysWireState.ZERO
    }

    /** Extracts a range of bits, accessible as [j,i] */
    operator fun get(j: Int, i: Int): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        var result = value shr i
        return SysInteger(result).truncate(j - i + 1)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? SysInteger)?.let {
            width == it.width && value == it.value
            //&& bitsState.equals(it.bitsState)  //Don't work
        } ?: false
    }

    override fun hashCode(): Int {
        var result = 13
        result = 19 * result + value.hashCode()
        result = 19 * result + width
        result = 19 * result + bitsState.hashCode()
        return result
    }

    override fun toString(): String {
        return "$value[$width]"
    }

    private fun minValue(): Long {
        if (width == 0) return 0
        if (width == MAX_WIDTH) return Long.MIN_VALUE;
        return -(1L shl (width )) + 1
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

    companion object {

        val MAX_WIDTH: Int = 64

        fun uninitialized(width: Int) = SysInteger(width.toShort())

        private fun valueBySWSArray(arr: Array<SysWireState>): Long {
            var value: Long = 0L
            var counter: Int = 0;
            while (arr[counter] == SysWireState.X)
                counter++;

            while (counter < arr.size() && arr[counter] != SysWireState.X) {
                if (arr[counter] == SysWireState.ONE)
                    value = (value shl 1) or (1L);
                else
                    value = value shl 1;
                counter++;
            }

            return value
        }


        private fun maskBySWSArray(arr: Array<SysWireState>): Array<Boolean> {

            // val mask: Array<Boolean> = Array<Boolean>(size, { false }) ; //Smth wrong with array constructing

            val mask = BooleanArray(arr.size())

            //mask.fill(false);

            for (i in 0..mask.size() - 1)
                if (arr[i] != SysWireState.X)
                    mask[i] = true;

            return mask.toTypedArray();
        }

        private fun maskByValue(value: Long, width: Int): Array<Boolean> {

            if(width==0)
                return BooleanArray(0).toTypedArray();

            val widthByValue = widthByValue(value)

            // val mask = Array<Boolean>(width) { false }; //smth with array

            val mask = BooleanArray(width);

            if (width < widthByValue) {
                throw IllegalArgumentException()
            } else {
                mask.fill(true, mask.lastIndex + 1 - widthByValue, mask.lastIndex + 1)

            }
            return mask.toTypedArray();
        }

        private fun widthByValue(value: Long): Int {
            var result = 0
            var current = 0L;
            if (value == 0L)
                return 1;
            if (value > 0)
                current = value;
            else
                current = -value;
            while (current != 0L) {
                result++
                current = current shr 1
            }
            return result
        }
    }
}

package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysDataCompanion

/** Width of integer / unsigned / ... */
@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class Width(val value: Int)

/**
 * Immutable class for a fixed-width integer, width can be from 0 to 64, inclusively.
 * Value is stored in a long integer.
 * Width can be checked statically (in future) if type usage is annotated by @width annotation
 */
class SysLongInteger private constructor(
        width: Int,
        override val value: Long,
        hasUndefined: Boolean = false,
        override val positiveMask: Long,
        override val negativeMask: Long
) : SysInteger(width, value, hasUndefined, positiveMask, negativeMask) {

    override lateinit var bitsState: Array<SysBit>


    /** Construct from given long value setting minimal possible width */
    private constructor(value: Long, width: Int = widthByValue(value)) :
    this(width, value, false,
            positiveMask = positiveValues[width],
            negativeMask = negativeValues[width])

    /**Construct uninitialized sys integer with given width.
     *  Short is used only to male a difference between constructor from Int value*/
    private constructor(width: Short) : this(width.toInt(), 0, true,
            positiveMask = positiveValues[width.toInt()],
            negativeMask = negativeValues[width.toInt()]) {
        bitsState = Array(width.toInt(), { i -> SysBit.X })
    }

    /** Construct from given value and width*/
    constructor(width: Int, value: Long) : this(width, value, false,
            positiveMask = positiveValues[width],
            negativeMask = negativeValues[width])

    private constructor(width: Int, value: Long, positiveMask: Long, negativeMask: Long) :
    this(width, value, false, positiveMask = positiveMask,
            negativeMask = negativeMask)

    constructor(width: Int, value: Int) : this(width, value.toLong(), false,
            positiveMask = positiveValues[width],
            negativeMask = negativeValues[width])

    /**Construct from given SysBit Array*/
    constructor (arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr), arr.contains(SysBit.X),
            positiveMask = positiveValues[arr.size],
            negativeMask = negativeValues[arr.size]) {
        bitsState = arr
    }

    /** Increase width to the given */
    override fun extend(width: Int): SysInteger {
        if (width < this.width)
            return truncate(width)

        if (width > MAX_WIDTH)
            return SysInteger(width, value)

        return SysLongInteger(width, value)
    }

    /** Decrease width to the given with value truncation */
    override fun truncate(width: Int): SysInteger {
        if (width < 0) throw IllegalArgumentException()
        if (width > MAX_WIDTH)
            return SysInteger(width, value)
        if (width >= this.width) return SysLongInteger(width, value)
        return truncate(width, value, positiveValues[width], negativeValues[width])
    }

    private fun truncate(width: Int, value: Long, positiveMask: Long, negativeMask: Long): SysLongInteger {
        //if (width == MAX_WIDTH)
        //  return SysLongInteger(width, value, positiveMask, negativeMask)
        if (value >= 0L)
            return SysLongInteger(width, value and positiveMask, positiveMask, negativeMask)
        else
            return SysLongInteger(width, value or negativeMask, positiveMask, negativeMask)
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
    override operator fun plus(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return arg + this
        return truncate(width, value + arg.value.toLong(), positiveMask, negativeMask)
    }

    /**Unary minus*/
    override operator fun unaryMinus(): SysLongInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        return truncate(width, -value, positiveMask, negativeMask)
    }

    /** Subtract arg from this integer*/
    override operator fun minus(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return arg - this
        return truncate(width, value - arg.value.toLong(), positiveMask, negativeMask)
    }

    /** Integer division by divisor*/
    override operator fun div(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > MAX_WIDTH)
            return this.extend(arg.width) / arg
        if (arg.value == 0L) throw IllegalArgumentException("Division by zero")

        if (arg.width > width)
            return truncate(arg.width, value / arg.value.toLong(), arg.positiveMask.toLong(), arg.negativeMask.toLong())

        return truncate(width, value / arg.value.toLong(), positiveMask, negativeMask)
    }

    /** Remainder of integer division*/
    override operator fun mod(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > MAX_WIDTH)
            return this.extend(arg.width) % arg
        if (arg.value == 0L) throw IllegalArgumentException("Division by zero")

        if (arg.width > width)
            return truncate(arg.width, value % arg.value.toLong(), arg.positiveMask.toLong(), arg.negativeMask.toLong())
        return truncate(width, value % arg.value.toLong(), positiveMask, negativeMask)
    }


    /** Multiplies arg to this integer, with result width is sum of argument's width */
    override operator fun times(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return arg * this
        return truncate(width, value * arg.value.toLong(), positiveMask, negativeMask)
    }

    override fun power(exp: Int) = truncate(width, Math.pow(value.toDouble(), exp.toDouble()).toLong()
            , positiveMask, negativeMask)

    override fun abs() = SysLongInteger(width, (if ( value >= 0L) value else -value),
            positiveMask = positiveMask, negativeMask = negativeMask)

    /** Bitwise logical shift right*/
    override infix fun ushr(shift: Int): SysLongInteger {
        if (shift == 0)
            return this;
        if (shift > width )
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
            return SysLongInteger(newBitsState)
        }
        if (value >= 0L)
            return SysLongInteger(width, value.ushr(realShift), positiveMask, negativeMask)
        else {
            if (width == MAX_WIDTH)
                return SysLongInteger(width, value.ushr(realShift), positiveMask, negativeMask)

            val result = (value and positiveMask).ushr(realShift)
            return SysLongInteger(width, result, positiveMask, negativeMask)
        }
    }


    /** Arithmetic shift right*/
    override infix fun shr(shift: Int): SysLongInteger {
        if (shift == 0)
            return this;
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
            return SysLongInteger(newBitsState)
        }
        return SysLongInteger(width, value shr shift, positiveMask, negativeMask)
    }

    /** Arithmetic shift left*/
    override infix fun shl(shift: Int): SysLongInteger {
        if (shift == 0)
            return this;
        if (shift > width )
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
            return SysLongInteger(newBitsState)
        }
        return SysLongInteger(width, value shl realShift, positiveMask, negativeMask)
    }

    /** Cyclic shift right*/
    override infix fun cshr(shift: Int): SysLongInteger {

        if (shift < 0)
            return this cshl -shift

        val realShift = shift % width

        if (realShift == 0)
            return this;

        return ((this ushr realShift)or(this shl -realShift))

    }

    /** Cyclic shift left*/
    override infix fun cshl(shift: Int): SysLongInteger {

        if (shift < 0)
            return this cshr -shift
        val realShift = shift % width

        if (realShift == 0)
            return this;

        return ((this shl realShift)or(this ushr -realShift))
    }

    /** Bitwise and*/
    override infix fun and(arg: SysInteger): SysLongInteger {
        if (arg.width > MAX_WIDTH)
            throw UnsupportedOperationException("Not implemented")

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

            return SysLongInteger(resultState)
        }

        if (arg.width > width)
            return SysLongInteger(arg.width, value and arg.value.toLong(),
                    arg.positiveMask.toLong(), arg.negativeMask.toLong())

        return SysLongInteger(width, value and arg.value.toLong(),
                positiveMask, negativeMask)
    }

    /** Bitwise or*/
    override infix fun or(arg: SysInteger): SysLongInteger {

        if (arg.width > MAX_WIDTH)
            throw UnsupportedOperationException("Not implemented")

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

            return SysLongInteger(resultState)
        }



        if (arg.width > width)
            return SysLongInteger(arg.width, value or arg.value.toLong(),
                    arg.positiveMask.toLong(), arg.negativeMask.toLong())

        return SysLongInteger(width, value or arg.value.toLong(),
                positiveMask, negativeMask)
    }

    /** Bitwise xor*/
    override infix fun xor(arg: SysInteger): SysLongInteger {


        if (arg.width > MAX_WIDTH)
            throw UnsupportedOperationException("Not implemented")

        if (hasUndefined || arg.hasUndefined) {

            val state = (if (hasUndefined) bitsState else Array(width, { i -> get(i) }))
            val argState = (if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg[i] }))
            val resultState: Array<SysBit>
            if (state.size > argState.size) {
                resultState = state
                for (i in argState.indices)
                    resultState[i] = resultState[i]xor  argState[i]
            } else {
                resultState = argState
                for (i in state.indices)
                    resultState[i] = resultState[i]xor state[i]
            }

            return SysLongInteger(resultState)
        }

        if (arg.width > width)
            return SysLongInteger(arg.width, value xor  arg.value.toLong(),
                    arg.positiveMask.toLong(), arg.negativeMask.toLong())

        return SysLongInteger(width, value xor  arg.value.toLong(),
                positiveMask, negativeMask)
    }

    /**Bitwise inversion (not)*/
    override fun inv(): SysLongInteger {
        if (hasUndefined) {
            val resultState = Array(bitsState.size, { i -> bitsState[i].not() })
            return SysLongInteger(resultState)
        }
        return SysLongInteger(width, value.inv(),
                positiveMask = positiveMask, negativeMask = negativeMask);
    }

    /** Extracts a single bit, accessible as [i] */
    override operator fun get(i: Int): SysBit {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined)
            return bitsState[i]
        if ((value and (1L shl i)) != 0L)
            return SysBit.ONE
        else
            return SysBit.ZERO
    }

    /** Extracts a range of bits, accessible as [j,i] */
    override operator fun get(j: Int, i: Int): SysLongInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        val result = value shr i
        val resWidth = j - i + 1
        return truncate(resWidth, result, positiveValues[resWidth], negativeValues[resWidth])
    }

    override fun set(j: Int, i: Int, bits: Array<SysBit>): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        if ((j - i) != bits.size) throw IllegalArgumentException()

        if (hasUndefined) {
            val newState = bitsState.copyOf()
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return SysLongInteger(newState)
        }
        if (bits.contains(SysBit.X)) {
            val newState = Array(width, { i -> get(i) })
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return SysLongInteger(newState)
        }
        var newValue = value
        for (a in 0..j - i)
            if (bits[a] == SysBit.ONE) {
                newValue = newValue or (1L shl (i + a))
            } else {
                newValue = newValue and (-1L xor (1L shl (i + a)))
            }
        return SysLongInteger(width, newValue, positiveMask, negativeMask)
    }

    override fun set(i: Int, bit: SysBit): SysLongInteger {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined) {
            val newState = bitsState.copyOf()
            newState[i] = bit
            return SysLongInteger(newState)
        }
        if (bit == SysBit.X) {
            val newState = Array(width, { i -> get(i) })
            newState[i] = bit
            return SysLongInteger(newState)
        }
        var newValue = value
        if (bit == SysBit.ONE) {
            newValue = newValue or (1L shl i)
        } else {
            newValue = newValue and (-1L xor (1L shl i))
        }
        return SysLongInteger(width, newValue, positiveMask, negativeMask)
    }

    override fun bits(): Array<SysBit> {
        if (hasUndefined)
            return bitsState
        return Array(width, { i -> get(i) })
    }

    override fun toSysLongInteger() = this

    override fun toSysBigInteger() = SysBigInteger(width, value)

    override fun toInt() = value.toInt()

    override fun toLong() = value

    override fun compareTo(other: SysInteger): Int {
        if (width != other.width) {
            throw IllegalArgumentException("Non comparable. Width not equal.")
        }
        return value.compareTo(other.value.toLong())
    }


    companion object : SysDataCompanion<SysLongInteger> {

        const val MAX_WIDTH: Int = 64

        private val positiveValues = LongArray(MAX_WIDTH + 1, { i -> maxValue(i) })

        private val negativeValues = LongArray(MAX_WIDTH + 1, { i -> minValue(i) })

        fun valueOf(value: Long) = SysLongInteger(value)

        //fun uninitialized(width: Int) = SysLongInteger(width.toShort())

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



        //        private fun maskBySWSArray(arr: Array<SysBit>): Array<Boolean> {
        //            val mask = BooleanArray(arr.size)
        //            for (i in 0..mask.size - 1)
        //                if (arr[i] != SysBit.X)
        //                    mask[i] = true;
        //            return mask.toTypedArray();
        //        }

        //        private fun maskByValue(value: Long, width: Int): Array<Boolean> {
        //
        //                        if (width == 0)
        //                            return BooleanArray(0).toTypedArray();
        //
        //                        val widthByValue = widthByValue(value)
        //
        //
        //                        val mask = BooleanArray(width);
        //
        //                        if (width < widthByValue) {
        //                            throw IllegalArgumentException("Width $width is too small for this value \n$value \nwith width $widthByValue")
        //                        } else {
        //                            mask.fill(true, mask.lastIndex + 1 - widthByValue, mask.lastIndex + 1)
        //
        //                        }
        //                        return mask.toTypedArray();
        //
        //            return Array(width, { i -> true })
        //        }


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

        //        private fun checkWidth(width: Int): Boolean {
        //            if (width < 0 || width > MAX_WIDTH) return false
        //            return true
        //        }

        override val undefined: SysLongInteger
            get() = SysLongInteger(arrayOf(SysBit.X))
    }
}

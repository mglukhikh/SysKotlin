package sysk

import java.math.BigInteger


class SysBigInteger(
        val width: Int,
        val value: BigInteger,
        defaultBitState: Boolean = false,
        private val bitsState: Array<Boolean> = Array(width, { i -> defaultBitState })) {

    init {


        if (bitsState.size != width) {
            throw IllegalArgumentException()
        }

    }

    constructor(width: Int, value: BigInteger) : this(width, value, bitsState = maskByValue(value, width))

    constructor(sysInteger: SysInteger) : this(sysInteger.width, BigInteger.valueOf(sysInteger.value),
            bitsState = maskByValue(BigInteger.valueOf(sysInteger.value), sysInteger.width))

    constructor(width: Int, value: Long) : this(width, BigInteger.valueOf(value),
            bitsState = maskByValue(BigInteger.valueOf(value), width))

    constructor(width: Int, value: Int) : this(width, BigInteger.valueOf(value.toLong()),
            bitsState = maskByValue(BigInteger.valueOf(value.toLong()), width))


    constructor(value: BigInteger) : this(value.bitLength() + 1, value,
            bitsState = maskByValue(value, value.bitLength() + 1))

    constructor(value: Long) : this(SysInteger(value))

    constructor(value: Int) : this(SysInteger(value))

    constructor(arr: Array<SysWireState>) : this(arr.size, valueBySWSArray(arr),
            bitsState = maskBySWSArray(arr))

    private constructor(width: Short) : this(width.toInt(), BigInteger.valueOf(0), false)

    /** Increase width to the given */
    fun extend(width: Int): SysBigInteger {
        if (width < value.bitLength())
            throw IllegalArgumentException()
        return SysBigInteger(width, value)
    }

    /** Decrease width to the given with value truncation */
    fun truncate(width: Int): SysBigInteger {
        val widthByValue = value.bitLength() + 1
        if (width >= widthByValue) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        val truncated = value.shiftRight(widthByValue - width)
        return SysBigInteger(width, truncated)
    }

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    operator fun plus(arg: SysBigInteger): SysBigInteger {
        val resWidth = Math.max(width, arg.width)
        return SysBigInteger(resWidth, value.add(arg.value)).truncate(resWidth)
    }


    /**Unary minus*/
    operator fun unaryMinus(): SysBigInteger {
        return SysBigInteger(value.negate()).truncate(this.width)
    }

    /** Subtract arg from this integer*/
    operator fun minus(arg: SysBigInteger): SysBigInteger {
        return SysBigInteger(Math.max(width, arg.width), value.subtract(arg.value))
    }

    /** Integer division by divisor*/
    operator fun div(arg: SysBigInteger): SysBigInteger {
        if (arg.value == BigInteger.ZERO) throw IllegalArgumentException("Division by zero")
        val argCopy = arg
        if (arg.width > width) argCopy.truncate(width)
        return SysBigInteger(width, value.divide(argCopy.value))
    }

    /** Remainder of integer division*/
    operator fun mod(arg: SysBigInteger): SysBigInteger {
        if (arg.value == BigInteger.ZERO) throw IllegalArgumentException("Division by zero")
        if (arg.width > width) return this
        return SysBigInteger(arg.width, value.remainder(arg.value))
    }

    /** Multiplies arg to this integer, with result width is sum of argument's width */
    operator fun times(arg: SysBigInteger): SysBigInteger {
        val resWidth = width + arg.width
        return SysBigInteger(resWidth, value.multiply(arg.value)).truncate(resWidth)
    }

    /**
     * Bitwise and
     * */
    infix fun and(arg: SysBigInteger): SysBigInteger {
        var temp = arg.bitsState;

        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysBigInteger(Math.max(width, arg.width), value.and(arg.value), bitsState = temp)

    }


    /** Bitwise or*/
    infix fun or(arg: SysBigInteger): SysBigInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysBigInteger(Math.max(width, arg.width), value.or(arg.value), bitsState = temp)

    }

    /** Bitwise xor*/
    infix fun xor(arg: SysBigInteger): SysBigInteger {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and  bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysBigInteger(Math.max(width, arg.width), value.xor(arg.value), bitsState = temp)
    }

    /**Bitwise inversion (not)*/
    fun inv(): SysBigInteger {
        return SysBigInteger(width, value.not(), bitsState = bitsState);
    }

    /** Extracts a single bit, accessible as [i] */
    operator fun get(i: Int): SysWireState {

        if (i < 0 || i >= width) throw IndexOutOfBoundsException()

        if (!bitsState[i])
            return SysWireState.X
        val shift = bitsState.indexOf(true)
        if (value.testBit(i - shift))
            return SysWireState.ONE
        else
            return SysWireState.ZERO

    }

    /** Extracts a range of bits, accessible as [j,i] */
    operator fun get(j: Int, i: Int): SysBigInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        var result = value.shiftRight(i)
        return SysBigInteger(result).truncate(j - i + 1)
    }


    /** Bitwise logical shift right*/
    infix fun ushr(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysWireStateExpression = Array<SysWireState>(width - shift) { i: Int -> this[i] }

        return SysBigInteger(sysWireStateExpression);
    }

    /** Bitwise logical shift left*/
    infix fun ushl(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysWireStateExpression = Array<SysWireState>(width - shift) { i: Int -> this[i + shift] }

        return SysBigInteger(sysWireStateExpression)
    }

    /** Arithmetic shift right*/
    infix fun shr(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()
        val sysWireStateExpression = Array<SysWireState>(width) { i -> this[i] }
        var i = width - 1
        while (i >= shift) {
            sysWireStateExpression[i] = sysWireStateExpression[i - shift]
            i--
        }
        while (i >= 0) {
            sysWireStateExpression[i] = SysWireState.ZERO
            i--
        }
        return SysBigInteger(sysWireStateExpression)
    }

    /** Arithmetic shift left*/
    infix fun shl(shift: Int): SysBigInteger {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()
        val sysWireStateExpression = Array<SysWireState>(width) { i -> this[i] }
        var i = 0
        while (i < sysWireStateExpression.size - shift) {
            sysWireStateExpression[i] = sysWireStateExpression[i + shift]
            i++
        }
        while (i < sysWireStateExpression.size) {
            sysWireStateExpression[i] = SysWireState.ZERO
            i++
        }
        return SysBigInteger(sysWireStateExpression)

    }

    /** Cyclic shift right*/
    infix fun cshr(shift: Int): SysBigInteger {

        if (shift < 0)
            return this cshl -shift

        val realShift = shift % width

        if (realShift == 0)
            return this;
        val sysWireStateExpression = Array<SysWireState>(width) { i -> this[i] }

        val tempArray = Array(realShift, { SysWireState.X })

        for (i in 0..realShift - 1) {
            tempArray[i] = sysWireStateExpression[sysWireStateExpression.size - realShift + i]
        }
        var i = sysWireStateExpression.size - 1
        while (i >= realShift) {
            sysWireStateExpression[i] = sysWireStateExpression[i - realShift]
            i--
        }
        while (i >= 0) {
            sysWireStateExpression[i] = tempArray[i]
            i--
        }
        return SysBigInteger(sysWireStateExpression)
    }

    /** Cyclic shift left*/
    infix fun cshl(shift: Int): SysBigInteger {

        if (shift < 0)
            return this cshr -shift
        val realShift = shift % width

        if (realShift == 0)
            return this;
        val sysWireStateExpression = Array<SysWireState>(width) { i -> this[i] }

        val tempArray = Array(realShift, { SysWireState.X })

        for (i in 0..realShift - 1) {
            tempArray[i] = sysWireStateExpression[i]
        }

        var i = 0
        while (i < sysWireStateExpression.size - shift) {
            sysWireStateExpression[i] = sysWireStateExpression[i + shift]
            i++
        }
        while (i < sysWireStateExpression.size) {
            sysWireStateExpression[i] = tempArray[i - sysWireStateExpression.size + realShift]
            i++
        }
        return SysBigInteger(sysWireStateExpression)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SysBigInteger

        if (!width.equals(other.width)) return false
        if (!value.equals(other.value)) return false
        // if (!bitsState.equals(other.bitsState)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width * 31
        result += 31 * result + value.hashCode()
        result += 31 * result + bitsState.hashCode()
        return result
    }

    override fun toString(): String {
        return "$value[$width]"
    }

    companion object {

        fun uninitialized(width: Int) = SysBigInteger(width.toShort())


        private fun maskByValue(value: BigInteger, width: Int): Array<Boolean> {

            if (width == 0)
                return BooleanArray(0).toTypedArray();

            val widthByValue = value.bitLength() + 1
            val mask = BooleanArray(width);

            if (width < widthByValue) {
                throw IllegalArgumentException()
            } else {
                mask.fill(true, mask.lastIndex + 1 - widthByValue, mask.lastIndex + 1)

            }
            return mask.toTypedArray();
        }


        private fun valueBySWSArray(arr: Array<SysWireState>): BigInteger {
            /*
                var counter: Int = 0;
                while (counter < arr.size && arr[counter] == SysWireState.X )
                    counter++;
                var shift = 0
                if (arr.size - counter == 0)
                    return BigInteger.ZERO
                var result = BigInteger.ZERO
                while (counter < arr.size && arr[counter] != SysWireState.X) {
                    if (arr[counter] == SysWireState.ONE)
                        result = result.setBit(shift)
                    shift++;
                    counter++;
                }

                return result
            */

            var leftShift = 0;
            var rightShift = 0;
            while (leftShift < arr.size && arr[leftShift] == SysWireState.X )
                leftShift++;

            while (arr.size - rightShift > leftShift && arr[arr.size - 1 - rightShift] == SysWireState.X )
                rightShift++;


            if (leftShift + rightShift == arr.size)
                return BigInteger.ZERO

            var result = BigInteger.ZERO


            val tempArray = arr.copyOfRange(leftShift, arr.size - rightShift)
            if (tempArray.last() != SysWireState.ONE) {
                for (i in tempArray.indices) {
                    if (tempArray[i] == SysWireState.ONE) {
                        result = result.setBit(i)
                    }
                }
                return result

            } else {

                val inverseArray = inverseSWSArray(tempArray)

                for (i in inverseArray.indices) {
                    if (inverseArray[i] == SysWireState.ONE) {
                        result = result.setBit(i)
                    }
                }
                result = result.negate()
                return result
            }



            /*
            var counter = tempArray.size / SIZE_OF_LONG

            var lastBlockSize = tempArray.size % SIZE_OF_LONG

            if (lastBlockSize != 0) {
                counter++
            } else {
                counter--
                lastBlockSize = SIZE_OF_LONG
            }*/
            /*
            var tempInteger: SysBigInteger

            for (i in 0..counter - 1) {
                tempInteger = SysBigInteger(SysInteger(tempArray.copyOfRange(i * SysInteger.MAX_WIDTH,
                        (i + 1) * SysInteger.MAX_WIDTH)))

                result = result.shiftLeft(SysInteger.MAX_WIDTH)
                result=result.add(tempInteger)
            }
            */
            /*
            var tempValue: BigInteger
            var i = 0
            while (i < (counter - 1)) {
                result.shiftLeft(SIZE_OF_LONG)
                tempValue = BigInteger.valueOf(longValueBySWSArray(tempArray.copyOfRange(i * SIZE_OF_LONG,
                        (i + 1) * SIZE_OF_LONG)))

                result = result.add(tempValue)
                i++
            }

            result.shiftLeft(lastBlockSize)
            tempValue = BigInteger.valueOf(longValueBySWSArray(tempArray.copyOfRange((counter - 1) * SIZE_OF_LONG,
                    (counter - 1) * SIZE_OF_LONG + lastBlockSize)))

            result = result.add(tempValue)

            return result
        */

        }

        private fun inverseSWSArray(arr: Array<SysWireState>): Array<SysWireState> {

            val result = arr;
            for (i in result.indices) {
                if (result[i] == SysWireState.ZERO) {
                    result[i] = SysWireState.ONE
                } else {
                    result[i] = SysWireState.ZERO
                }
            }

            var breaker = true;
            var i = 0;
            while (breaker && i < result.size ) {

                if (result[i] == SysWireState.ZERO) {
                    result[i] = SysWireState.ONE
                    breaker = false
                } else {

                    result[i] = SysWireState.ZERO

                }
                i++
            }
            return result
        }

        /*
       private fun longValueBySWSArray(arr: Array<SysWireState>): Long {
           var value: Long = 0L
           var counter: Int = 0;
           /*
           while (counter < arr.size && arr[counter] == SysWireState.X )
               counter++;
           */
           var shift = 0
           while (counter < arr.size && arr[counter] != SysWireState.X) {
               if (arr[counter] == SysWireState.ONE)
                   value = value  or (1L shl shift );
               shift++;
               counter++;
           }

           return value
       }
    */
        private fun maskBySWSArray(arr: Array<SysWireState>): Array<Boolean> {


            val mask = BooleanArray(arr.size)


            for (i in 0..mask.size - 1)
                if (arr[i] != SysWireState.X)
                    mask[i] = true;

            return mask.toTypedArray();
        }


    }
}
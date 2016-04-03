package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysDataCompanion

class SysUnsigned
private constructor(
        width: Int,
        override val value: Long,
        hasUndefined: Boolean = false,
        override val positiveMask: Long,
        override val negativeMask: Long = Long.MIN_VALUE
) : SysInteger(width, value, hasUndefined, positiveMask, negativeMask) {

    override lateinit var bitsState: Array<SysBit>

    private constructor(width: Int, value: Long) : this(width, value,
            positiveMask = maxValue(width))

    private constructor(width: Int, value: Long, positiveMask: Long) :
    this(width, value, false,
            positiveMask = positiveMask)

    private constructor(arr: Array<SysBit>) :
    this(arr.size, valueBySWSArray(arr), arr.contains(SysBit.X), positiveValues[arr.size]) {
        bitsState = arr
    }

    //    private constructor(width: Int, value: Long, bitsState: Array<Boolean>) :
    //    this(width, value, bitsState = bitsState,
    //            positiveMask = maxValue(width))

    override fun extend(width: Int): SysInteger {
        if (width < this.width)
            throw IllegalArgumentException()
        if (width > MAX_WIDTH)
            return SysBigInteger(width, value)
        return SysUnsigned(width, value)
    }

    override fun truncate(width: Int): SysInteger {
        if (width > this.width) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        return truncate(width, value, positiveValues[width])
    }

    private fun truncate(width: Int, value: Long, positiveMask: Long): SysUnsigned {
        if (width == MAX_WIDTH)
            return SysUnsigned(width, value, positiveMask)
        else
            return SysUnsigned(width, value and positiveMask, positiveMask)
    }

    operator fun plus(arg: SysUnsigned): SysUnsigned {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return truncate(arg.width, value + arg.value, arg.positiveMask)
        else
            return truncate(width, value + arg.value, positiveMask)
    }

    operator fun minus(arg: SysUnsigned): SysUnsigned {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return truncate(arg.width, value - arg.value, arg.positiveMask)
        else
            return truncate(width, value - arg.value, positiveMask)
    }

    operator fun times(arg: SysUnsigned): SysUnsigned {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return truncate(arg.width, value * arg.value, arg.positiveMask)
        else
            return truncate(width, value * arg.value, positiveMask)
    }

    operator fun div(arg: SysUnsigned): SysUnsigned {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return truncate(arg.width, java.lang.Long.divideUnsigned(value, arg.value), arg.positiveMask)
        else
            return truncate(width, java.lang.Long.divideUnsigned(value, arg.value), positiveMask)
    }

    operator fun mod(arg: SysUnsigned): SysUnsigned {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > width)
            return truncate(arg.width, java.lang.Long.remainderUnsigned (value, arg.value), arg.positiveMask)
        else
            return truncate(width, java.lang.Long.remainderUnsigned(value, arg.value), positiveMask)
    }

    override operator fun plus(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > MAX_WIDTH)
            return toSysBigInteger() + arg
        return toSysLongInteger() + arg
    }

    override operator fun minus(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > MAX_WIDTH)
            return toSysBigInteger() - arg
        return toSysLongInteger() - arg
    }

    override operator fun times(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > MAX_WIDTH)
            return toSysBigInteger() * arg
        return toSysLongInteger() * arg
    }

    override operator fun div(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > MAX_WIDTH)
            return toSysBigInteger() / arg
        return toSysLongInteger() / arg
    }

    override operator fun mod(arg: SysInteger): SysInteger {
        if (hasUndefined)
            throw UnsupportedOperationException("Not implemented yet")
        if (arg.width > MAX_WIDTH)
            return toSysBigInteger() % arg
        return toSysLongInteger() % arg
    }

    override operator fun inc(): SysUnsigned = this + valueOf(width, 1)
    override operator fun dec(): SysUnsigned = this - valueOf(width, 1)

    override operator fun get(i: Int): SysBit {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined)
            return bitsState[i]
        if ((value and (1L shl i )) != 0L)
            return SysBit.ONE
        else
            return SysBit.ZERO
    }

    override operator fun get(j: Int, i: Int): SysUnsigned {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        val result = value shr i
        val resWidth = j - i + 1
        return truncate(resWidth, result, positiveValues[resWidth])
    }

    /** Bitwise logical shift right*/
    override infix fun ushr(shift: Int): SysUnsigned {
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
            return SysUnsigned(newBitsState)
        }

        return SysUnsigned(width, value.ushr(realShift), false, positiveMask, negativeMask)

    }


    /** Arithmetic shift left*/
    override infix fun shl(shift: Int): SysUnsigned {
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
            return SysUnsigned(newBitsState)
        }
        return SysUnsigned(width, value shl realShift, false, positiveMask, negativeMask)
    }

    /** Cyclic shift right*/
    override infix fun cshr(shift: Int): SysUnsigned {

        if (shift < 0)
            return this cshl -shift

        val realShift = shift % width

        if (realShift == 0)
            return this;

        return ((this ushr realShift)or(this shl -realShift))

    }

    /** Cyclic shift left*/
    override infix fun cshl(shift: Int): SysUnsigned {

        if (shift < 0)
            return this cshr -shift
        val realShift = shift % width

        if (realShift == 0)
            return this;

        return ((this shl realShift)or(this ushr -realShift))
    }

    /** Bitwise and*/
    override infix fun and(arg: SysInteger): SysUnsigned {
        if (arg.width > MAX_WIDTH)
            throw UnsupportedOperationException("Not implemented")

        if (hasUndefined || arg.hasUndefined) {

            val state = (if (hasUndefined) bitsState else Array(width, { i -> get(i) }))
            val argState = (if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg.get(i) }))
            var resultState: Array<SysBit>
            if (state.size > argState.size) {
                resultState = state
                for (i in argState.indices)
                    resultState[i] = resultState[i]and argState[i]
            } else {
                resultState = argState
                for (i in state.indices)
                    resultState[i] = resultState[i]and state[i]
            }

            return SysUnsigned(resultState)
        }

        if (arg.width > width)
            return SysUnsigned(arg.width, value and arg.value.toLong(), false,
                    arg.positiveMask.toLong(), arg.negativeMask.toLong())

        return SysUnsigned(width, value and arg.value.toLong(), false,
                positiveMask, negativeMask)
    }

    /** Bitwise or*/
    override infix fun or(arg: SysInteger): SysUnsigned {

        if (arg.width > MAX_WIDTH)
            throw UnsupportedOperationException("Not implemented")

        if (hasUndefined || arg.hasUndefined) {

            val state = (if (hasUndefined) bitsState else Array(width, { i -> get(i) }))
            val argState = (if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg.get(i) }))
            var resultState: Array<SysBit>
            if (state.size > argState.size) {
                resultState = state
                for (i in argState.indices)
                    resultState[i] = resultState[i]or argState[i]
            } else {
                resultState = argState
                for (i in state.indices)
                    resultState[i] = resultState[i]or state[i]
            }

            return SysUnsigned(resultState)
        }



        if (arg.width > width)
            return SysUnsigned(arg.width, value or arg.value.toLong(), false,
                    arg.positiveMask.toLong(), arg.negativeMask.toLong())

        return SysUnsigned(width, value or arg.value.toLong(), false,
                positiveMask, negativeMask)
    }

    /** Bitwise xor*/
    override infix fun xor(arg: SysInteger): SysUnsigned {


        if (arg.width > MAX_WIDTH)
            throw UnsupportedOperationException("Not implemented")

        if (hasUndefined || arg.hasUndefined) {

            val state = (if (hasUndefined) bitsState else Array(width, { i -> get(i) }))
            val argState = (if (arg.hasUndefined) arg.bitsState else Array(arg.width, { i -> arg.get(i) }))
            var resultState: Array<SysBit>
            if (state.size > argState.size) {
                resultState = state
                for (i in argState.indices)
                    resultState[i] = resultState[i]xor  argState[i]
            } else {
                resultState = argState
                for (i in state.indices)
                    resultState[i] = resultState[i]xor state[i]
            }

            return SysUnsigned(resultState)
        }

        if (arg.width > width)
            return SysUnsigned(arg.width, value xor  arg.value.toLong(), false,
                    arg.positiveMask.toLong(), arg.negativeMask.toLong())

        return SysUnsigned(width, value xor  arg.value.toLong(), false,
                positiveMask, negativeMask)
    }

    /**Bitwise inversion (not)*/
    override fun inv(): SysUnsigned {
        if (hasUndefined) {
            val resultState = Array(bitsState.size, { i -> bitsState[i].not() })
            return SysUnsigned(resultState)
        }
        return SysUnsigned(width, value.inv(),
                positiveMask = positiveMask, negativeMask = negativeMask);
    }

    override fun unaryMinus(): SysInteger {
        return SysInteger.valueOf(width + 1, -value)
    }

    override fun plus(arg: Int) = this + valueOf(32, arg)

    override fun plus(arg: Long) = this + valueOf(64, arg)

    override fun minus(arg: Int) = this - valueOf(32, arg)

    override fun minus(arg: Long) = this - valueOf(64, arg)

    override fun times(arg: Int) = this * valueOf(32, arg)

    override fun times(arg: Long) = this * valueOf(64, arg)

    override fun div(arg: Int) = this / valueOf(32, arg)

    override fun div(arg: Long) = this / valueOf(64, arg)

    override fun mod(arg: Int) = this % valueOf(32, arg)

    override fun mod(arg: Long) = this % valueOf(64, arg)

    override fun power(exp: Int) = truncate(width, Math.pow(this.value.toDouble(), exp.toDouble()).toLong(), positiveMask)

    override fun abs() = this

    //override fun shl(shift: Int) = this ushl shift

    override fun shr(shift: Int) = this ushr shift

    override fun set(j: Int, i: Int, bits: Array<SysBit>): SysUnsigned {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        if ((j - i + 1) != bits.size) throw IllegalArgumentException()

        if (hasUndefined) {
            val newState = bitsState.copyOf()
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return SysUnsigned(newState)
        }
        if (bits.contains(SysBit.X)) {
            val newState = Array(width, { i -> get(i) })
            for (a in 0..bits.lastIndex)
                newState[i + a] = bits[a]
            return SysUnsigned(newState)
        }
        var newValue = value
        for (a in 0..j - i)
            if (bits[a] == SysBit.ONE) {
                newValue = newValue or (1L shl (i + a))
            } else {
                newValue = newValue and ((1L shl i).inv())
            }
        return SysUnsigned(width, newValue, positiveMask)
    }

    override fun set(i: Int, bit: SysBit): SysUnsigned {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (hasUndefined) {
            val newState = bitsState.copyOf()
            newState[i] = bit
            return SysUnsigned(newState)
        }
        if (bit == SysBit.X) {
            val newState = Array(width, { i -> get(i) })
            newState[i] = bit
            return SysUnsigned(newState)
        }
        var newValue = value
        if (bit == SysBit.ONE) {
            newValue = newValue or (1L shl i)
        } else {
            newValue = newValue and ((1L shl i).inv())
        }
        return SysUnsigned(width, newValue, positiveMask)
    }

    override fun bits(): Array<SysBit> {
        if (hasUndefined)
            return bitsState
        return Array(width, { i -> get(i) })
    }


    override fun toSysLongInteger() = SysLongInteger.valueOf(value)

    override fun toSysBigInteger() = SysBigInteger.valueOf(value)

    override fun toInt() = value.toInt()

    override fun toLong() = value

    override fun compareTo(other: SysInteger): Int {
        if (width != other.width) {
            throw IllegalArgumentException("Non comparable. Width not equal.")
        }
        return value.compareTo(other.value.toLong())
    }


    companion object : SysDataCompanion<SysUnsigned> {

        const val MAX_WIDTH = 64

        val positiveValues = Array(MAX_WIDTH + 1, { i -> maxValue(i) })

        fun valueOf(width: Int, value: Int): SysUnsigned {
            val lValue = Integer.toUnsignedLong(value)
            val realWidth = widthByValue(lValue)
            if (realWidth > width)
                throw IllegalArgumentException("Minimal possible width is $realWidth")
            return SysUnsigned(width, lValue)
        }

        fun valueOf(arr: Array<SysBit>): SysUnsigned {
            return SysUnsigned(arr)
        }

        //maybe some bugs
        fun valueOf(width: Int, value: Long): SysUnsigned {
            val realWidth = widthByValue(value)
            if (realWidth > width)
                throw IllegalArgumentException("Minimal possible width is $realWidth")
            return SysUnsigned(width, value)
        }

        private fun valueBySWSArray(arr: Array<SysBit>): Long {
            val value = CharArray(64, { i -> '0' })
            var counter: Int = 0;
            while (counter < arr.size && arr[counter] == SysBit.X )
                counter++;
            var shift = 0
            while (counter < arr.size && arr[counter] != SysBit.X) {
                if (arr[counter].one)
                    value[value.lastIndex - shift] = '1'
                shift++;
                counter++;
            }
            return java.lang.Long.parseUnsignedLong(String(value), 2)
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

        //        private fun maskByValue(width: Int, minimalWidth: Int): Array<Boolean> {
        //
        //            if (width == 0)
        //                return BooleanArray(0).toTypedArray();
        //
        //            val mask = BooleanArray(width);
        //
        //            if (width < minimalWidth) {
        //                throw IllegalArgumentException("width = $width, byValue = $minimalWidth")
        //            } else {
        //                mask.fill(true, mask.lastIndex + 1 - minimalWidth, mask.lastIndex + 1)
        //            }
        //            return mask.toTypedArray();
        //        }

        private fun widthByValue(value: Long): Int {
            return java.lang.Long.SIZE - java.lang.Long.numberOfLeadingZeros(value)
        }

        override val undefined: SysUnsigned
            get() = Companion.valueOf(arrayOf(SysBit.X))


        private fun maxValue(width: Int): Long {
            if (width == 0) return 0
            return (-1L ushr (MAX_WIDTH - width ))
        }
    }

}


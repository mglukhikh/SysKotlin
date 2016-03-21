package ru.spbstu.sysk.data

class SysUnsigned
private constructor(
        width: Int,
        override val value: Long,
        defaultBitState: Boolean = false,
        bitsState: Array<Boolean> = Array(width, { i -> defaultBitState }),
        override val positiveMask: Long,
        override val negativeMask: Long = Long.MIN_VALUE
) : SysBaseInteger(width, value, bitsState, positiveMask, negativeMask) {

    private constructor(width: Int, value: Long) : this(width, value, bitsState = maskByValue(width, widthByValue(value)),
            positiveMask = maxValue(width))

    private constructor(width: Int, value: Long, positiveMask: Long) :
    this(width, value, bitsState = maskByValue(width, widthByValue(value)),
            positiveMask = positiveMask)

    private constructor(width: Int, value: Long, bitsState: Array<Boolean>) :
    this(width, value, bitsState = bitsState,
            positiveMask = maxValue(width))

    override fun extend(width: Int): SysUnsigned {
        if (width < widthByValue(value))
            throw IllegalArgumentException()
        return SysUnsigned(width, value)
    }

    override fun truncate(width: Int): SysUnsigned {
        val widthByValue = widthByValue(value)
        if (width >= widthByValue) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        val truncated = value shr (widthByValue - width)
        return SysUnsigned(width, truncated)
    }

    private fun truncate(width: Int, value: Long, positiveMask: Long): SysUnsigned {
        if (width == MAX_WIDTH)
            return SysUnsigned(width, value, positiveMask)
        else
            return SysUnsigned(width, value and positiveMask, positiveMask)
    }

    override operator fun plus(arg: SysBaseInteger): SysUnsigned {
        var resWidth: Int
        var posMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
        }
        return truncate(resWidth, value + arg.value.toLong(), posMask)
    }

    override operator fun minus(arg: SysBaseInteger): SysUnsigned {
        var resWidth: Int
        var posMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
        }
        return truncate(resWidth, value - arg.value.toLong(), posMask)
    }

    override operator fun times(arg: SysBaseInteger): SysUnsigned {
        var resWidth: Int
        var posMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
        }
        return truncate(resWidth, value * arg.value.toLong(), posMask)
    }

    override operator fun div(arg: SysBaseInteger): SysUnsigned {
        var resWidth: Int
        var posMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
        }
        return truncate(resWidth, java.lang.Long.divideUnsigned(value, arg.value.toLong()), posMask)
    }

    override operator fun mod(arg: SysBaseInteger): SysUnsigned {
        var resWidth: Int
        var posMask: Long
        if (arg.width > width) {
            resWidth = Math.min(arg.width, MAX_WIDTH)
            posMask = arg.positiveMask.toLong()
        } else {
            resWidth = Math.min(width, MAX_WIDTH)
            posMask = positiveMask
        }
        return truncate(resWidth, java.lang.Long.remainderUnsigned(value, arg.value.toLong()), posMask)
    }

    override operator fun inc(): SysUnsigned = this + valueOf(width, 1)
    override operator fun dec(): SysUnsigned = this - valueOf(width, 1)

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

    override operator fun get(j: Int, i: Int): SysUnsigned {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        var result = value shr i
        return SysUnsigned(j - i + 1, result)
    }

    /** Bitwise and*/
    override infix fun and(arg: SysBaseInteger): SysUnsigned {
        var temp = arg.bitsState;

        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysUnsigned(Math.max(width, arg.width), value and arg.value.toLong(), bitsState = temp)
    }

    /** Bitwise or*/
    override infix fun or(arg: SysBaseInteger): SysUnsigned {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysUnsigned(Math.max(width, arg.width), value or arg.value.toLong(), bitsState = temp)

    }

    /** Bitwise xor*/
    override infix fun xor(arg: SysBaseInteger): SysUnsigned {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and  bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysUnsigned(Math.max(width, arg.width), value xor arg.value.toLong(), bitsState = temp)
    }

    /**Bitwise inversion (not)*/
    override fun inv(): SysUnsigned {
        return SysUnsigned(width, value.inv(), bitsState = bitsState);
    }


    /** Bitwise logical shift right*/
    override infix fun ushr(shift: Int): SysUnsigned {
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
        return SysUnsigned.valueOf(sysBitExpression);
    }

    /** Bitwise logical shift left*/
    override infix fun ushl(shift: Int): SysUnsigned {

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

        return SysUnsigned.valueOf(sysBitExpression)
    }

    override fun unaryMinus(): SysBaseInteger {
        return SysBaseInteger.valueOf(width + 1, -value)
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

    override fun shl(shift: Int) = this ushl shift

    override fun shr(shift: Int) = this ushr shift

    override fun toSysInteger() = SysInteger.valueOf(value)

    override fun toSysBigInteger() = SysBigInteger.valueOf(value)

    override fun compareTo(other: SysBaseInteger): Int {
        if (width != other.width) {
            throw IllegalArgumentException("Non comparable. Width not equal.")
        }
        return value.compareTo(other.value.toLong())
    }

    /** Cyclic shift right*/
    override infix fun cshr(shift: Int): SysUnsigned {

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
        return SysUnsigned.valueOf(sysBitExpression)
    }

    /** Cyclic shift left*/
    override infix fun cshl(shift: Int): SysUnsigned {

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
        return SysUnsigned.valueOf(sysBitExpression)
    }

    companion object : SysDataCompanion<SysUnsigned> {

        val MAX_WIDTH = 64

        fun valueOf(width: Int, value: Int): SysUnsigned {
            val lValue = Integer.toUnsignedLong(value)
            val realWidth = widthByValue(lValue)
            if (realWidth > width)
                throw IllegalArgumentException("Minimal possible width is $realWidth")
            return SysUnsigned(width, lValue, bitsState = maskByValue(width, realWidth))
        }

        fun valueOf(arr: Array<SysBit>): SysUnsigned {
            return SysUnsigned(arr.size, valueBySWSArray(arr), bitsState = maskBySWSArray(arr))
        }

        //maybe some bugs
        fun valueOf(width: Int, value: Long): SysUnsigned {
            val realWidth = widthByValue(value)
            if (realWidth > width)
                throw IllegalArgumentException("Minimal possible width is $realWidth")
            return SysUnsigned(width, value, bitsState = maskByValue(width, realWidth))
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

        private fun maskBySWSArray(arr: Array<SysBit>): Array<Boolean> {


            val mask = BooleanArray(arr.size)


            for (i in 0..mask.size - 1)
                if (arr[i] != SysBit.X)
                    mask[i] = true;

            return mask.toTypedArray();
        }

        private fun maskByValue(width: Int, minimalWidth: Int): Array<Boolean> {

            if (width == 0)
                return BooleanArray(0).toTypedArray();

            val mask = BooleanArray(width);

            if (width < minimalWidth) {
                throw IllegalArgumentException("width = $width, byValue = $minimalWidth")
            } else {
                mask.fill(true, mask.lastIndex + 1 - minimalWidth, mask.lastIndex + 1)
            }
            return mask.toTypedArray();
        }

        private fun widthByValue(value: Long): Int {
            return java.lang.Long.SIZE - java.lang.Long.numberOfLeadingZeros(value)
        }

        override val undefined: SysUnsigned
            get() = SysUnsigned.valueOf(arrayOf(SysBit.X))


        private fun maxValue(width: Int): Long {
            if (width == 0) return 0
            return (-1L ushr (MAX_WIDTH - width ))
        }
    }

}


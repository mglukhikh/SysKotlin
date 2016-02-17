package ru.spbstu.sysk.data

import java.util.*

class SysUnsigned
private constructor(
        val width: Int,
        val value: Long,
        defaultBitState: Boolean = false,
        private val bitsState: Array<Boolean> = Array(width, { i -> defaultBitState })
) : SysData {

    private constructor(width: Int, value: Long) : this(width, value, bitsState = maskByValue(width, widthByValue(value)))

    fun extend(width: Int): SysUnsigned {
        if (width < widthByValue(value))
            throw IllegalArgumentException()
        return SysUnsigned(width, value)
    }

    fun truncate(width: Int): SysUnsigned {
        val widthByValue = widthByValue(value)
        if (width >= widthByValue) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        val truncated = value shr (widthByValue - width)
        return SysUnsigned(width, truncated)
    }

    operator fun plus(arg: SysUnsigned) = SysUnsigned(Math.max(width, arg.width), arg.value + value)
    operator fun minus(arg: SysUnsigned) = SysUnsigned(Math.max(width, arg.width), value - arg.value)
    operator fun times(arg: SysUnsigned) = SysUnsigned(Math.max(width, arg.width), value * arg.value)
    operator fun div(arg: SysUnsigned) = SysUnsigned(Math.max(width, arg.width),
            java.lang.Long.divideUnsigned(value, arg.value))

    operator fun mod(arg: SysUnsigned) = SysUnsigned(Math.max(width, arg.width),
            java.lang.Long.remainderUnsigned(value, arg.value))

    operator fun get(i: Int): SysBit {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        if (!bitsState[i])
            return SysBit.X
        val shift = bitsState.indexOf(true)
        if ((value and (1L shl i - shift)) != 0L)
            return SysBit.ONE
        else
            return SysBit.ZERO
    }

    operator fun get(j: Int, i: Int): SysUnsigned {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        var result = value shr i
        return SysUnsigned(j - i + 1, result)
    }

    /** Bitwise and*/
    infix fun and(arg: SysUnsigned): SysUnsigned {
        var temp = arg.bitsState;

        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysUnsigned(Math.max(width, arg.width), value and arg.value, bitsState = temp)
    }

    /** Bitwise or*/
    infix fun or(arg: SysUnsigned): SysUnsigned {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysUnsigned(Math.max(width, arg.width), value or arg.value, bitsState = temp)

    }

    /** Bitwise xor*/
    infix fun xor(arg: SysUnsigned): SysUnsigned {

        var temp = arg.bitsState;
        for (i in 0..Math.min(temp.lastIndex, bitsState.lastIndex)) {
            temp[i] = temp[i] and  bitsState[i];
        }
        if (temp.size < bitsState.size)
            temp = temp.plus(bitsState.copyOfRange(temp.size, bitsState.size));
        return SysUnsigned(Math.max(width, arg.width), value xor arg.value, bitsState = temp)
    }

    /**Bitwise inversion (not)*/
    fun inv(): SysUnsigned {
        return SysUnsigned(width, value.inv(), bitsState = bitsState);
    }


    /** Bitwise logical shift right*/
    infix fun ushr(shift: Int): SysUnsigned {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysBitExpression = Array(width - shift) { i: Int -> this[i] }

        return SysUnsigned.valueOf(sysBitExpression);
    }

    /** Bitwise logical shift left*/
    infix fun ushl(shift: Int): SysUnsigned {
        if (shift == 0)
            return this;
        if (shift > width || shift < 0)
            throw IllegalArgumentException()

        val sysBitExpression = Array(width - shift) { i: Int -> this[i + shift] }

        return SysUnsigned.valueOf(sysBitExpression)
    }


    /** Cyclic shift right*/
    infix fun cshr(shift: Int): SysUnsigned {

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
    infix fun cshl(shift: Int): SysUnsigned {

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
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SysUnsigned

        if (width != other.width) return false
        if (value != other.value) return false
        if (!Arrays.equals(bitsState, other.bitsState)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result += 31 * result + value.hashCode()
        result += 31 * result + Arrays.hashCode(bitsState)
        return result
    }

    override fun toString(): String {
        return "SysUnsigned(width=$width, value=$value, bitsState=${Arrays.toString(bitsState)})"
    }

}


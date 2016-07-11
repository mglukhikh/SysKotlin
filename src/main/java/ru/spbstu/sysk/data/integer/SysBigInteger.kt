package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit
import java.math.BigInteger

class SysBigInteger private constructor(
        width: Int,
        override val value: GInt.BInt,
        hasUndefined: Boolean,
        override val positiveMask: GInt.BInt,
        override val negativeMask: GInt.BInt
) : SysInteger(width, value, hasUndefined, positiveMask, negativeMask) {

    override lateinit var bitsState: Array<SysBit>


    constructor(arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr), arr.contains(SysBit.X),
            positiveMask = getMaxValue(arr.size),
            negativeMask = getMaxValue(arr.size)) {
        bitsState = arr
    }

    private constructor(value: GInt.BInt, width: Int = value.value.bitLength() + 1) :
    this(width, value, false,
            positiveMask = getMaxValue(width),
            negativeMask = getMinValue(width))

    internal constructor(width: Int, value: GInt.BInt, positiveMask: GInt.BInt, negativeMask: GInt.BInt) :
    this(width, value, false, positiveMask = positiveMask,
            negativeMask = negativeMask)

    override fun extend(width: Int): SysInteger {
        if (width < this.width) throw IllegalArgumentException("extended width smaller then current width")
        return SysBigInteger(value, width)
    }

    override fun truncate(width: Int): SysInteger {
        if (width < 0 || width >= this.width) throw IllegalArgumentException()
        return truncate(width, value, getMaxValue(width), getMinValue(width))
    }

    override fun getPositiveValue(width: Int) = getMaxValue(width)
    override fun getNegativeValue(width: Int) = getMinValue(width)

    companion object {
        private val positiveValues = Array(129, { i -> maxValue(i) })
        private val negativeValues = Array(129, { i -> minValue(i) })

        internal fun getMaxValue(width: Int) = GInt.BInt(if (128 >= width) positiveValues[width] else maxValue(width))
        internal fun getMinValue(width: Int) = GInt.BInt(if (128 >= width) negativeValues[width] else minValue(width))

        private fun minValue(width: Int): BigInteger {
            if (width == 0) return BigInteger.ZERO
            return (((-BigInteger.ONE).shiftLeft(width - 1)))
        }

        private fun maxValue(width: Int): BigInteger {
            if (width == 0) return BigInteger.ZERO
            return (BigInteger.ONE.shiftLeft(width - 1)) - BigInteger.ONE
        }

        private fun valueBySWSArray(arr: Array<SysBit>): GInt.BInt {

            var leftShift = 0
            var rightShift = 0
            while (leftShift < arr.size && arr[leftShift] == SysBit.X)
                leftShift++

            while (arr.size - rightShift > leftShift && arr[arr.size - 1 - rightShift] == SysBit.X)
                rightShift++
            if (leftShift + rightShift == arr.size)
                return GInt.BInt(BigInteger.ZERO)
            var result = BigInteger.ZERO
            val tempArray = arr.copyOfRange(leftShift, arr.size - rightShift)
            if (!tempArray.last().one) {
                for (i in tempArray.indices) {
                    if (tempArray[i].one) {
                        result = result.setBit(i)
                    }
                }
                return GInt.BInt(result)
            } else {
                val inverseArray = inverseSWSArray(tempArray)
                for (i in inverseArray.indices) {
                    if (inverseArray[i].one) {
                        result = result.setBit(i)
                    }
                }
                result = result.negate()
                return GInt.BInt(result)
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
                } else result[i] = SysBit.ZERO
                i++
            }
            return result
        }


    }
}
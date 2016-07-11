package ru.spbstu.sysk.data.integer

import ru.spbstu.sysk.data.SysBit

class SysLongInteger private constructor(
        width: Int,
        override val value: GInt.LInt,
        hasUndefined: Boolean,
        override val positiveMask: GInt.LInt,
        override val negativeMask: GInt.LInt
) : SysInteger(width, value, hasUndefined, positiveMask, negativeMask) {

    override lateinit var bitsState: Array<SysBit>


    constructor(arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr), arr.contains(SysBit.X),
            positiveMask = getMaxValue(arr.size),
            negativeMask = getMinValue(arr.size)) {
        bitsState = arr
    }

    private constructor(value: GInt.LInt, width: Int = widthByValue(value.value)) :
    this(width, value, false,
            positiveMask = getMaxValue(width),
            negativeMask = getMinValue(width))

    internal constructor(width: Int, value: GInt.LInt, positiveMask: GInt.LInt, negativeMask: GInt.LInt) :
    this(width, value, false, positiveMask = positiveMask,
            negativeMask = negativeMask)

    override fun extend(width: Int): SysInteger {
        if (width < this.width) throw IllegalArgumentException("extended width smaller then current width")
        if (width > MAX_WIDTH)
            return construct(width, value)
        return SysLongInteger(value, width)
    }

    override fun truncate(width: Int): SysInteger {
        if (width < 0 || width >= this.width) throw IllegalArgumentException()
        return truncate(width, value, getPositiveValue(width), getNegativeValue(width))
    }

    override fun getPositiveValue(width: Int) = getMaxValue(width)
    override fun getNegativeValue(width: Int) = getMinValue(width)

    companion object {

        const val MAX_WIDTH: Int = 64

        internal fun getMaxValue(width: Int) = GInt.LInt(positiveValues[width])
        internal fun getMinValue(width: Int) = GInt.LInt(negativeValues[width])

        private val positiveValues = LongArray(MAX_WIDTH + 1, { i -> maxValue(i) })
        private val negativeValues = LongArray(MAX_WIDTH + 1, { i -> minValue(i) })

        private fun minValue(width: Int): Long {
            if (width == 0) return 0
            if (width == MAX_WIDTH) return Long.MIN_VALUE
            return ((-1L shl (width - 1)))
        }

        private fun maxValue(width: Int): Long {
            if (width == 0) return 0
            if (width == MAX_WIDTH) return Long.MAX_VALUE
            return (1L shl (width - 1)) - 1
        }

        private fun widthByValue(value: Long): Int {
            var current = value
            if (current == 0L)
                return 1
            if (current < 0)
                current = current.inv()
            return java.lang.Long.SIZE - java.lang.Long.numberOfLeadingZeros(current) + 1
        }

        private fun valueBySWSArray(arr: Array<SysBit>): GInt.LInt {
            var value: Long = 0L
            var counter: Int = 0
            while (counter < arr.size && arr[counter] == SysBit.X)
                counter++
            var shift = 0
            while (counter < arr.size && arr[counter] != SysBit.X) {
                if (arr[counter].one)
                    value = value or (1L shl shift)
                shift++
                counter++
            }
            if (arr[counter - 1].one)
                for (i in 0..64 - shift) {
                    value = value or (1L shl (63 - i))
                }
            return GInt.LInt(value)
        }
    }

}
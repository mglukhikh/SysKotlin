package ru.spbstu.sysk.data.integer
import ru.spbstu.sysk.data.SysBit

class SysUnsigned private constructor(
        width: Int,
        override val value: GInt.UInt,
        hasUndefined: Boolean,
        override val positiveMask: GInt.UInt,
        override val negativeMask: GInt.UInt
) : SysInteger(width, value, hasUndefined, positiveMask, negativeMask) {

    override lateinit var bitsState: Array<SysBit>


    constructor(arr: Array<SysBit>) : this(arr.size, valueBySWSArray(arr), arr.contains(SysBit.X),
            positiveMask = getMaxValue(arr.size),
            negativeMask = getMinValue(arr.size)) {
        bitsState = arr
    }

    private constructor(value: GInt.UInt, width: Int = widthByValue(value.value)) :
    this(width, value, false,
            positiveMask = getMaxValue(width),
            negativeMask = getMinValue(width))

    internal constructor(width: Int, value: GInt.UInt, positiveMask: GInt.UInt, negativeMask: GInt.UInt = GInt.UInt(0L)) :
    this(width, value, false, positiveMask = positiveMask,
            negativeMask = negativeMask)

    override fun extend(width: Int): SysInteger {
        if (width < this.width) throw IllegalArgumentException("extended width smaller then current width")
        if (width > MAX_WIDTH)
            return construct(width, value)
        return SysUnsigned(value, width)
    }

    override fun truncate(width: Int): SysInteger {
        if (width < 0 || width >= this.width) throw IllegalArgumentException()
        return truncate(width, value, getPositiveValue(width), getNegativeValue(width))
    }


    operator fun plus(arg: SysUnsigned) = calc(arg, GInt.UInt::plus)
    operator fun minus(arg: SysUnsigned) = calc(arg, GInt.UInt::minus)
    operator fun times(arg: SysUnsigned) = calc(arg, GInt.UInt::times)
    operator fun div(arg: SysUnsigned) = calc(arg, GInt.UInt::div)
    operator fun mod(arg: SysUnsigned) = calc(arg, GInt.UInt::mod)

    override operator fun plus(arg: Int) = calc(value + arg)
    override operator fun minus(arg: Int) = calc(value - arg)
    override operator fun times(arg: Int) = calc(value * arg)
    override operator fun div(arg: Int) = calc(value / arg)
    override operator fun mod(arg: Int) = calc(value % arg)

    override operator fun plus(arg: Long) = calc(value + arg)
    override operator fun minus(arg: Long) = calc(value - arg)
    override operator fun times(arg: Long) = calc(value * arg)
    override operator fun div(arg: Long) = calc(value / arg)
    override operator fun mod(arg: Long) = calc(value % arg)


    private fun trunc(width: Int, value: GInt.UInt, positiveMask: GInt.UInt)
            = SysUnsigned(width, value and positiveMask, positiveMask)

    private fun undefined(width: Int) = SysUnsigned(Array(width, { i -> SysBit.X }))

    private fun calc(arg: SysUnsigned, func: (GInt.UInt, GInt.UInt) -> GInt.UInt): SysUnsigned {
        if (hasUndefined || arg.hasUndefined)
            return undefined(Math.max(width, arg.width))
        return if (width >= arg.width) trunc(width, func(value, arg.value), positiveMask)
        else trunc(arg.width, func(value, arg.value), arg.positiveMask)
    }

    private fun calc(result: GInt.UInt) = trunc(width, result, positiveMask)


    override fun getPositiveValue(width: Int) = getMaxValue(width)
    override fun getNegativeValue(width: Int) = getMinValue(width)


    companion object {

        const val MAX_WIDTH: Int = 64

        fun getMaxValue(width: Int) = GInt.UInt(positiveValues[width])

        private val positiveValues = Array(MAX_WIDTH + 1, { i -> maxValue(i) })

        private fun widthByValue(value: Long): Int {
            return java.lang.Long.SIZE - java.lang.Long.numberOfLeadingZeros(value)
        }

        private fun valueBySWSArray(arr: Array<SysBit>): GInt.UInt {
            val value = CharArray(64, { i -> '0' })
            var counter: Int = 0
            while (counter < arr.size && arr[counter] == SysBit.X)
                counter++
            var shift = 0
            while (counter < arr.size && arr[counter] != SysBit.X) {
                if (arr[counter].one)
                    value[value.lastIndex - shift] = '1'
                shift++
                counter++
            }
            return GInt.UInt(java.lang.Long.parseUnsignedLong(String(value), 2))
        }

        protected fun getMinValue(width: Int) = GInt.UInt(0L)

        private fun maxValue(width: Int): Long {
            if (width == 0) return 0
            return (-1L ushr (MAX_WIDTH - width))
        }
    }
}
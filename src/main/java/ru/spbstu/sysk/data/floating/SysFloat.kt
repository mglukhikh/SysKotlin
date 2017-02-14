package ru.spbstu.sysk.data.floating

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysDataCompanion
import ru.spbstu.sysk.data.integer.SysUnsigned

class SysFloat private constructor(
        sign: SysBit,
        exponent: SysUnsigned,
        mantissa: SysUnsigned,
        override val value: Float
) : SysBaseFP(sign, exponent, mantissa, value) {

    constructor(sign: SysBit, exponent: SysUnsigned, mantissa: SysUnsigned) :
            this(sign, exponent, mantissa, valueByBits(sign, exponent, mantissa))

    override operator fun plus(arg: SysBaseFP) = valueOf(value + arg.value.toFloat())
    override operator fun minus(arg: SysBaseFP) = valueOf(value - arg.value.toFloat())
    override operator fun times(arg: SysBaseFP) = valueOf(value * arg.value.toFloat())
    override operator fun div(arg: SysBaseFP) = valueOf(value / arg.value.toFloat())
    override operator fun rem(arg: SysBaseFP) = valueOf(value % arg.value.toFloat())
    override fun power(arg: SysBaseFP) = valueOf(Math.pow(value.toDouble(), arg.value.toDouble()).toFloat())

    companion object : SysDataCompanion<SysFloat> {
        override val undefined: SysFloat
            get() = SysFloat(SysBit.X, SysUnsigned.undefined, SysUnsigned.undefined, 0f)

        fun valueOf(arg: Float): SysFloat {

            val bits: Int = java.lang.Float.floatToIntBits(arg)
            val signBit = (bits and (1 shl 31)) != 0
            val exponentBits = bits and 0x7ff00000
            val mantis = (bits and 0x007fffff)
            val exponent = (exponentBits shr 23)
            val sign: SysBit
            if (signBit)
                sign = SysBit.ONE
            else
                sign = SysBit.ZERO
            return SysFloat(sign, SysUnsigned.valueOf(8, exponent), SysUnsigned.valueOf(23, mantis), arg)
        }

        private fun valueByBits(sign: SysBit, exponent: SysUnsigned, mantissa: SysUnsigned): Float {
            val mantis = mantissa.value.toInt()

            var result = mantis or ((exponent.value.toInt())shl 23)
            if (sign == SysBit.ONE)
                result = result or (1 shl 31)

            return java.lang.Float.intBitsToFloat(result)
        }
    }
}

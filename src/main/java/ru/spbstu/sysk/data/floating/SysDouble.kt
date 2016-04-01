package ru.spbstu.sysk.data.floating

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysDataCompanion
import ru.spbstu.sysk.data.integer.SysUnsigned

class SysDouble private constructor(
        sign: SysBit,
        exponent: SysUnsigned,
        mantissa: SysUnsigned,
        override val value: Double
) : SysBaseFP(sign, exponent, mantissa, value) {

    constructor(sign: SysBit, exponent: SysUnsigned, mantissa: SysUnsigned) :
            this(sign, exponent, mantissa, valueByBits(sign, exponent, mantissa))

    override operator fun plus(arg: SysBaseFP) = valueOf(value + arg.value.toDouble())
    override operator fun minus(arg: SysBaseFP) = valueOf(value - arg.value.toDouble())
    override operator fun times(arg: SysBaseFP) = valueOf(value * arg.value.toDouble())
    override operator fun div(arg: SysBaseFP) = valueOf(value / arg.value.toDouble())
    override operator fun mod(arg: SysBaseFP) = valueOf(value % arg.value.toDouble())
    override fun power(arg: SysBaseFP) = valueOf(Math.pow(value, arg.value.toDouble()))

    companion object : SysDataCompanion<SysDouble> {
        override val undefined: SysDouble
            get() = SysDouble(SysBit.X, SysUnsigned.undefined, SysUnsigned.undefined, 0.0)

        fun valueOf(arg: Double): SysDouble {

            val bits: Long = java.lang.Double.doubleToLongBits(arg);

            val signBit = (bits and (1L shl 63)) != 0L
            val exponentBits = bits and 0x7ff0000000000000L;
            var mantis = (bits and 0x000fffffffffffffL)//or 0x0010000000000000L
            var exponent = (exponentBits shr 52).toInt()
            val sign: SysBit
            if (signBit)
                sign = SysBit.ONE
            else
                sign = SysBit.ZERO
            return SysDouble(sign, SysUnsigned.valueOf(11, exponent), SysUnsigned.valueOf(52, mantis))
        }

        private fun valueByBits(sign: SysBit, exponent: SysUnsigned, mantissa: SysUnsigned): Double {
            var mantis = mantissa.value

            var result = mantis or ((exponent.value)shl 52)
            if (sign == SysBit.ONE)
                result = result or (1L shl 63)

            return java.lang.Double.longBitsToDouble(result)
        }
    }
}
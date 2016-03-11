package ru.spbstu.sysk.data

class SysFloat private constructor(
        val sign: SysBit,
        val exponent: SysUnsigned,
        val mantiss: SysUnsigned,
        val value: Float
) : SysData {
    val width = 1 + exponent.width + mantiss.width


    constructor(sign: SysBit, exponent: SysUnsigned, mantiss: SysUnsigned) : this(sign, exponent, mantiss,
            valueByBits(sign, exponent, mantiss))

    fun toFloat(): Float {
        return value
    }

    operator fun plus(arg: SysFloat) = valueOf(value + arg.value)
    operator fun minus(arg: SysFloat) = valueOf(value - arg.value)
    operator fun times(arg: SysFloat) = valueOf(value * arg.value)
    operator fun div(arg: SysFloat) = valueOf(value / arg.value)
    operator fun mod(arg: SysFloat) = valueOf(value % arg.value)
    fun power(arg: SysFloat) = valueOf(Math.pow(value.toDouble(), arg.value.toDouble()).toFloat())


    companion object : SysDataCompanion<SysFloat> {
        override val undefined: SysFloat
            get() = SysFloat(SysBit.X, SysUnsigned.undefined, SysUnsigned.undefined, 0f)


        fun valueOf(arg: Float): SysFloat {

            val bits: Int = java.lang.Float.floatToIntBits(arg)
            val signBit = (bits and (1 shl 31)) != 0
            val exponentBits = bits and 0x7ff00000;
            var mantis = (bits and 0x007fffff)
            var exponent = (exponentBits shr 23)
            val sign: SysBit
            if (signBit)
                sign = SysBit.ONE
            else
                sign = SysBit.ZERO
            return SysFloat(sign, SysUnsigned.valueOf(8, exponent), SysUnsigned.valueOf(23, mantis), arg)
        }


        private fun valueByBits(sign: SysBit, exponent: SysUnsigned, mantiss: SysUnsigned): Float {
            var mantis = mantiss.value.toInt()

            var result = mantis.toInt() or ((exponent.value.toInt())shl 23)
            if (sign == SysBit.ONE)
                result = result or (1 shl 31)

            return java.lang.Float.intBitsToFloat(result)
        }


    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SysFloat

        if (sign != other.sign) return false
        if (exponent != other.exponent) return false
        if (mantiss != other.mantiss) return false
        if (value != other.value) return false
        if (width != other.width) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sign.hashCode()
        result += 31 * result + exponent.hashCode()
        result += 31 * result + mantiss.hashCode()
        result += 31 * result + value.hashCode()
        result += 31 * result + width
        return result
    }

    override fun toString(): String {
        return "$value [$width]"
    }
}

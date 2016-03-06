package ru.spbstu.sysk.data

class SysDouble private constructor(
        val sign: SysBit,
        val exponent: SysUnsigned,
        val mantiss: SysUnsigned,
        val value: Double
) : SysData {
    val width = 1 + exponent.width + mantiss.width

    constructor(sign: SysBit, exponent: SysUnsigned, mantiss: SysUnsigned) : this(sign, exponent, mantiss,
            valueByBits(sign, exponent, mantiss))

    fun toDouble() = value

    operator fun plus(arg: SysDouble) = valueOf(value + arg.value)
    operator fun minus(arg: SysDouble) = valueOf(value - arg.value)
    operator fun times(arg: SysDouble) = valueOf(value * arg.value)
    operator fun div(arg: SysDouble) = valueOf(value / arg.value)
    operator fun mod(arg: SysDouble) = valueOf(value % arg.value)
    fun power(arg: SysDouble) = valueOf(Math.pow(value, arg.value))

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

        private fun valueByBits(sign: SysBit, exponent: SysUnsigned, mantiss: SysUnsigned): Double {
            var mantis = mantiss.value

            var result = mantis or ((exponent.value)shl 52)
            if (sign == SysBit.ONE)
                result = result or (1L shl 63)

            return java.lang.Double.longBitsToDouble(result)
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SysDouble

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
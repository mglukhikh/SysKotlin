package ru.spbstu.sysk.data

class SysFloat(
        val sign: SysBit,
        val exponent: SysUnsigned,
        val mantiss: SysUnsigned
) : SysData {
    val width = 1 + exponent.width + mantiss.width

    fun toDouble(): Double {

        var mantis = mantiss.value

        var result = mantis or ((exponent.value)shl 52)
        if (sign == SysBit.ONE)
            result = result or (1L shl 63)

        return java.lang.Double.longBitsToDouble(result)
    }


    companion object {
        fun valueOf(arg: Double): SysFloat {

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
            return SysFloat(sign, SysUnsigned.valueOf(11, exponent), SysUnsigned.valueOf(52, mantis))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SysFloat

        if (sign != other.sign) return false
        if (exponent != other.exponent) return false
        if (mantiss != other.mantiss) return false
        if (width != other.width) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sign.hashCode()
        result += 31 * result + exponent.hashCode()
        result += 31 * result + mantiss.hashCode()
        result += 31 * result + width
        return result
    }

    override fun toString(): String {
        return "SysFloat(sign=$sign, exponent=$exponent, mantiss=$mantiss, width=$width)"
    }

}

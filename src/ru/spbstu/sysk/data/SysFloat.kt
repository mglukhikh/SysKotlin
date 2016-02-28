package ru.spbstu.sysk.data

class SysFloat(
        val sign: SysBit,
        val exponent: SysUnsigned,
        val mantiss: SysUnsigned
) : SysData {
    val width = 1 + exponent.width + mantiss.width

    init {
        println("$sign " + java.lang.Long.toBinaryString(exponent.value) + " " + java.lang.Long.toBinaryString(mantiss.value))
    }


    //right exp is smaller or equal
    private fun add(left: SysFloat, right: SysFloat): SysFloat {

        //println(left.exponent.value)
        //println(right.exponent.value)

        var rightExp = right.exponent
        var leftExp = left.exponent

        val exp: Int = (leftExp - rightExp).value.toInt()
        println(exp)
        var rightMantiss = right.mantiss
        var leftMantiss = left.mantiss

        leftMantiss = SysUnsigned.valueOf(leftMantiss.width + 1, leftMantiss.value or (1L shl leftMantiss.width))
        rightMantiss = SysUnsigned.valueOf(rightMantiss.width + 1, rightMantiss.value or (1L shl rightMantiss.width))
        println(java.lang.Long.toBinaryString(leftMantiss.value))
        println(java.lang.Long.toBinaryString(rightMantiss.value))


        if (exp > 0) {
            if (exp >= right.mantiss.width)
                return left
            rightMantiss = rightMantiss ushr exp
        } else if (exp < 0) {
            throw IllegalArgumentException()
        }
        println(java.lang.Long.toBinaryString(rightMantiss.value))

        var result = leftMantiss + rightMantiss
        println(java.lang.Long.toBinaryString(result.value))
        println(result.width)
        var resExp = leftExp
        while (result[result.width - 1] != SysBit.ONE) {
            result = result ushl 1
            resExp--
        }
        result = result ushl 1
        result = result.truncate(result.width - 1)
        if (result[0] != SysBit.ZERO)
            result++
        println(java.lang.Long.toBinaryString(result.value))
        return SysFloat(left.sign, resExp, result)
    }

    operator fun plus(arg: SysFloat): SysFloat {
        if (arg.width != width)
            throw IllegalArgumentException("Widths not equals")


        if (this.exponent.value < arg.exponent.value)
            return add(arg, this)
        else
            return add(this, arg)

    }

    fun toDouble(): Double {

        var mantis = mantiss.value

        var result = mantis or ((exponent.value)shl 52)
        if (sign == SysBit.ONE)
            result = result or (1L shl 63)

        return java.lang.Double.longBitsToDouble(result)
    }

    fun toFloat(): Float {
        var mantis = mantiss.value.toInt()

        var result = mantis.toInt() or ((exponent.value.toInt())shl 23)
        if (sign == SysBit.ONE)
            result = result or (1 shl 31)

        return java.lang.Float.intBitsToFloat(result)
    }

    companion object : SysDataCompanion<SysFloat> {
        override val undefined: SysFloat
            get() = SysFloat(SysBit.X, SysUnsigned.undefined, SysUnsigned.undefined)

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

        fun valueOf(arg: Float): SysFloat {

            val bits: Int = java.lang.Float.floatToIntBits(arg)
            val signBit = (bits and (1 shl 31)) != 0
            val exponentBits = bits and 0x7ff00000;
            var mantis = (bits and 0x007fffff)
            var exponent = (exponentBits shr 23)
            /*println(java.lang.Integer.toBinaryString(bits) + "\n" +
                    "" + java.lang.Integer.toBinaryString(exponent) + " " +
                    "" + java.lang.Integer.toBinaryString(mantis))*/
            val sign: SysBit
            if (signBit)
                sign = SysBit.ONE
            else
                sign = SysBit.ZERO
            return SysFloat(sign, SysUnsigned.valueOf(8, exponent), SysUnsigned.valueOf(23, mantis))
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

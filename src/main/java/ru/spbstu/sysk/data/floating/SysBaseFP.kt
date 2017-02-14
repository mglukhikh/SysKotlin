package ru.spbstu.sysk.data.floating

import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.integer.SysUnsigned

abstract class SysBaseFP protected constructor(
        val sign: SysBit,
        val exponent: SysUnsigned,
        val mantissa: SysUnsigned,
        open val value: Number
) : SysData {
    val width = 1 + exponent.width + mantissa.width

    fun toFloat(): Float {
        return value.toFloat()
    }

    fun toDouble(): Double {
        return value.toDouble()
    }

    abstract operator fun plus(arg: SysBaseFP): SysBaseFP
    abstract operator fun minus(arg: SysBaseFP): SysBaseFP
    abstract operator fun times(arg: SysBaseFP): SysBaseFP
    abstract operator fun div(arg: SysBaseFP): SysBaseFP
    abstract operator fun rem(arg: SysBaseFP): SysBaseFP

    abstract fun power(arg: SysBaseFP): SysBaseFP

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is SysBaseFP) {

            if (sign != other.sign) return false
            if (exponent != other.exponent) return false
            if (mantissa != other.mantissa) return false
            if (value != other.value) return false
            if (width != other.width) return false

            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = sign.hashCode()
        result += 31 * result + exponent.hashCode()
        result += 31 * result + mantissa.hashCode()
        result += 31 * result + value.hashCode()
        result += 31 * result + width
        return result
    }

    override fun toString(): String {
        return "$value [$width]"
    }
}

fun float(value: Float) = SysFloat.valueOf(value)

fun float(sign: SysBit, exponent: SysUnsigned, mantissa: SysUnsigned) = SysFloat(sign, exponent, mantissa)

fun double(value: Double) = SysDouble.valueOf(value)

fun double(sign: SysBit, exponent: SysUnsigned, mantissa: SysUnsigned) = SysDouble(sign, exponent, mantissa)

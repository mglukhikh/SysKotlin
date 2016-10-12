package ru.spbstu.sysk.data

enum class SysBit(
        val one: Boolean = false,
        val zero: Boolean = false,
        val x: Boolean = false,
        val z: Boolean = false
) : SysData {

    ONE(one = true),
    ZERO(zero = true),
    X(x = true),
    Z(z = true);

    companion object : SysDataCompanion<SysBit> {

        operator fun invoke(value: Boolean) = if (value) ONE else ZERO

        override val undefined: SysBit
            get() = SysBit.X
    }

    operator fun not() = when (this) {
        X -> X
        ONE -> ZERO
        ZERO -> ONE
        Z -> Z
    }

    infix fun and(other: SysBit) = when (this) {
        ZERO -> ZERO
        ONE -> other
        X -> if (other == ZERO) ZERO else X
        Z -> if (other == ONE) Z else other
    }

    infix fun or(other: SysBit) = !((!other).and(!this))

    infix fun xor(other: SysBit) = when(this) {
        X -> X
        Z -> other
        ONE -> if (other.x) X else if (!other.one) ONE else ZERO
        ZERO -> if (other.z) ZERO else other
    }

    /** Direct connection of two wires */
    fun wiredAnd(other: SysBit) = when (this) {
        Z -> other
        X -> X
        ZERO -> if (other.zero || other.z) ZERO else X
        ONE -> if (other.one || other.z) ONE else X
    }
}
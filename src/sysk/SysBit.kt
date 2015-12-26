package sysk

enum class SysBit(
        open val one: Boolean = false,
        open val zero: Boolean = false,
        open val x: Boolean = false,
        open val z: Boolean = false
) : SysData {

    ONE(one = true),
    ZERO(zero = true),
    X(x = true),
    Z(z = true);

    companion object : SysDataCompanion<SysBit> {

        override val undefined: SysBit
            get() = SysBit.X
    }

    operator fun not() = when (this) {
        X -> X
        ONE -> ZERO
        ZERO -> ONE
        Z -> Z
    }

    fun and(other: SysBit) = when (this) {
        ZERO -> ZERO
        ONE -> other
        X -> if (other == ZERO) ZERO else X
        Z -> if (other == ONE) Z else other
    }

    fun or(other: SysBit) = !((!other).and(!this))

    /** Direct connection of two wires */
    fun wiredAnd(other: SysBit) = when (this) {
        Z -> other
        X -> X
        ZERO -> if (other.zero || other.z) ZERO else X
        ONE -> if (other.one || other.z) ONE else X
    }
}
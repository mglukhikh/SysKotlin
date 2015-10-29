package sysk

enum class SysWireState {
    ONE {
        override val one = true
    },
    ZERO {
        override val zero = true
    },
    X {
        override val x = true
    },
    Z {
        override val z = true
    };

    open val one = false
    open val zero = false
    open val x = false
    open val z = false

    operator fun not() = when (this) {
        X    -> X
        ONE  -> ZERO
        ZERO -> ONE
        Z    -> Z
    }

    fun and(other: SysWireState) = when (this) {
        ZERO -> ZERO
        ONE  -> other
        X    -> if (other == ZERO) ZERO else X
        Z    -> if (other == ONE) Z else other
    }

    fun or(other: SysWireState) = !((!other).and(!this))

    /** Direct connection of two wires */
    fun wiredAnd(other: SysWireState) = when (this) {
        Z    -> other
        X    -> X
        ZERO -> if (other.zero || other.z) ZERO else X
        ONE  -> if (other.one || other.z) ONE else X
    }
}
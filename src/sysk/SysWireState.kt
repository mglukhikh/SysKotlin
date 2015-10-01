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
    };

    open val one = false
    open val zero = false
    open val x = false

    operator fun not() = when (this) {
        X    -> X
        ONE  -> ZERO
        ZERO -> ONE
    }

    fun and(other: SysWireState) = if (this == ZERO || other == ZERO) ZERO else if (this == ONE && other == ONE) ONE else X

    fun or(other: SysWireState) = !((!other).and(!this))

}
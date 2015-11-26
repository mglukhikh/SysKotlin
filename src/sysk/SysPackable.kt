package sysk

abstract class SysPackable {

    public val undefined: Boolean

    constructor(undefined: Boolean) {
        this.undefined = undefined
    }

    constructor(undefined: SysPackable.Undefined) {
        this.undefined = undefined.undefined
    }

    override public abstract fun toString(): String

    companion object Undefined : SysPackable(true) {
        override public fun toString() = "Undefined"
    }
}
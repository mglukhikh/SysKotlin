package sysk

interface SysPackable {

    val undefined: Boolean
        get() = false

    companion object Undefined : SysPackable {
        override val undefined: Boolean
            get() = true

        override public fun toString() = "Undefined"
    }
}
package sysk

open class SysBung<T> constructor(private val defaultValue: T) {

    public var value: T = defaultValue
        get() = defaultValue

}

open class SysFifoBung<T> constructor(private val defaultValue: T) : SysBung<T>(defaultValue) {

    public val size: Int = 0

    public val full: Boolean = false

    public val empty: Boolean = true

}

open class SysBusBung<T> constructor(private val defaultValue: T) : SysBung<T>(defaultValue) {

    public operator fun get(index: Int): T = defaultValue

}
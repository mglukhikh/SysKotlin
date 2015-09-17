package sysk

data class SysName(val name: String)

open class SysObject(val sysName: SysName, val parent: SysObject? = null) {

    private val parentName: String
        get() = if (parent != null) parent.name + "." else ""

    val name: String
        get() = parentName + sysName.name

    constructor(name: String, parent: SysObject? = null): this(SysName(name), parent)
}
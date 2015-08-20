package sysk

public data class SysName(public val name: String)

public open class SysObject(public val sysName: SysName, public val parent: SysObject? = null) {

    private val parentName: String
        get() = if (parent != null) parent.name + "." else ""

    public val name: String
        get() = parentName + sysName.name

    public constructor(name: String, parent: SysObject? = null): this(SysName(name), parent)
}
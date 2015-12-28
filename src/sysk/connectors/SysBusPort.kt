package sysk.connectors

import sysk.core.SysObject
import sysk.data.SysData
import sysk.data.SysPort

open class SysBusPort<T : SysData> constructor(
        name: String, parent: SysObject? = null, bus: SysBus<T>? = null
) : SysPort<SysBus<T>>(name, parent, bus) {

    operator fun get(index: Int): T {
        if (bound == null) throw IllegalStateException("Port $name is not bound")
        return bound!![index]
    }

    fun set(value: T, index: Int) {
        if (bound == null) throw IllegalStateException("Port $name is not bound")
        bound!!.set(value, index, this)
    }
}
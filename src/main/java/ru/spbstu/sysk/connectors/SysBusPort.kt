package ru.spbstu.sysk.connectors

import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysPort

open class SysBusPort<T : SysData>  internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, bus: SysBus<T>? = null, private val defaultValue: T? = null
) : SysPort<SysBus<T>>(name, scheduler, parent, bus) {

    operator fun get(index: Int): T {
        if (isBound) {
            return bound!![index]
        } else {
            if (defaultValue == null) throw IllegalStateException("Port $name is not bound")
            return defaultValue
        }
    }

    fun set(value: T, index: Int) {
        if (!sealed) {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            bound!!.set(value, index, this)
        }
    }

    operator fun invoke(value: T, index: Int) = set(value, index)
}
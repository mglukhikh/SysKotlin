package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysInteger
import ru.spbstu.sysk.data.SysPort

open class SysBusPort<T : SysData> internal constructor(
        val capacity: Int, name: String, scheduler: SysScheduler,
        parent: SysObject? = null, private val defaultValue: T? = null
) : SysPort<SysBus<T>>(name, scheduler, parent, null) {

    override infix fun bind(sysInterface: SysBus<T>) {
        //BUG: capacity = 0 for any parameters in constructor
        if (capacity != sysInterface.capacity) {
            throw IllegalArgumentException("Port $name capacity is not the same as the capacity of the bus ${sysInterface.name}")
        }
        super.bind(sysInterface)
    }

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

class SysBitBusPort internal constructor(
        capacity: Int, name: String, scheduler: SysScheduler,
        parent: SysObject? = null, defaultValue: SysBit? = null
) : SysBusPort<SysBit>(capacity, name, scheduler, parent, defaultValue) {

    operator fun invoke(value: SysInteger) {
        for (i in 0..capacity - 1) {
            if (i >= value.width) set(X, i)
            else set(value[i], i)
        }
    }

    operator fun invoke() = SysInteger(Array(capacity, { get(it) }))

    fun disable() {
        for (i in 0..capacity-1) set(Z, i)
    }
}
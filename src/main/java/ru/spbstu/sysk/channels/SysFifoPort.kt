package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysPort

open class SysFifoInput<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, Fifo: SysFifo<T>? = null
) : SysPort<SysFifo<T>>(name, scheduler, parent, Fifo) {

    val value: T
        get() {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            return bound!!.output
        }

    operator fun invoke() = value

    val size: Int
        get() {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            return bound!!.size
        }

    val full: Boolean
        get() {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            return bound!!.full
        }

    val empty: Boolean
        get() {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            return bound!!.empty
        }

    var pop: SysBit
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for pop port")
        set(value) {
            if (!sealed) {
                if (!isBound) throw IllegalStateException("Port $name is not bound")
                bound!!.pop = value
            }
        }
}

open class SysFifoOutput<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, Fifo: SysFifo<T>? = null
) : SysPort<SysFifo<T>>(name, scheduler, parent, Fifo) {

    var value: T
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for output port")
        set(value) {
            if (!sealed) {
                if (!isBound) throw IllegalStateException("Port $name is not bound")
                bound!!.input = value
            }
        }

    operator fun invoke(value: T) {
        this.value = value
    }

    val size: Int
        get() {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            return bound!!.size
        }

    val full: Boolean
        get() {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            return bound!!.full
        }

    val empty: Boolean
        get() {
            if (!isBound) throw IllegalStateException("Port $name is not bound")
            return bound!!.empty
        }

    var push: SysBit
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for push port")
        set(value) {
            if (!sealed) {
                if (!isBound) throw IllegalStateException("Port $name is not bound")
                bound!!.push = value
            }
        }
}

package ru.spbstu.sysk.connectors

import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysPort

open class SysFifoInput<T : SysData>(
        name: String, parent: SysObject? = null, Fifo: SysFifo<T>? = null
) : SysPort<SysFifo<T>>(name, parent, Fifo) {

    val value: T
        get() {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            return bound!!.output
        }

    val size: Int
        get() {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            return bound!!.size
        }

    val full: Boolean
        get() {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            return bound!!.full
        }

    val empty: Boolean
        get() {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            return bound!!.empty
        }

    var pop: SysBit
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for pop port")
        set(value) {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            bound!!.pop = value
        }
}

open class SysFifoOutput<T : SysData> constructor(
        name: String, parent: SysObject? = null, Fifo: SysFifo<T>? = null
) : SysPort<SysFifo<T>>(name, parent, Fifo) {

    var value: T
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for output port")
        set(value) {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            bound!!.input = value
        }

    val size: Int
        get() {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            return bound!!.size
        }

    val full: Boolean
        get() {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            return bound!!.full
        }

    val empty: Boolean
        get() {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            return bound!!.empty
        }

    var push: SysBit
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for push port")
        set(value) {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            bound!!.push = value
        }
}

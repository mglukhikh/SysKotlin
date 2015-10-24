package sysk

open class SysPort<IF : SysInterface> internal constructor(
        name: String, parent: SysObject? = null, sysInterface: IF? = null
) : SysObject(name, parent) {

    protected var bound: IF? = null
        private set

    init {
        sysInterface?.let { bind(it) }
    }

    fun bind(port: SysPort<IF>): Unit = bind(port())

    infix fun bind(sysInterface: IF) {
        assert(bound == null) { "Port $name is already bound to $bound" }
        bound = sysInterface
        sysInterface.register(this)
    }

    fun to(port: SysPort<IF>): Pair<SysPort<IF>, IF> = Pair(this, port())

    fun to(sysInterface: IF): Pair<SysPort<IF>, IF> = Pair(this, sysInterface)

    operator fun invoke(): IF {
        assert(bound != null) { "Port $name is not bound" }
        return bound!!
    }

    val defaultEvent: SysWait.Finder = object : SysWait.Finder {
        override operator fun invoke() = bound?.defaultEvent
    }
}

fun <IF : SysInterface> bind(vararg pairs: Pair<SysPort<IF>, IF>) {
    for (pair in pairs) {
        pair.first.bind(pair.second)
    }
}

open class SysInput<T> internal constructor(
        name: String, parent: SysObject? = null, signalRead: SysSignalRead<T>? = null
) : SysPort<SysSignalRead<T>>(name, parent, signalRead) {
    val value: T
        get() = bound?.value ?: throw IllegalStateException("Port $name is not bound")
}

class SysWireInput internal constructor(
        name: String, parent: SysObject? = null, signalRead: SysWireRead? = null
) : SysInput<SysWireState>(name, parent, signalRead) {
    val posEdgeEvent: SysWait.Finder = object : SysWait.Finder {
        override fun invoke() = (bound as? SysWireRead)?.posEdgeEvent
    }

    val negEdgeEvent: SysWait.Finder = object : SysWait.Finder {
        override fun invoke() = (bound as? SysWireRead)?.negEdgeEvent
    }

    val zero: Boolean
        get() = value.zero

    val one: Boolean
        get() = value.one

    val x: Boolean
        get() = value.x
}

open class SysOutput<T> internal constructor(
        name: String, parent: SysObject? = null, signalWrite: SysSignalWrite<T>? = null
) : SysPort<SysSignalWrite<T>>(name, parent, signalWrite) {
    var value: T
        get() = throw UnsupportedOperationException("Signal read is not supported for output port")
        set(value) {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            bound!!.value = value
        }
}

open class SysFIFOInput<T> constructor(
        name: String, parent: SysObject? = null, fifo: SysFIFOInterface<T>? = null
) : SysPort<SysFIFOInterface<T>>(name, parent, fifo) {
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
    var pop: SysWireState
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for pop port")
        set(value) {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            bound!!.pop = value
        }
}

open class SysFIFOOutput<T> constructor(
        name: String, parent: SysObject? = null, fifo: SysFIFOInterface<T>? = null
) : SysPort<SysFIFOInterface<T>>(name, parent, fifo) {
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
    var push: SysWireState
        get() = throw UnsupportedOperationException(
                "SysFifoPort $name: Read is not supported for push port")
        set(value) {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            bound!!.push = value
        }
}


open class SysBusPort<T> constructor(
        name: String, parent: SysObject? = null, bus: SysBus<T>? = null
) : SysPort<SysBus<T>>(name, parent, bus) {

    operator fun get(index: Int): T {
        if (bound == null) throw IllegalStateException("Port $name is not bound")
        return bound!![index]
    }

    fun set(value: T, index: Int) {
        if (bound == null) throw IllegalStateException("Port $name is not bound")
        bound!!.set(value, index)
    }
}

open class SysBusInput<T> constructor(
        name: String, parent: SysObject? = null, bus: SysBus<T>? = null
) : SysPort<SysBus<T>>(name, parent, bus) {

    operator fun get(index: Int): T {
        if (bound == null) throw IllegalStateException("Port $name is not bound")
        return bound!![index]
    }
}


open class SysBusOutput<T> constructor(
        name: String, parent: SysObject? = null, bus: SysBus<T>? = null
) : SysPort<SysBus<T>>(name, parent, bus) {

    fun set(value: T, index: Int) {
        if (bound == null) throw IllegalStateException("Port $name is not bound")
        bound!!.set(value, index)
    }
}

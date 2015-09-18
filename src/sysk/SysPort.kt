package sysk

internal open class SysPort<IF : SysInterface>(name: String, parent: SysObject? = null, sysInterface: IF? = null):
        SysObject(name, parent) {

    protected var bound: IF? = null
        private set

    init {
        sysInterface?.let { bind(it) }
    }

    fun bind(port: SysPort<IF>): Unit = bind(port())

    fun bind(sysInterface: IF) {
        assert(bound == null) { "Port $name is already bound to $bound" }
        bound = sysInterface
        sysInterface.register(this)
    }

    fun to(port: SysPort<IF>): Pair<SysPort<IF>, IF> = Pair(this, port())

    fun to(sysInterface: IF): Pair<SysPort<IF>, IF> = Pair(this, sysInterface)

    fun invoke(): IF {
        assert(bound != null) { "Port $name is not bound" }
        return bound!!
    }

    val defaultEvent: SysWait.Finder = object: SysWait.Finder {
        override fun invoke() = bound?.defaultEvent
    }
}

fun <IF: SysInterface> bind(vararg pairs: Pair<SysPort<IF>, IF>) {
    for (pair in pairs) {
        pair.first.bind(pair.second)
    }
}

internal open class SysInput<T>(name: String, parent: SysObject? = null, signalRead: SysSignalRead<T>? = null):
        SysPort<SysSignalRead<T>>(name, parent, signalRead) {
    val value: T
        get() = bound?.value ?: throw IllegalStateException("Port $name is not bound")
}

internal class SysWireInput(name: String, parent: SysObject? = null, signalRead: SysWireRead? = null):
        SysInput<SysWireState>(name, parent, signalRead) {
    val posEdgeEvent: SysWait.Finder = object: SysWait.Finder {
        override fun invoke() = (bound as? SysWireRead)?.posEdgeEvent
    }

    val negEdgeEvent: SysWait.Finder = object: SysWait.Finder {
        override fun invoke() = (bound as? SysWireRead)?.negEdgeEvent
    }

    val zero: Boolean
        get() = value.zero

    val one: Boolean
        get() = value.one

    val x: Boolean
        get() = value.x
}

internal open class SysOutput<T>(name: String, parent: SysObject? = null, signalWrite: SysSignalWrite<T>? = null):
        SysPort<SysSignalWrite<T>>(name, parent, signalWrite) {
    var value: T
        get() = throw UnsupportedOperationException("Signal read is not supported for output port")
        set(value) {
            if (bound == null) throw IllegalStateException("Port $name is not bound")
            bound!!.value = value
        }
}
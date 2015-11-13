package sysk

interface SysInterface {
    // TODO: do we really need this?
    fun register(port: SysPort<*>)

    val defaultEvent: SysWait.Event
}

interface SysSignalRead<T>: SysInterface {

    val value: T
        get() = read()

    fun read(): T
}

interface SysEdged {

    val posEdgeEvent: SysWait

    val negEdgeEvent: SysWait
}

interface SysWireRead : SysSignalRead<SysWireState>, SysEdged {

    override val posEdgeEvent: SysWait.Event

    override val negEdgeEvent: SysWait.Event
}

interface SysSignalWrite<T>: SysInterface {

    var value: T
        get() { throw UnsupportedOperationException("Signal read is not supported for writer") }
        set(value) = write(value)

    fun write(value: T)
}


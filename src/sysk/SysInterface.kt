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

interface SysWireRead : SysSignalRead<SysWireState> {

    val posEdgeEvent: SysWait.Event

    val negEdgeEvent: SysWait.Event
}

interface SysSignalWrite<T>: SysInterface {

    var value: T
        get() { throw UnsupportedOperationException("Signal read is not supported for writer") }
        set(value) = write(value)

    fun write(value: T)
}


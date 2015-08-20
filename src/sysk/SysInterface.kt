package sysk

public interface SysInterface {
    // TODO: do we really need this?
    fun register(port: SysPort<*>)

    val defaultEvent: SysWait.Event
}

public interface SysSignalRead<T>: SysInterface {

    public val value: T
        get() = read()

    fun read(): T
}

public interface SysBooleanRead: SysSignalRead<Boolean> {

    val posEdgeEvent: SysWait.Event

    val negEdgeEvent: SysWait.Event
}

public interface SysSignalWrite<T>: SysInterface {

    public var value: T
        get() { throw UnsupportedOperationException("Signal read is not supported for writer") }
        set(value) = write(value)

    fun write(value: T)
}


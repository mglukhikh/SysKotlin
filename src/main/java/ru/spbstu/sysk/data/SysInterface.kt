package ru.spbstu.sysk.data

import ru.spbstu.sysk.core.SysWait

interface SysInterface {
    fun register(port: SysPort<*>)

    val defaultEvent: SysWait.Event
}

interface SysSignalRead<T : SysData>: SysInterface {

    val value: T
        get() = read()

    fun read(): T
}

interface SysEdged {

    val posEdgeEvent: SysWait

    val negEdgeEvent: SysWait
}

interface SysBitRead : SysSignalRead<SysBit>, SysEdged {

    override val posEdgeEvent: SysWait.Event

    override val negEdgeEvent: SysWait.Event
}

interface SysSignalWrite<T : SysData>: SysInterface {

    var value: T
        get() { throw UnsupportedOperationException("Signal read is not supported for writer") }
        set(value) = write(value)

    fun write(value: T)
}


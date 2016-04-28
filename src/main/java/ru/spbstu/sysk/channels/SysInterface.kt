package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysBit

interface SysInterface {
    fun register(port: SysPort<*>)

    val defaultEvent: SysWait.Event

    val name: String
}

interface SysSignalRead<out T : SysData>: SysInterface {

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


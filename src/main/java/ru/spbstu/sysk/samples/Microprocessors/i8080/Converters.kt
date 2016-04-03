package ru.spbstu.sysk.samples.microprocessors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.integer

class SysConverter constructor(val capacity: Int, name: String, parent: SysModule) : SysModule(name, parent) {

    val signalPort = bidirPort<SysInteger>("signal")
    val signalsPort = Array(capacity, { bidirPort<SysBit>("signal.$it") })

    init {
        function(signalPort.defaultEvent) {
            if (signalPort().width != capacity) throw IllegalArgumentException()
            for (i in 0..capacity - 1) signalsPort[i](signalPort()[i])
        }
        function(SysWait.OneOf(Array(capacity, { signalsPort[it].defaultEvent }).toList())) {
            signalPort(integer(Array(capacity, { signalsPort[it]() })))
        }
    }
}
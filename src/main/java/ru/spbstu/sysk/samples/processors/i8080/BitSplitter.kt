package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.integer

class BitSplitter constructor(capacity: Int, name: String, parent: SysModule) : SysModule(name, parent) {

    val integerPort = bidirPort<SysInteger>("signal")
    val bitPorts = Array(capacity, { bidirPort<SysBit>("signal.$it") })

    init {
        function(integerPort.defaultEvent) {
            if (integerPort().width != capacity) throw IllegalArgumentException()
            for (i in 0..capacity - 1) bitPorts[i](integerPort()[i])
        }
        function(SysWait.OneOf(Array(capacity, { bitPorts[it].defaultEvent }).toList())) {
            integerPort(integer(Array(capacity, { bitPorts[it]() })))
        }
    }
}
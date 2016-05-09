package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned

class Gateway constructor(
        capacity: Int,
        parent: SysModule
) : SysModule("Gateway", parent) {

    val front = bidirPort<SysUnsigned>("front")
    val back = bidirPort<SysUnsigned>("back")
    val en = bitInput("en")

    init {
        function(front.defaultEvent, false) {
            if (front().width != capacity) throw IllegalArgumentException()
            if (back().width != capacity) throw IllegalArgumentException()
            if (en.one) back(front())
        }

        function(back.defaultEvent, false) {
            if (front().width != capacity) throw IllegalArgumentException()
            if (back().width != capacity) throw IllegalArgumentException()
            if (en.one) front(back())
        }
    }
}
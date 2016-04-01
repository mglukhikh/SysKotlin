package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysData

/**
 * Register for storing any kind of information
 */
class SysRegister <T : SysData> (name: String, defValue: T, parent: SysModule): SysModule(name, parent) {

    val d = input<T>("d")      // data input
    private val dinp by portReader(d)

    val en = bitInput("en")   // enable
    val clk = bitInput("clk") // clock

    private var value = defValue

    val q = output<T>("q")     // data output
    private var qout by portWriter(q)

    init {
        stateFunction(clk) {
            init {
                qout = value
            }
            infiniteState {
                if (en.one) {
                    qout = dinp
                    value = dinp
                } else {
                    qout = value
                }
            }
        }
    }
}

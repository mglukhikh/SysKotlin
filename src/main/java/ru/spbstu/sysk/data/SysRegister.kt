package ru.spbstu.sysk.data

import ru.spbstu.sysk.core.SysModule

/**
 * Register for storing any kind of information
 */
class SysRegister <T : SysData> (name: String, defValue: T, parent: SysModule): SysModule(name, parent) {

    val d = readOnlyPort<T>("d")      // data input
    private val dinp by d

    val en = bitInput("en")   // enable
    val clk = bitInput("clk") // clock

    private var value = defValue

    val q = readWritePort<T>("q")     // data output
    private var qout by q

    init {
        stateFunction(clk) {
            Init {
                qout = value
            }
            InfiniteState {
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

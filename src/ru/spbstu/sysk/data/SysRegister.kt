package ru.spbstu.sysk.data

import ru.spbstu.sysk.core.SysModule

/**
 * Register for storing any kind of information
 */
public class SysRegister <T : SysData> (name: String, defValue: T, parent: SysModule): SysModule(name, parent) {

    val d = input<T>("d")      // data input
    val en = bitInput("en")   // enable
    val clk = bitInput("clk") // clock

    private var state = defValue
    val q = output<T>("q")     // data output

    init {
        stateFunction(clk) {
            init {
                q.value = this@SysRegister.state
            }
            infinite {
                if (en.one) {
                    q.value = d.value
                    this@SysRegister.state = d.value
                } else {
                    q.value = this@SysRegister.state
                }
            }
        }
    }
}

package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysData

class SysRegister <T : SysData>(
        name: String, defaultValue: T, parent: SysModule
) : SysModule(name, parent) {

    val d = input<T>("d")
    val q = output<T>("q")
    val en = bitInput("en")
    val clk = bitInput("clk")

    private var value = defaultValue

    init {
        stateFunction(clk) {
            init {
                q(value)
            }
            infinite.state {
                if (en.one) {
                    q(d())
                    value = d()
                } else {
                    q(value)
                }
            }
        }
    }
}

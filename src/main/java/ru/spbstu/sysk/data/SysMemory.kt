package ru.spbstu.sysk.data

import ru.spbstu.sysk.core.SysModule

open class SysMemory<T : SysData>(
        name: String,
        private val addrWidth: Int,
        private val defaultValue: T,
        parent: SysModule,
        private val correct: (T) -> Boolean = { true }
) : SysModule(name, parent) {

    private val storage = hashMapOf<Long, T>()

    val din = input<T>("din")
    val dout = output<T>("dout")
    val addr = input<SysInteger>("addr")

    val en = bitInput("en")   // enable
    val clk = bitInput("clk") // clock
    val wr = bitInput("wr")   // 1 = write, 0 = read

    init {
        stateFunction(clk) {
            init {
                dout(defaultValue)
            }
            infiniteState {
                assert(addr().width == addrWidth) {
                    "Inconsistent memory address: required width $addrWidth, got ${addr()}"
                }
                if (en.one) {
                    if (wr.one) {
                        assert(correct(din())) {
                            "Inconsistent memory $name data: $din()"
                        }
                        storage[addr().toLong()] = din()
                    }
                    else {
                        dout(storage[addr().toLong()] ?: defaultValue)
                    }
                }
            }
        }
    }
}

class SysIntegerMemory(
        name: String,
        private val addrWidth: Int,
        private val dataWidth: Int,
        parent: SysModule
) : SysMemory<SysInteger>(name, addrWidth, SysInteger.uninitialized(dataWidth), parent, {
    din -> din.width == dataWidth
})

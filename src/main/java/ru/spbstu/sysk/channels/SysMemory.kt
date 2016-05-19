package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.SysUnsigned

open class SysMemory<T : SysData>(
        name: String,
        private val addrWidth: Int,
        defaultValue: T,
        parent: SysModule,
        correct: (T) -> Boolean = { true }
) : SysModule(name, parent) {

    private val storage = hashMapOf<Long, T>()

    val din = input<T>("din")
    val dout = output<T>("dout")
    val addr = input<SysUnsigned>("addr")

    val en = bitInput("en")   // enable
    val clk = bitInput("clk") // clock
    val wr = bitInput("wr")   // 1 = write, 0 = read

    init {
        stateFunction(clk) {
            init {
                dout(defaultValue)
            }
            infinite.state {
                assert(addr().width == addrWidth) {
                    "Inconsistent memory address: required width $addrWidth, got ${addr()}"
                }
                if (en.one) {
                    if (wr.one) {
                        assert(correct(din())) {
                            "Inconsistent memory $name data: $din()"
                        }
                        storage[addr().toLong()] = din()
                        dout(din())
                    }
                    else {
                        dout(storage[addr().toLong()] ?: defaultValue)
                    }
                }
            }
        }
    }

    fun lastAddress(): Long {
        var result = 1
        for (i in 0..addrWidth-1) {
            result *= 2
        }
        return result - 1L
    }

    fun load(what: (Long) -> T?) {
        for (address in 0..lastAddress()) {
            val data = what(address) ?: continue
            storage[address] = data
        }
    }

    fun check(what: (Long) -> T?): Boolean {
        for (address in 0..lastAddress()) {
            val data = what(address) ?: continue
            if (storage[address] != data) return false
        }
        return true
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

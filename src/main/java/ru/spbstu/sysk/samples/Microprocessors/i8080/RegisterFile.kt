package ru.spbstu.sysk.samples.microprocessors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned

val A = unsigned(4, 0)
val Flag = unsigned(4, 1)
val B = unsigned(4, 2)
val C = unsigned(4, 3)
val D = unsigned(4, 4)
val E = unsigned(4, 5)
val H = unsigned(4, 6)
val L = unsigned(4, 7)
val PCF = unsigned(4, 8)
val PCS = unsigned(4, 9)
val SPF = unsigned(4, 10)
val SPS = unsigned(4, 11)

val STORAGE = unsigned(2, 0)
val WRITE = unsigned(2, 1)
val READ = unsigned(2, 2)
val RESET = unsigned(2, 3)

class RegisterFile(parent: SysModule) : SysModule("RegisterFile", parent) {

    private val PSW = unsigned(16, 0)
    private val BC = unsigned(16, 0)
    private val DE = unsigned(16, 0)
    private val HL = unsigned(16, 0)
    private val PC = unsigned(16, 0)
    private val SP = unsigned(16, 0)

    val data = port<SysUnsigned>("data")
    val address = input<SysUnsigned>("address")
    val command = input<SysUnsigned>("command")

    val clk = bitInput("clk")

    init {
        stateFunction(clk, true) {
            infiniteState {
                when (command()) {
                    STORAGE -> {
                    }
                    WRITE -> write(address())
                    READ -> read(address())
                    RESET -> reset()
                }
            }
        }
    }

    private fun reset() {
    }

    private fun read(address: SysUnsigned) {
        when (address) {
            A -> data(PSW[7, 0])
            Flag -> data(PSW[15, 8])
            B -> data(BC[7, 0])
            C -> data(BC[15, 8])
            D -> data(DE[7, 0])
            E -> data(DE[15, 8])
            H -> data(HL[7, 0])
            L -> data(HL[15, 8])
            PCF -> data(PC[7, 0])
            PCS -> data(PC[15, 8])
            SPF -> data(SP[7, 0])
            SPS -> data(SP[15, 8])
        }
    }

    private fun write(address: SysUnsigned) {
        when (address) {
            A -> PSW.set(7, 0, data())
            Flag -> PSW.set(15, 8, data())
            B -> BC.set(7, 0, data())
            C -> BC.set(15, 8, data())
            D -> DE.set(7, 0, data())
            E -> DE.set(15, 8, data())
            H -> HL.set(7, 0, data())
            L -> HL.set(15, 8, data())
            PCF -> PC.set(7, 0, data())
            PCS -> PC.set(15, 8, data())
            SPF -> SP.set(7, 0, data())
            SPS -> SP.set(15, 8, data())
        }
    }
}

// Slow?
internal fun SysUnsigned.set(i: Int, bit: Boolean): SysUnsigned {
    if (i < 0 || i >= width) throw IndexOutOfBoundsException()
    if (this[i].one == bit) return this
    val mask = unsigned(width, 1 shl i)
    return this xor mask
}

internal fun SysUnsigned.set(j: Int, i: Int, bits: SysUnsigned): SysUnsigned {
    if (j < i) throw IllegalArgumentException()
    if (j >= width || i < 0) throw IndexOutOfBoundsException()
    var temp = this
    for (it in i..j) temp = temp.set(it, bits[it - i].one)
    return temp
}
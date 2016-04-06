package ru.spbstu.sysk.samples.microprocessors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.*

object REGISTER {
    val A = unsigned(4, 0)
    val Flag = unsigned(4, 1)
    val B = unsigned(4, 2)
    val C = unsigned(4, 3)
    val D = unsigned(4, 4)
    val E = unsigned(4, 5)
    val H = unsigned(4, 6)
    val L = unsigned(4, 7)

    val PSW = unsigned(4, 0)
    val BC = unsigned(4, 2)
    val DE = unsigned(4, 4)
    val HL = unsigned(4, 6)
    val PC = unsigned(4, 8)
    val SP = unsigned(4, 10)
}

object COMMAND {
    val STORAGE = unsigned(3, 0)
    val WRITE = unsigned(3, 1)
    val READ = unsigned(3, 2)
    val INC = unsigned(3, 3)
    val DEC = unsigned(3, 4)
    val SHL = unsigned(3, 5)
    val SHR = unsigned(3, 6)
    val RESET = unsigned(3, 7)
}

class RegisterFile constructor(val capacity: Int, parent: SysModule) : SysModule("RegisterFile", parent) {

    private var PSW = unsigned(capacity * 2, 0)
    private var BC = unsigned(capacity * 2, 0)
    private var DE = unsigned(capacity * 2, 0)
    private var HL = unsigned(capacity * 2, 0)
    private var PC = unsigned(capacity * 2, 0)
    private var SP = unsigned(capacity * 2, 0)

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")
    val register = input<SysUnsigned>("register")
    val command = input<SysUnsigned>("command")

    val clk = bitInput("clk")

    init {
        stateFunction(clk, true) {
            infiniteState {
                when (command()) {
                    COMMAND.STORAGE -> {}
                    COMMAND.WRITE -> write(register())
                    COMMAND.READ -> read(register())
                    COMMAND.INC -> inc(register())
                    COMMAND.DEC -> dec(register())
                    COMMAND.SHL -> shl(register())
                    COMMAND.SHR -> shr(register())
                    COMMAND.RESET -> reset()
                }
            }
        }
    }

    private fun shl(register: SysUnsigned) {
        when (register) {
            REGISTER.PSW -> PSW shl 1
            REGISTER.BC -> BC shl 1
            REGISTER.DE -> DE shl 1
            REGISTER.HL -> HL shl 1
            REGISTER.PC -> PC shl 1
            REGISTER.SP -> SP shl 1
        }
    }

    private fun shr(register: SysUnsigned) {
        when (register) {
            REGISTER.PSW -> PSW shr 1
            REGISTER.BC -> BC shr 1
            REGISTER.DE -> DE shr 1
            REGISTER.HL -> HL shr 1
            REGISTER.PC -> PC shr 1
            REGISTER.SP -> SP shr 1
        }
    }

    private fun inc(register: SysUnsigned) {
        when (register) {
            REGISTER.PSW -> ++PSW
            REGISTER.BC -> ++BC
            REGISTER.DE -> ++DE
            REGISTER.HL -> ++HL
            REGISTER.PC -> ++PC
            REGISTER.SP -> ++SP
        }
    }

    private fun dec(register: SysUnsigned) {
        when (register) {
            REGISTER.PSW -> --PSW
            REGISTER.BC -> --BC
            REGISTER.DE -> --DE
            REGISTER.HL -> --HL
            REGISTER.PC -> --PC
            REGISTER.SP -> --SP
        }
    }

    private fun reset() {
        PSW = unsigned(capacity * 2, 0)
        BC = unsigned(capacity * 2, 0)
        DE = unsigned(capacity * 2, 0)
        HL = unsigned(capacity * 2, 0)
        PC = unsigned(capacity * 2, 0)
        SP = unsigned(capacity * 2, 0)
    }

    private fun read(register: SysUnsigned) {
        when (register) {
            REGISTER.A -> data(PSW[capacity - 1, 0])
            REGISTER.Flag -> data(PSW[capacity * 2 - 1, capacity])
            REGISTER.B -> data(BC[capacity - 1, 0])
            REGISTER.C -> data(BC[capacity * 2 - 1, capacity])
            REGISTER.D -> data(DE[capacity - 1, 0])
            REGISTER.E -> data(DE[capacity * 2 - 1, capacity])
            REGISTER.H -> data(HL[capacity - 1, 0])
            REGISTER.L -> data(HL[capacity * 2 - 1, capacity])
            REGISTER.PC -> address(PC)
            REGISTER.SP -> address(SP)
        }
    }

    private fun write(register: SysUnsigned) {
        if (data().width != capacity) throw IllegalArgumentException("${data().width}")
        when (register) {
            REGISTER.A -> PSW = PSW.set(capacity - 1, 0, data())
            REGISTER.Flag -> PSW = PSW.set(capacity * 2 - 1, capacity, data())
            REGISTER.B -> BC = BC.set(capacity - 1, 0, data())
            REGISTER.C -> BC = BC.set(capacity * 2 - 1, capacity, data())
            REGISTER.D -> DE = DE.set(capacity - 1, 0, data())
            REGISTER.E -> DE = DE.set(capacity * 2 - 1, capacity, data())
            REGISTER.H -> HL = HL.set(capacity - 1, 0, data())
            REGISTER.L -> HL = HL.set(capacity * 2 - 1, capacity, data())
            REGISTER.PC -> PC = address()[capacity * 2 - 1, 0]
            REGISTER.SP -> SP = address()[capacity * 2 - 1, 0]
        }
    }
}

// Slow?
internal fun <T : SysInteger> T.set(i: Int, bit: Boolean): T {
    if (i >= SysLongInteger.MAX_WIDTH) throw UnsupportedOperationException()
    if (i < 0 || i >= width) throw IndexOutOfBoundsException()
    if (this[i].one == bit) return this
    val mask = SysInteger(width, 1L shl i)
    @Suppress("UNCHECKED_CAST")
    return (this xor mask) as T
}

internal fun <T : SysInteger> T.set(j: Int, i: Int, bits: T) = sets(j, i, bits.bits())

internal fun <T : SysInteger> T.sets(j: Int, i: Int, bits: Array<SysBit>): T {
    if (j >= SysLongInteger.MAX_WIDTH) throw UnsupportedOperationException()
    if (j < i) throw IllegalArgumentException()
    if (j >= width || i < 0) throw IndexOutOfBoundsException()
    if ((j - i + 1) != bits.size) throw IllegalArgumentException()
    var temp = this
    for (it in i..j) temp = temp.set(it, bits[it - i].one)
    return temp
}
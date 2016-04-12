package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.*

class RegisterFile constructor(
        val capacityData: Int,
        capacityAddress: Int,
        capacityRegisterAddress: Int,
        capacityCommand: Int,
        parent: SysModule
) : SysModule("RegisterFile", parent) {

    private var PSW = unsigned(capacityData * 2, 0)
    private var BC = unsigned(capacityData * 2, 0)
    private var DE = unsigned(capacityData * 2, 0)
    private var HL = unsigned(capacityData * 2, 0)
    private var PC = unsigned(capacityData * 2, 0)
    private var SP = unsigned(capacityData * 2, 0)

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")
    val register = input<REGISTER>("register")
    val command = input<COMMAND>("command")

    val en = bitInput("en")
    val clk = bitInput("clk")

    init {
        stateFunction(clk) {
            infiniteState {
                if (command().width != capacityCommand) throw IllegalArgumentException()
                if (register().width != capacityRegisterAddress) throw IllegalArgumentException()
                if (data().width != capacityData) throw IllegalArgumentException()
                if (address().width != capacityAddress) throw IllegalArgumentException()
                if (en().one) when (command()) {
                    COMMAND.WRITE -> write(register())
                    COMMAND.READ -> read(register())
                    COMMAND.INC -> inc(register())
                    COMMAND.DEC -> dec(register())
                    COMMAND.SHL -> shl(register())
                    COMMAND.SHR -> shr(register())
                    COMMAND.RESET -> reset()
                    else -> {}
                }
            }
        }
    }

    private fun shl(register: REGISTER) {
        when (register) {
            REGISTER.PSW -> PSW shl 1
            REGISTER.BC -> BC shl 1
            REGISTER.DE -> DE shl 1
            REGISTER.HL -> HL shl 1
            REGISTER.PC -> PC shl 1
            REGISTER.SP -> SP shl 1
            else -> {}
        }
    }

    private fun shr(register: REGISTER) {
        when (register) {
            REGISTER.PSW -> PSW shr 1
            REGISTER.BC -> BC shr 1
            REGISTER.DE -> DE shr 1
            REGISTER.HL -> HL shr 1
            REGISTER.PC -> PC shr 1
            REGISTER.SP -> SP shr 1
            else -> {}
        }
    }

    private fun inc(register: REGISTER) {
        when (register) {
            REGISTER.PSW -> ++PSW
            REGISTER.BC -> ++BC
            REGISTER.DE -> ++DE
            REGISTER.HL -> ++HL
            REGISTER.PC -> ++PC
            REGISTER.SP -> ++SP
            else -> {}
        }
    }

    private fun dec(register: REGISTER) {
        when (register) {
            REGISTER.PSW -> --PSW
            REGISTER.BC -> --BC
            REGISTER.DE -> --DE
            REGISTER.HL -> --HL
            REGISTER.PC -> --PC
            REGISTER.SP -> --SP
            else -> {}
        }
    }

    private fun reset() {
        PSW = unsigned(capacityData * 2, 0)
        BC = unsigned(capacityData * 2, 0)
        DE = unsigned(capacityData * 2, 0)
        HL = unsigned(capacityData * 2, 0)
        PC = unsigned(capacityData * 2, 0)
        SP = unsigned(capacityData * 2, 0)
    }

    private fun read(register: REGISTER) {
        when (register) {
            REGISTER.A -> data(PSW[capacityData - 1, 0])
            REGISTER.Flag -> data(PSW[capacityData * 2 - 1, capacityData])
            REGISTER.B -> data(BC[capacityData - 1, 0])
            REGISTER.C -> data(BC[capacityData * 2 - 1, capacityData])
            REGISTER.D -> data(DE[capacityData - 1, 0])
            REGISTER.E -> data(DE[capacityData * 2 - 1, capacityData])
            REGISTER.H -> data(HL[capacityData - 1, 0])
            REGISTER.L -> data(HL[capacityData * 2 - 1, capacityData])
            REGISTER.PC -> address(PC)
            REGISTER.SP -> address(SP)
            else -> {}
        }
    }

    private fun write(register: REGISTER) {
        when (register) {
            REGISTER.A -> PSW = PSW.set(capacityData - 1, 0, data())
            REGISTER.Flag -> PSW = PSW.set(capacityData * 2 - 1, capacityData, data())
            REGISTER.B -> BC = BC.set(capacityData - 1, 0, data())
            REGISTER.C -> BC = BC.set(capacityData * 2 - 1, capacityData, data())
            REGISTER.D -> DE = DE.set(capacityData - 1, 0, data())
            REGISTER.E -> DE = DE.set(capacityData * 2 - 1, capacityData, data())
            REGISTER.H -> HL = HL.set(capacityData - 1, 0, data())
            REGISTER.L -> HL = HL.set(capacityData * 2 - 1, capacityData, data())
            REGISTER.PC -> PC = address()[capacityData * 2 - 1, 0]
            REGISTER.SP -> SP = address()[capacityData * 2 - 1, 0]
            else -> {}
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
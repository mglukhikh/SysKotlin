package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.*

class RegisterFile constructor(
        private val capacityData: Int,
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

    private var current = REGISTER.A

    val inp = input<SysUnsigned>("inp")
    val out = output<SysUnsigned>("out")
    val B = output<SysUnsigned>("B")

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")
    val register = input<REGISTER>("register")
    val command = input<COMMAND>("command")

    val en = bitInput("en")
    val clk = bitInput("clk")

    init {
        stateFunction(clk) {
            infinite.state {
                if (command().width != capacityCommand) throw IllegalArgumentException()
                if (register().width != capacityRegisterAddress) throw IllegalArgumentException()
                if (data().width != capacityData) throw IllegalArgumentException()
                if (address().width != capacityAddress) throw IllegalArgumentException()
                if (en.one) when (command()) {
                    COMMAND.WRITE_DATA -> write(data(), register())
                    COMMAND.WRITE_ADDRESS -> write(address(), register())
                    COMMAND.READ_DATA -> data(read(register()) ?: throw IllegalArgumentException("$register is not found"))
                    COMMAND.READ_ADDRESS -> address(read(register()) ?: throw IllegalArgumentException("$register is not found"))
                    COMMAND.SET_A -> out(read(register()) ?: throw IllegalArgumentException("$register is not found"))
                    COMMAND.SET_B -> B(read(register()) ?: throw IllegalArgumentException("$register is not found"))
                    COMMAND.SET_CURRENT -> current = register()
                    COMMAND.INC -> inc(register())
                    COMMAND.DEC -> dec(register())
                    COMMAND.SHL -> shl(register())
                    COMMAND.SHR -> shr(register())
                    COMMAND.RESET -> reset()
                    else -> {
                    }
                }
            }
        }
        function(inp.defaultEvent) { write(inp(), current) }
    }

    private fun shl(register: REGISTER) = when (register) {
        REGISTER.PSW -> PSW shl 1
        REGISTER.BC -> BC shl 1
        REGISTER.DE -> DE shl 1
        REGISTER.HL -> HL shl 1
        REGISTER.PC -> PC shl 1
        REGISTER.SP -> SP shl 1
        else -> null
    }

    private fun shr(register: REGISTER) = when (register) {
        REGISTER.PSW -> PSW shr 1
        REGISTER.BC -> BC shr 1
        REGISTER.DE -> DE shr 1
        REGISTER.HL -> HL shr 1
        REGISTER.PC -> PC shr 1
        REGISTER.SP -> SP shr 1
        else -> null
    }

    private fun inc(register: REGISTER) = when (register) {
        REGISTER.PSW -> ++PSW
        REGISTER.BC -> ++BC
        REGISTER.DE -> ++DE
        REGISTER.HL -> ++HL
        REGISTER.PC -> ++PC
        REGISTER.SP -> ++SP
        else -> null
    }

    private fun dec(register: REGISTER) = when (register) {
        REGISTER.PSW -> --PSW
        REGISTER.BC -> --BC
        REGISTER.DE -> --DE
        REGISTER.HL -> --HL
        REGISTER.PC -> --PC
        REGISTER.SP -> --SP
        else -> null
    }

    private fun reset() {
        PSW = unsigned(capacityData * 2, 0)
        BC = unsigned(capacityData * 2, 0)
        DE = unsigned(capacityData * 2, 0)
        HL = unsigned(capacityData * 2, 0)
        PC = unsigned(capacityData * 2, 0)
        SP = unsigned(capacityData * 2, 0)
    }

    private fun read(register: REGISTER): SysUnsigned? = when (register) {
        REGISTER.A -> PSW[capacityData - 1, 0]
        REGISTER.Flag -> PSW[capacityData * 2 - 1, capacityData]
        REGISTER.B -> BC[capacityData - 1, 0]
        REGISTER.C -> BC[capacityData * 2 - 1, capacityData]
        REGISTER.D -> DE[capacityData - 1, 0]
        REGISTER.E -> DE[capacityData * 2 - 1, capacityData]
        REGISTER.H -> HL[capacityData - 1, 0]
        REGISTER.L -> HL[capacityData * 2 - 1, capacityData]
        REGISTER.BC -> BC
        REGISTER.DE -> DE
        REGISTER.HL -> HL
        REGISTER.PSW -> PSW
        REGISTER.PC -> PC
        REGISTER.SP -> SP
        else -> null
    }

    private fun write(value: SysUnsigned, register: REGISTER) = when (register) {
        REGISTER.A -> PSW = PSW.set(capacityData - 1, 0, value)
        REGISTER.Flag -> PSW = PSW.set(capacityData * 2 - 1, capacityData, value)
        REGISTER.B -> BC = BC.set(capacityData - 1, 0, value)
        REGISTER.C -> BC = BC.set(capacityData * 2 - 1, capacityData, value)
        REGISTER.D -> DE = DE.set(capacityData - 1, 0, value)
        REGISTER.E -> DE = DE.set(capacityData * 2 - 1, capacityData, value)
        REGISTER.H -> HL = HL.set(capacityData - 1, 0, value)
        REGISTER.L -> HL = HL.set(capacityData * 2 - 1, capacityData, value)
        REGISTER.BC -> BC = value[capacityData * 2 - 1, 0]
        REGISTER.DE -> DE = value[capacityData * 2 - 1, 0]
        REGISTER.HL -> HL = value[capacityData * 2 - 1, 0]
        REGISTER.PSW -> PSW = value[capacityData * 2 - 1, 0]
        REGISTER.PC -> PC = value[capacityData * 2 - 1, 0]
        REGISTER.SP -> SP = value[capacityData * 2 - 1, 0]
        else -> null
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
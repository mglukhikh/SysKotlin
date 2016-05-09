package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Assert
import ru.spbstu.sysk.core.SysModule
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
    val flag = bidirPort<SysUnsigned>("flag")

    val inc = bitInput("inc")
    val read = bitInput("read")
    val pc = output<SysUnsigned>("pc")

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")
    val register = input<REGISTER>("register")
    val command = input<COMMAND>("command")

    val en = bitInput("en")
    val clk = bitInput("clk")

    init {
        if (capacityAddress != capacityData * 2) throw IllegalArgumentException()
        function(inc.posEdgeEvent, false) { inc(REGISTER.PC) }
        function(read.posEdgeEvent, false) { pc(read(REGISTER.PC)) }
        function(inp.defaultEvent, false) {
            write(inp(), current)
            changed(current)
        }
        function(flag.defaultEvent, false) {
            write(flag(), REGISTER.Flag)
            changed(REGISTER.Flag)
        }
        stateFunction(clk) {
            infinite.state {
                Assert.assertEquals(capacityCommand, command().width)
                Assert.assertEquals(capacityRegisterAddress, register().width)
                Assert.assertEquals(capacityData, data().width)
                Assert.assertEquals(capacityAddress, address().width)
                if (en.one) when (command()) {
                    COMMAND.WRITE_DATA -> {
                        write(data(), register())
                        changed(register())
                    }
                    COMMAND.WRITE_ADDRESS -> {
                        write(address(), register())
                        changed(register())
                    }
                    COMMAND.READ_DATA -> data(read(register()).truncate(capacityData) as SysUnsigned)
                    COMMAND.READ_ADDRESS -> address(read(register()).truncate(capacityData * 2) as SysUnsigned)
                    COMMAND.SET_A -> out(read(register()))
                    COMMAND.SET_B -> B(read(register()))
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

    private fun read(register: REGISTER): SysUnsigned = when (register) {
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
        else -> throw IllegalArgumentException("$register is not found")
    }

    private fun write(value: SysUnsigned, register: REGISTER) = when (register) {
        REGISTER.A -> PSW = PSW.set(capacityData - 1, 0, value.truncate(capacityData) as SysUnsigned)
        REGISTER.Flag -> {
            PSW = PSW.set(capacityData * 2 - 1, capacityData, value.truncate(capacityData) as SysUnsigned)
            flag(read(REGISTER.Flag))
        }
        REGISTER.B -> BC = BC.set(capacityData - 1, 0, value.truncate(capacityData) as SysUnsigned)
        REGISTER.C -> BC = BC.set(capacityData * 2 - 1, capacityData, value.truncate(capacityData) as SysUnsigned)
        REGISTER.D -> DE = DE.set(capacityData - 1, 0, value.truncate(capacityData) as SysUnsigned)
        REGISTER.E -> DE = DE.set(capacityData * 2 - 1, capacityData, value.truncate(capacityData) as SysUnsigned)
        REGISTER.H -> HL = HL.set(capacityData - 1, 0, value.truncate(capacityData) as SysUnsigned)
        REGISTER.L -> HL = HL.set(capacityData * 2 - 1, capacityData, value.truncate(capacityData) as SysUnsigned)
        REGISTER.BC -> BC = value.truncate(capacityData * 2) as SysUnsigned
        REGISTER.DE -> DE = value.truncate(capacityData * 2) as SysUnsigned
        REGISTER.HL -> HL = value.truncate(capacityData * 2) as SysUnsigned
        REGISTER.PSW -> PSW = value.truncate(capacityData * 2) as SysUnsigned
        REGISTER.PC -> PC = value.truncate(capacityData * 2) as SysUnsigned
        REGISTER.SP -> SP = value.truncate(capacityData * 2) as SysUnsigned
        else -> throw IllegalArgumentException("$register is not found")
    }

    private fun changed(register: REGISTER) = println("$register -> ${read(register)}")
}

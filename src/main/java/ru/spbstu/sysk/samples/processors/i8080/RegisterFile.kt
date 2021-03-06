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
    private var THL = unsigned(capacityData * 2, 0)

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
        function(inp.defaultEvent, false) { write(inp(), current) }
        function(flag.defaultEvent, false) { write(flag(), REGISTER.Flag) }
        stateFunction(clk) {
            infinite.state {
                Assert.assertEquals(capacityCommand, command().width)
                Assert.assertEquals(capacityRegisterAddress, register().width)
                Assert.assertEquals(capacityData, data().width)
                Assert.assertEquals(capacityAddress, address().width)
                if (en.one) when (command()) {
                    COMMAND.WRITE_DATA -> write(data(), register())
                    COMMAND.WRITE_ADDRESS -> write(address(), register())
                    COMMAND.READ_DATA -> data(read(register()).extend(capacityData) as SysUnsigned)
                    COMMAND.READ_ADDRESS -> address(read(register()).extend(capacityData * 2) as SysUnsigned)
                    COMMAND.SET_A -> out(read(register()))
                    COMMAND.SET_B -> B(read(register()))
                    COMMAND.SET_CURRENT -> current = register()
                    COMMAND.INC -> inc(register())
                    COMMAND.DEC -> dec(register())
                    COMMAND.SHL_A -> shl()
                    COMMAND.USL_A -> {
                        val flag = PSW[0]
                        shl()
                        PSW = PSW.set(capacityData, flag)
                    }
                    COMMAND.SHR_A -> shr()
                    COMMAND.USR_A -> {
                        val flag = PSW[0]
                        shr()
                        PSW = PSW.set(2 * capacityData - 1, flag)
                    }
                    COMMAND.SHL -> shl(register())
                    COMMAND.SHR -> shr(register())
                    COMMAND.RESET -> reset()
                    else -> {
                    }
                }
            }
        }
    }

    private fun shl() {
        PSW = PSW.set(0, PSW[2 * capacityData - 1])
        PSW = PSW.set(2 * capacityData - 1, capacityData + 1, PSW[2 * capacityData - 2, capacityData])
    }

    private fun shr() {
        PSW = PSW.set(0, PSW[capacityData])
        PSW = PSW.set(2 * capacityData - 2, capacityData, PSW[2 * capacityData - 1, capacityData + 1])
    }

    private fun shl(register: REGISTER) = when (register) {
        REGISTER.PSW -> PSW = PSW shl 1
        REGISTER.BC -> BC = BC shl 1
        REGISTER.DE -> DE = DE shl 1
        REGISTER.HL -> HL = HL shl 1
        REGISTER.PC -> PC = PC shl 1
        REGISTER.SP -> SP = SP shl 1
        REGISTER.THL -> THL = THL shl 1
        else -> null
    }

    private fun shr(register: REGISTER) = when (register) {
        REGISTER.PSW -> PSW = PSW shr 1
        REGISTER.BC -> BC = BC shr 1
        REGISTER.DE -> DE = DE shr 1
        REGISTER.HL -> HL = HL shr 1
        REGISTER.PC -> PC = PC shr 1
        REGISTER.SP -> SP = SP shr 1
        REGISTER.THL -> THL = THL shr 1
        else -> null
    }

    private fun inc(register: REGISTER) {
        val inc: (REGISTER) -> Unit = { write(read(it) + 1, it) }
        when (register) {
            REGISTER.A -> inc(register)
            REGISTER.Flag -> inc(register)
            REGISTER.B -> inc(register)
            REGISTER.C -> inc(register)
            REGISTER.D -> inc(register)
            REGISTER.E -> inc(register)
            REGISTER.H -> inc(register)
            REGISTER.L -> inc(register)
            REGISTER.TH -> inc(register)
            REGISTER.TL -> inc(register)
            REGISTER.PSW -> ++PSW
            REGISTER.BC -> ++BC
            REGISTER.DE -> ++DE
            REGISTER.HL -> ++HL
            REGISTER.PC -> ++PC
            REGISTER.SP -> ++SP
            REGISTER.THL -> ++THL
            else -> {
            }
        }
    }

    private fun dec(register: REGISTER) {
        val dec: (REGISTER) -> Unit = { write(read(it) - 1, it) }
        when (register) {
            REGISTER.A -> dec(register)
            REGISTER.Flag -> dec(register)
            REGISTER.B -> dec(register)
            REGISTER.C -> dec(register)
            REGISTER.D -> dec(register)
            REGISTER.E -> dec(register)
            REGISTER.H -> dec(register)
            REGISTER.L -> dec(register)
            REGISTER.TH -> dec(register)
            REGISTER.TL -> dec(register)
            REGISTER.PSW -> --PSW
            REGISTER.BC -> --BC
            REGISTER.DE -> --DE
            REGISTER.HL -> --HL
            REGISTER.PC -> --PC
            REGISTER.SP -> --SP
            REGISTER.THL -> --THL
            else -> {
            }
        }
    }

    private fun reset() {
        PSW = unsigned(capacityData * 2, 0)
        BC = unsigned(capacityData * 2, 0)
        DE = unsigned(capacityData * 2, 0)
        HL = unsigned(capacityData * 2, 0)
        PC = unsigned(capacityData * 2, 0)
        SP = unsigned(capacityData * 2, 0)
        THL = unsigned(capacityData * 2, 0)
    }

    private fun read(register: REGISTER): SysUnsigned = when (register) {
        REGISTER.A -> PSW[capacityData * 2 - 1, capacityData]
        REGISTER.Flag -> PSW[capacityData - 1, 0]
        REGISTER.B -> BC[capacityData * 2 - 1, capacityData]
        REGISTER.C -> BC[capacityData - 1, 0]
        REGISTER.D -> DE[capacityData * 2 - 1, capacityData]
        REGISTER.E -> DE[capacityData - 1, 0]
        REGISTER.H -> HL[capacityData * 2 - 1, capacityData]
        REGISTER.L -> HL[capacityData - 1, 0]
        REGISTER.TH -> THL[capacityData * 2 - 1, capacityData]
        REGISTER.TL -> THL[capacityData - 1, 0]
        REGISTER.BC -> BC
        REGISTER.DE -> DE
        REGISTER.HL -> HL
        REGISTER.PSW -> PSW
        REGISTER.PC -> PC
        REGISTER.SP -> SP
        REGISTER.THL -> THL
        else -> throw IllegalArgumentException("$register is not found")
    }

    private fun write(value: SysUnsigned, register: REGISTER) = when (register) {
        REGISTER.A -> PSW = PSW.set(capacityData * 2 - 1, capacityData, value.extend(capacityData))
        REGISTER.Flag -> {
            PSW = PSW.set(capacityData - 1, 0, value.extend(capacityData))
            flag(PSW[capacityData - 1, 0])
        }
        REGISTER.B -> BC = BC.set(capacityData * 2 - 1, capacityData, value.extend(capacityData))
        REGISTER.C -> BC = BC.set(capacityData - 1, 0, value.extend(capacityData))
        REGISTER.D -> DE = DE.set(capacityData * 2 - 1, capacityData, value.extend(capacityData))
        REGISTER.E -> DE = DE.set(capacityData - 1, 0, value.extend(capacityData))
        REGISTER.H -> HL = HL.set(capacityData * 2 - 1, capacityData, value.extend(capacityData))
        REGISTER.L -> HL = HL.set(capacityData - 1, 0, value.extend(capacityData))
        REGISTER.TH -> THL = THL.set(capacityData * 2 - 1, capacityData, value.extend(capacityData))
        REGISTER.TL -> THL = THL.set(capacityData - 1, 0, value.extend(capacityData))
        REGISTER.BC -> BC = value.extend(capacityData * 2) as SysUnsigned
        REGISTER.DE -> DE = value.extend(capacityData * 2) as SysUnsigned
        REGISTER.HL -> HL = value.extend(capacityData * 2) as SysUnsigned
        REGISTER.PSW -> PSW = value.extend(capacityData * 2) as SysUnsigned
        REGISTER.PC -> PC = value.extend(capacityData * 2) as SysUnsigned
        REGISTER.SP -> SP = value.extend(capacityData * 2) as SysUnsigned
        REGISTER.THL -> THL = value.extend(capacityData * 2) as SysUnsigned
        else -> throw IllegalArgumentException("$register is not found")
    }
}

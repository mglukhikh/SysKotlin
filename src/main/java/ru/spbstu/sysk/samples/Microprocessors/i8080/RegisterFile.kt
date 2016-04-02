package ru.spbstu.sysk.samples.microprocessors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysBit.ZERO
import ru.spbstu.sysk.data.integer.SysInteger
import java.util.*

val A = SysInteger(5, 0)
val Flag = SysInteger(5, 1)
val B = SysInteger(5, 2)
val C = SysInteger(5, 3)
val D = SysInteger(5, 4)
val E = SysInteger(5, 5)
val H = SysInteger(5, 6)
val L = SysInteger(5, 7)
val PCF = SysInteger(5, 8)
val PCS = SysInteger(5, 9)
val SPF = SysInteger(5, 10)
val SPS = SysInteger(5, 11)

val STORAGE = SysInteger(3, 0)
val PUSH = SysInteger(3, 1)
val PULL = SysInteger(3, 2)
val RESET = SysInteger(3, 3)

class RegisterFile(parent: SysModule) : SysModule("RegisterFile", parent) {

    private val PSW = ArrayList<SysBit>(16)
    private val BC = ArrayList<SysBit>(16)
    private val DE = ArrayList<SysBit>(16)
    private val HL = ArrayList<SysBit>(16)
    private val PC = ArrayList<SysBit>(16)
    private val SP = ArrayList<SysBit>(16)

    val data = bitBusPort(8, "data")
    val address = input<SysInteger>("address")
    val command = input<SysInteger>("command")

    val clk = bitInput("clk")

    init {
        for (i in 0..15) {
            PSW.add(ZERO)
            BC.add(ZERO)
            DE.add(ZERO)
            HL.add(ZERO)
            PC.add(ZERO)
            SP.add(ZERO)
        }

        stateFunction(clk, true) {
            infiniteState {
                when (command()) {
                    STORAGE -> data.disable()
                    PUSH -> push(address())
                    PULL -> pull(address())
                    RESET -> reset()
                }
            }
        }
    }

    private fun reset() {
        for (i in 0..15) {
            PSW[i] = ZERO
            BC[i] = ZERO
            DE[i] = ZERO
            HL[i] = ZERO
            PC[i] = ZERO
            SP[i] = ZERO
        }
    }

    private fun pull(address: SysInteger) {
        when (address) {
            A -> pull(PSW, true)
            Flag -> pull(PSW, false)
            B -> pull(BC, true)
            C -> pull(BC, false)
            D -> pull(DE, true)
            E -> pull(DE, false)
            H -> pull(HL, true)
            L -> pull(HL, false)
            PCF -> pull(PC, true)
            PCS -> pull(PC, false)
            SPF -> pull(SP, true)
            SPS -> pull(SP, false)
        }
    }

    private fun pull(register: ArrayList<SysBit>, first: Boolean) {
        for (i in 0..7) data(register[if (first) i else i + 8], i)
    }

    private fun push(address: SysInteger) {
        when (address) {
            A -> push(PSW, true)
            Flag -> push(PSW, false)
            B -> push(BC, true)
            C -> push(BC, false)
            D -> push(DE, true)
            E -> push(DE, false)
            H -> push(HL, true)
            L -> push(HL, false)
            PCF -> push(PC, true)
            PCS -> push(PC, false)
            SPF -> push(SP, true)
            SPS -> push(SP, false)
        }
    }

    private fun push(register: ArrayList<SysBit>, first: Boolean) {
        for (i in 0..7) register[if (first) i else i + 8] = data[i]
    }
}

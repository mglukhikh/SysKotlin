package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysUnsigned

class ControlUnit constructor(
        parent: SysModule
) : SysModule("ControlUnit", parent) {

    val CtoALU = output<COMMAND>("CtoALU")
    val ENtoATM = output<SysBit>("ENtoATM")
    val ENtoBTM = output<SysBit>("ENtoBTM")
    val RtoRF = output<REGISTER>("RtoRF")
    val CtoRF = output<COMMAND>("CtoRF")
    val ENtoRF = output<SysBit>("ENtoRF")
    val AtoCR = output<SysUnsigned>("AtoCR")
    val CtoCR = output<COMMAND>("CtoCR")
    val ENtoCR = output<SysBit>("ENtoCR")
    val CtoDG = output<COMMAND>("CtoDG")
    val ENtoDG = output<SysBit>("ENtoDG")
    val CtoAG = output<COMMAND>("CtoAG")
    val ENtoAG = output<SysBit>("ENtoAG")
    val clk = output<SysBit>("operation")

    val operation = bidirPort<SysUnsigned>("operation")

    val WR = bitInput("WR")
    val DBIN = bitInput("DBIN")
    val INTE = output<SysBit>("INTE")
    val INT = bitInput("INT")
    val HOLD_ACK = output<SysBit>("HOLD_ACK")
    val HOLD = bitInput("HOLD")
    val WAIT = output<SysBit>("WAIT")
    val READY = bitInput("READY")
    val SYNC = output<SysBit>("SYNC")
    val CLK1 = bitInput("CLK1")
    val CLK2 = bitInput("CLK2")
    val RESET = bitInput("RESET")

    init {

    }
}

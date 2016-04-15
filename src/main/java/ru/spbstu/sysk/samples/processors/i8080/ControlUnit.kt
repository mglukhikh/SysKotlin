package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit

class ControlUnit(parent: SysModule) : SysModule("ControlUnit", parent) {

    val reset = bitInput("reset")
    val clk1 = bitInput("clk1")
    val clk2 = bitInput("clk2")

    val clk = output<SysBit>("clk")
    val registerRF = output<REGISTER>("registerRF")
    val commandRF = output<COMMAND>("commandRF")
    val enRF = output<SysBit>("enRF")
    val operationALU = output<COMMAND>("operationALU")
    val commandOF = output<COMMAND>("commandOF")
    val operationOF = input<OPERATION>("operationOF")
    val enOF = output<SysBit>("enOF")
    val commandDG = output<COMMAND>("commandDG")
    val enDG = output<SysBit>("enDG")
    val commandAG = output<COMMAND>("commandAG")
    val enAG = output<SysBit>("enAG")

}

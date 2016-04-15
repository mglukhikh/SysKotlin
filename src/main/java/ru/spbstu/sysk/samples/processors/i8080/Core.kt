package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned

class Core(parent: SysModule) : SysModule("i8080", parent) {

    private val ALU = ArithmeticLogicUnit(CAPACITY.DATA, CAPACITY.COMMAND, this)
    private val RF = RegisterFile(CAPACITY.DATA, CAPACITY.ADDRESS, CAPACITY.REGISTER, CAPACITY.COMMAND, this)
    private val OF = OperationFifo(CAPACITY.DATA, CAPACITY.COMMAND, this)
    private val CU = ControlUnit(this)
    private val DG = Gateway(CAPACITY.DATA, this)
    private val AG = Gateway(CAPACITY.ADDRESS, this)

    val reset = bitInput("reset")
    val clk1 = bitInput("clk1")
    val clk2 = bitInput("clk2")

    private val dataBus = signal<SysUnsigned>("dataBus")
    private val addressBus = signal<SysUnsigned>("addressBus")
    private val clk = bitSignal("clk")

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")

    init {
        connector("connector", RF.out, ALU.A)
        connector("connector", RF.B, ALU.B)
        connector("connector", ALU.out, RF.inp)
        RF.data bind dataBus
        RF.address bind addressBus
        connector("connector", CU.registerRF, RF.register)
        connector("connector", CU.commandRF, RF.command)
        RF.clk bind clk
        connector("connector", CU.enRF, RF.en)
        connector("connector", CU.operationALU, ALU.operation)

        OF.data bind dataBus
        connector("connector", CU.commandOF, OF.command)
        connector("connector", OF.operation, CU.operationOF)
        OF.clk bind clk
        connector("connector", CU.enOF, OF.en)

        DG.back bind data
        DG.front bind dataBus
        connector("connector", CU.commandDG, DG.command)
        DG.clk bind clk
        connector("connector", CU.enDG, DG.en)

        AG.back bind address
        AG.front bind addressBus
        connector("connector", CU.commandAG, AG.command)
        AG.clk bind clk
        connector("connector", CU.enAG, AG.en)

        CU.reset bind reset
        CU.clk1 bind clk1
        CU.clk2 bind clk2
        CU.clk bind clk
    }
}

package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned

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
    val wr = bitOutput("dbin")
    val dbin = bitOutput("dbin")

    private val dataBus = signal("dataBus", unsigned(CAPACITY.DATA, 0))
    private val addressBus = signal("addressBus", unsigned(CAPACITY.ADDRESS, 0))
    private val clk = bitSignal("clk")

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")

    init {
        connector("connector", unsigned(CAPACITY.DATA, 0), RF.out, ALU.A)
        connector("connector", unsigned(CAPACITY.DATA, 0), RF.B, ALU.B)
        connector("connector", unsigned(CAPACITY.DATA, 0), ALU.out, RF.inp)
        RF.data bind dataBus
        RF.address bind addressBus
        connector("connector", CU.registerRF, RF.register)
        connector("connector", CU.commandRF, RF.command)
        RF.clk bind clk
        bitConnector("connector", CU.enRF, RF.en)
        connector("connector", CU.operationALU, ALU.operation)

        OF.data bind dataBus
        connector("connector", CU.commandOF, OF.command)
        connector("connector", OF.operation, CU.operationOF)
        OF.clk bind clk
        bitConnector("connector", CU.enOF, OF.en)

        DG.back bind data
        DG.front bind dataBus
        bitConnector("connector", CU.enDG, DG.en)

        AG.back bind address
        AG.front bind addressBus
        bitConnector("connector", CU.enAG, AG.en)

        CU.reset bind reset
        CU.clk1 bind clk1
        CU.clk2 bind clk2
        CU.clk bind clk
        CU.dbin bind dbin
        CU.wr bind wr
    }
}

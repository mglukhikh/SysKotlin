package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned
import ru.spbstu.sysk.data.SysBit.*

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
    private val dataSignal = signal("dataSignal", unsigned(CAPACITY.DATA, 0))
    private val addressSignal = signal("addressSignal", unsigned(CAPACITY.ADDRESS, 0))
    private val clkSignal1 = bitSignal("clkSignal1")
    private val clkSignal2 = bitSignal("clkSignal2")
    private val dbinSignal = bitSignal("dbinSignal", ZERO)

    val data = bidirPort<SysUnsigned>("data")
    val address = bidirPort<SysUnsigned>("address")

    init {
        connector("connector", unsigned(CAPACITY.DATA, 0), RF.out, ALU.A)
        connector("connector", unsigned(CAPACITY.DATA, 0), RF.B, ALU.B)
        connector("connector", unsigned(CAPACITY.DATA, 0), ALU.out, RF.inp)
        connector("connector", CU.registerRF, RF.register)
        connector("connector", CU.commandRF, RF.command)
        connector("connector", CU.commandOF, OF.command)
        connector("connector", OF.operation, CU.operationOF)
        connector("connector", CU.operationALU, ALU.operation)
        bitConnector("connector", ZERO, CU.enRF, RF.en)
        bitConnector("connector", ZERO, CU.enOF, OF.en)
        bitConnector("connector", ZERO, CU.enDG, DG.en)
        bitConnector("connector", ZERO, CU.enAG, AG.en)
        bitConnector("connector", ONE, CU.allowOF, OF.allow)
        bitConnector("connector", ONE, OF.empty, CU.emptyOF)
        bitConnector("connector", ZERO, OF.inc, RF.inc)
        bitConnector("connector", ZERO, OF.read, RF.read)

        RF.data bind dataBus
        RF.address bind addressBus
        RF.clk bind clkSignal2
        RF.pc bind addressSignal
        OF.data bind dataSignal
        OF.clk1 bind clkSignal1
        OF.clk2 bind clkSignal2
        OF.dbin bind dbinSignal
        DG.back bind dataSignal
        DG.front bind dataBus
        AG.back bind addressSignal
        AG.front bind addressBus
        CU.reset bind reset
        CU.clk1 bind clkSignal1
        CU.clk2 bind clkSignal2
        CU.dbin bind dbinSignal
        CU.wr bind wr

        function(data.defaultEvent) { dataSignal(data()) }
        function(address.defaultEvent) { addressSignal(address()) }
        function(dataSignal.defaultEvent) { data(dataSignal()) }
        function(addressSignal.defaultEvent) { address(addressSignal()) }
        function(clk1.defaultEvent) { clkSignal1(clk1()) }
        function(clk2.defaultEvent) { clkSignal2(clk2()) }
        function(dbinSignal.defaultEvent) { dbin(dbinSignal()) }
    }
}

package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysUnsigned

class Core constructor(parent: SysModule) : SysModule("i8080", parent) {

    private val ATM = TempMemory(CAPACITY.DATA, this)
    private val BTM = TempMemory(CAPACITY.DATA, this)
    private val ALU = ArithmeticLogicUnit(CAPACITY.DATA, CAPACITY.COMMAND, this)
    private val CU = ControlUnit(this)
    private val RF = RegisterFile(CAPACITY.DATA, CAPACITY.ADDRESS, CAPACITY.REGISTER, CAPACITY.COMMAND, this)
    private val CR = CommandRegister(CAPACITY.DATA, VALUE.MAX_ADDRESS, CAPACITY.ADDRESS, CAPACITY.COMMAND, this)
    private val DG = Gateway(CAPACITY.DATA, this)
    private val AG = Gateway(CAPACITY.ADDRESS, this)

    private val data = signal<SysUnsigned>("data")
    private val address = signal<SysUnsigned>("address")
    private val clk = bitSignal("clk")

    private val ATMtoALU = signal<SysUnsigned>("ATMtoALU")
    private val BTMtoALU = signal<SysUnsigned>("BTMtoALU")
    private val CRtoCU = signal<SysUnsigned>("CRtoCU")

    private val CtoALU = signal<COMMAND>("CtoALU")
    private val ENtoATM = bitSignal("ENtoATM")
    private val ENtoBTM = bitSignal("ENtoBTM")
    private val RtoRF = signal<REGISTER>("RtoRF")
    private val CtoRF = signal<COMMAND>("CtoRF")
    private val ENtoRF = bitSignal("ENtoRF")
    private val AtoCR = signal<SysUnsigned>("AtoCR")
    private val CtoCR = signal<COMMAND>("CtoCR")
    private val ENtoCR = bitSignal("ENtoCR")
    private val CtoDG = signal<COMMAND>("CtoDG")
    private val ENtoDG = bitSignal("ENtoDG")
    private val CtoAG = signal<COMMAND>("CtoAG")
    private val ENtoAG = bitSignal("ENtoAG")

    private val wr = signal<SysBit>("wr")
    private val dbin = signal<SysBit>("dbin")
    private val inte = signal<SysBit>("inte")
    private val int = signal<SysBit>("int")
    private val hold_ack = signal<SysBit>("hold_ack")
    private val hold = signal<SysBit>("hold")
    private val wait = signal<SysBit>("wait")
    private val ready = signal<SysBit>("ready")
    private val sync = signal<SysBit>("sync")
    private val clk1 = signal<SysBit>("clk1")
    private val clk2 = signal<SysBit>("clk2")
    private val reset = signal<SysBit>("reset")
    private val dataBack = signal<SysUnsigned>("dataBack")
    private val addressBack = signal<SysUnsigned>("addressBack")

    val WR = input<SysBit>("WR")
    val DBIN = input<SysBit>("DBIN")
    val INTE = output<SysBit>("INTE")
    val INT = input<SysBit>("INT")
    val HOLD_ACK = output<SysBit>("HOLD_ACK")
    val HOLD = input<SysBit>("HOLD")
    val WAIT = output<SysBit>("WAIT")
    val READY = input<SysBit>("READY")
    val SYNC = output<SysBit>("SYNC")
    val CLK1 = input<SysBit>("CLK1")
    val CLK2 = input<SysBit>("CLK2")
    val RESET = input<SysBit>("RESET")
    val DATA = bidirPort<SysUnsigned>("DATA")
    val ADDRESS = bidirPort<SysUnsigned>("ADDRESS")

    init {
        ATM.inp bind data
        ATM.out bind ATMtoALU
        ATM.clk bind clk
        ATM.en bind ENtoATM

        BTM.inp bind data
        BTM.out bind BTMtoALU
        BTM.clk bind clk
        BTM.en bind ENtoBTM

        ALU.A bind ATMtoALU
        ALU.B bind BTMtoALU
        ALU.out bind data
        ALU.operation bind CtoALU

        CR.inp bind data
        CR.out bind CRtoCU
        CR.address bind AtoCR
        CR.command bind CtoCR
        CR.clk bind clk
        CR.en bind ENtoCR

        RF.data bind data
        RF.address bind address
        RF.register bind RtoRF
        RF.command bind CtoRF
        RF.clk bind clk
        RF.en bind ENtoRF

        DG.front bind data
        DG.back bind dataBack
        DG.command bind CtoDG
        DG.clk bind clk
        DG.en bind ENtoDG

        AG.front bind data
        AG.back bind addressBack
        AG.command bind CtoAG
        AG.clk bind clk
        AG.en bind ENtoAG

        CU.clk bind clk
        CU.operation bind CRtoCU

        CU.CtoALU bind CtoALU
        CU.ENtoATM bind ENtoATM
        CU.ENtoBTM bind ENtoBTM
        CU.RtoRF bind RtoRF
        CU.CtoRF bind CtoRF
        CU.ENtoRF bind ENtoRF
        CU.AtoCR bind AtoCR
        CU.CtoCR bind CtoCR
        CU.ENtoCR bind ENtoCR
        CU.CtoDG bind CtoDG
        CU.ENtoDG bind ENtoDG
        CU.CtoAG bind CtoAG
        CU.ENtoAG bind ENtoAG

        CU.WR bind wr
        CU.DBIN bind dbin
        CU.INTE bind inte
        CU.INT bind int
        CU.HOLD_ACK bind hold_ack
        CU.HOLD bind hold
        CU.WAIT bind wait
        CU.READY bind ready
        CU.SYNC bind sync
        CU.CLK1 bind clk1
        CU.CLK2 bind clk2
        CU.RESET bind reset

        function(WR.defaultEvent) { wr(WR()) }
        function(DBIN.defaultEvent) { dbin(DBIN()) }
        function(CU.INTE.defaultEvent) { INTE(inte()) }
        function(INT.defaultEvent) { int(INT()) }
        function(CU.HOLD_ACK.defaultEvent) { HOLD_ACK(hold_ack()) }
        function(HOLD.defaultEvent) { hold(HOLD()) }
        function(CU.WAIT.defaultEvent) { WAIT(wait()) }
        function(READY.defaultEvent) { ready(READY()) }
        function(CU.SYNC.defaultEvent) { SYNC(sync()) }
        function(CLK1.defaultEvent) { clk1(CLK1()) }
        function(CLK2.defaultEvent) { clk2(CLK2()) }
        function(RESET.defaultEvent) { reset(RESET()) }

        function(DATA.defaultEvent) { DG.back(DATA()) }
        function(DG.back.defaultEvent) { DATA(DG.back()) }
        function(ADDRESS.defaultEvent) { AG.back(ADDRESS()) }
        function(AG.back.defaultEvent) { ADDRESS(AG.back()) }
    }
}

class TOP: SysTopModule() {

    val core = Core(this)

    @Test
    fun show() = start()
}
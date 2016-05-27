package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.StateContainer
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.samples.processors.i8080.OPERATION.*
import ru.spbstu.sysk.samples.processors.i8080.COMMAND.*

class ControlUnit(parent: SysModule) : SysModule("ControlUnit", parent) {

    val reset = bitInput("reset")
    val clk1 = bitInput("clk1")
    val clk2 = bitInput("clk2")
    val dbin = bitOutput("dbin")
    val wr = bitOutput("wr")

    val registerRF = output<REGISTER>("registerRF")
    val commandRF = output<COMMAND>("commandRF")
    val commandOF = output<COMMAND>("commandOF")
    val allowOF = bitOutput("allowOF")
    val sleepOF = bitInput("sleepOF")
    val emptyOF = bitInput("emptyOF")
    val flag = input<SysUnsigned>("flag")
    val operationALU = output<COMMAND>("operationALU")
    val outsideALU = bitOutput("outsideALU")
    val operationOF = input<OPERATION>("operationOF")
    val enOF = bitOutput("enOF")
    val enDG = bitOutput("enDG")
    val enAG = bitOutput("enAG")
    val enRF = bitOutput("enRF")
    val enALU = bitOutput("enALU")

    private fun StateContainer.get(command: COMMAND) {
        state {
            enOF(ONE)
            commandOF(command)
        }
        state {
            enOF(ZERO)
        }
    }

    private fun StateContainer.getOperation() = get(READ)

    private fun StateContainer.getArgs() {
        loop({ emptyOF.one }) { sleep(1) }
        get(READ_DATA)
    }

    private fun StateContainer.add(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, ADD, outside, ADD_A, ADD_B, ADD_C, ADD_D, ADD_E, ADD_H, ADD_L, ADD_M)

    private fun StateContainer.adc(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, ADC, outside, ADC_A, ADC_B, ADC_C, ADC_D, ADC_E, ADC_H, ADC_L, ADC_M)

    private fun StateContainer.sub(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, SUB, outside, SUB_A, SUB_B, SUB_C, SUB_D, SUB_E, SUB_H, SUB_L, SUB_M)

    private fun StateContainer.sbb(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, SBB, outside, SBB_A, SBB_B, SBB_C, SBB_D, SBB_E, SBB_H, SBB_L, SBB_M)

    private fun StateContainer.xra(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, XRA, outside, XRA_A, XRA_B, XRA_C, XRA_D, XRA_E, XRA_H, XRA_L, XRA_M)

    private fun StateContainer.ora(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, ORA, outside, ORA_A, ORA_B, ORA_C, ORA_D, ORA_E, ORA_H, ORA_L, ORA_M)

    private fun StateContainer.ana(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, ANA, outside, ANA_A, ANA_B, ANA_C, ANA_D, ANA_E, ANA_H, ANA_L, ANA_M)

    private fun StateContainer.cmp(initOperation: () -> OPERATION, outside: Boolean)
            = arithmeticOperation(initOperation, CMP, outside, CMP_A, CMP_B, CMP_C, CMP_D, CMP_E, CMP_H, CMP_L, CMP_M)

    private fun StateContainer.arithmeticOperation(initOperation: () -> OPERATION, command: COMMAND, outside: Boolean,
                                                   A: OPERATION,
                                                   B: OPERATION, C: OPERATION,
                                                   D: OPERATION, E: OPERATION,
                                                   H: OPERATION, L: OPERATION,
                                                   M: OPERATION) {
        var operation = OPERATION.UNDEFINED
        state.instant { operation = initOperation() }
        if (outside) getArgs()
        state {
            enRF(ONE)
            commandRF(SET_A)
            registerRF(REGISTER.A)
        }
        state {
            commandRF(SET_CURRENT)
            registerRF(REGISTER.A)
        }
        state.instant { commandRF(SET_B) }
        case { operation == A }.state { registerRF(REGISTER.A) }
        case { operation == B }.state { registerRF(REGISTER.B) }
        case { operation == C }.state { registerRF(REGISTER.C) }
        case { operation == D }.state { registerRF(REGISTER.D) }
        case { operation == E }.state { registerRF(REGISTER.E) }
        case { operation == H }.state { registerRF(REGISTER.H) }
        case { operation == L }.state { registerRF(REGISTER.L) }
        case { operation == M }.state { registerRF(REGISTER.HL) }
        state {
            if (outside) outsideALU(ONE)
            enALU(ONE)
            enRF(ZERO)
            operationALU(command)
        }
        state.instant {
            if (outside) outsideALU(ZERO)
            enALU(ZERO)
        }
    }

    private fun StateContainer.dad(register: REGISTER) {
        mov(register, REGISTER.THL)
        state {
            enRF(ONE)
            commandRF(SET_A)
            registerRF(REGISTER.L)
        }
        state {
            commandRF(SET_CURRENT)
            registerRF(REGISTER.L)
        }
        state {
            commandRF(SET_B)
            registerRF(REGISTER.TL)
        }
        state {
            enALU(ONE)
            operationALU(ADD)
        }
        state {
            enALU(ZERO)
            commandRF(SET_A)
            registerRF(REGISTER.H)
        }
        state {
            commandRF(SET_CURRENT)
            registerRF(REGISTER.H)
        }
        state {
            commandRF(SET_B)
            registerRF(REGISTER.TH)
        }
        state {
            enALU(ONE)
            operationALU(ADC)
        }
        state.instant {
            enALU(ZERO)
            enRF(ZERO)
        }
    }

    private fun StateContainer.mov(initOperation: () -> OPERATION) {
        var operation = OPERATION.UNDEFINED
        state.instant {
            operation = initOperation()
            enRF(ONE)
            commandRF(READ_ADDRESS)
        }
        case { operation.id in MOV_A_B.id..MOV_A_A.id }.state { registerRF(REGISTER.A) }
        case { operation.id in MOV_B_B.id..MOV_B_A.id }.state { registerRF(REGISTER.B) }
        case { operation.id in MOV_C_B.id..MOV_C_A.id }.state { registerRF(REGISTER.C) }
        case { operation.id in MOV_D_B.id..MOV_D_A.id }.state { registerRF(REGISTER.D) }
        case { operation.id in MOV_E_B.id..MOV_E_A.id }.state { registerRF(REGISTER.E) }
        case { operation.id in MOV_H_B.id..MOV_H_A.id }.state { registerRF(REGISTER.H) }
        case { operation.id in MOV_L_B.id..MOV_L_A.id }.state { registerRF(REGISTER.L) }
        case { operation.id in MOV_M_B.id..MOV_M_A.id }.state { registerRF(REGISTER.HL) }
        state.instant { commandRF(WRITE_ADDRESS) }
        case { operation()[2, 0] == MOV_A_A()[2, 0] }.state { registerRF(REGISTER.A) }
        case { operation()[2, 0] == MOV_A_B()[2, 0] }.state { registerRF(REGISTER.B) }
        case { operation()[2, 0] == MOV_A_C()[2, 0] }.state { registerRF(REGISTER.C) }
        case { operation()[2, 0] == MOV_A_D()[2, 0] }.state { registerRF(REGISTER.D) }
        case { operation()[2, 0] == MOV_A_E()[2, 0] }.state { registerRF(REGISTER.E) }
        case { operation()[2, 0] == MOV_A_H()[2, 0] }.state { registerRF(REGISTER.H) }
        case { operation()[2, 0] == MOV_A_L()[2, 0] }.state { registerRF(REGISTER.L) }
        case { operation()[2, 0] == MOV_A_M()[2, 0] }.state { registerRF(REGISTER.HL) }
        state.instant { enRF(ZERO) }
    }

    private fun StateContainer.mvi(register: REGISTER) {
        getArgs()
        state {
            enRF(ONE)
            commandRF(WRITE_DATA)
            registerRF(register)
        }
        state.instant { enRF(ZERO) }
    }

    private fun StateContainer.wait(number: Int) {
        loop(1..number) {
            state { wait = true }
            loop ({ wait }) { sleep(1) }
        }
    }

    private fun StateContainer.sta(data: REGISTER, address: REGISTER? = null) {
        state.instant {
            enDG(ONE)
            enAG(ONE)
        }
        if (address == null) {
            mvi(REGISTER.TL)
            mvi(REGISTER.TH)
        }
        state {
            enRF(ONE)
            commandRF(READ_ADDRESS)
            registerRF(address ?: REGISTER.THL)
        }
        state {
            commandRF(READ_DATA)
            registerRF(data)
        }
        state {
            enRF(ZERO)
            allowOF(ZERO)
        }
        loop ({ sleepOF.zero }) { sleep(1) }
        state.instant { wr(ONE) }
        wait(1)
        state.instant { wr(ZERO) }
        wait(1)
        state {
            enDG(ZERO)
            enAG(ZERO)
            wait = true
            allowOF(ONE)
        }
    }

    private fun StateContainer.lda(data: REGISTER, address: REGISTER? = null) {
        state.instant {
            enDG(ONE)
            enAG(ONE)
        }
        if (address == null) {
            mvi(REGISTER.TL)
            mvi(REGISTER.TH)
        }
        state {
            enRF(ONE)
            commandRF(READ_ADDRESS)
            registerRF(address ?: REGISTER.THL)
        }
        state {
            enRF(ZERO)
            allowOF(ZERO)
        }
        loop ({ sleepOF.zero }) { sleep(1) }
        state.instant { dbin(ONE) }
        wait(1)
        state.instant { dbin(ZERO) }
        wait(1)
        state {
            enDG(ZERO)
            enAG(ZERO)
            enRF(ONE)
            commandRF(WRITE_DATA)
            registerRF(data)
        }
        state {
            enRF(ZERO)
            wait = true
            allowOF(ONE)
        }
    }

    private fun StateContainer.inc(register: REGISTER) {
        state {
            enRF(ONE)
            commandRF(INC)
            registerRF(register)
        }
        state.instant { enRF(ZERO) }
    }

    private fun StateContainer.dcr(register: REGISTER) {
        state {
            enRF(ONE)
            commandRF(DEC)
            registerRF(register)
        }
        state.instant { enRF(ZERO) }
    }

    private fun StateContainer.shift(right: Boolean, cycle: Boolean) {
        state {
            enRF(ONE)
            when {
                right && cycle -> commandRF(USR_A)
                right && !cycle -> commandRF(SHR_A)
                !right && cycle -> commandRF(USL_A)
                !right && !cycle -> commandRF(SHL_A)
            }
        }
        state.instant { enRF(ZERO) }
    }

    private fun StateContainer.mov(inp: REGISTER, out: REGISTER) {
        state {
            enRF(ONE)
            commandRF(READ_ADDRESS)
            registerRF(inp)
        }
        state {
            commandRF(WRITE_ADDRESS)
            registerRF(out)
        }
        state.instant { enRF(ZERO) }
    }

    private fun StateContainer.swap(a: REGISTER, b: REGISTER) {
        mov(a, REGISTER.THL)
        mov(b, a)
        mov(REGISTER.THL, b)
    }

    private fun StateContainer.jump() {
        mvi(REGISTER.TL)
        mvi(REGISTER.TH)
        state {
            enRF(ONE)
            commandRF(READ_ADDRESS)
            registerRF(REGISTER.THL)
        }
        state.instant { enRF(ZERO) }
        resetOF()
        mov(REGISTER.THL, REGISTER.PC)
        state {
            allowOF(ONE)
        }
    }

    private fun StateContainer.resetOF() {
        state {
            allowOF(ZERO)
        }
        loop ({ sleepOF.zero }) { sleep(1) }
        state {
            enOF(ONE)
            commandOF(RESET)
        }
        state.instant { enOF(ZERO) }
    }

    private fun StateContainer.resetRF() {
        state {
            enRF(ONE)
            commandRF(RESET)
        }
        state.instant { enRF(ZERO) }
    }

    private fun StateContainer.reset() {
        resetOF()
        resetRF()
        state(init)
    }

    private val init: () -> Unit = {
        println("init")
        enOF(ZERO)
        enDG(ZERO)
        enAG(ZERO)
        enRF(ZERO)
        allowOF(ONE)
        wr(ZERO)
        dbin(ZERO)
        outsideALU(ZERO)
        prepareReset = false
        wait = false
    }

    private var wait = false

    private var prepareReset = false

    init {
        function(clk1.posEdgeEvent) { wait = false }
        function(reset.posEdgeEvent) { prepareReset = true }
        var operation = OPERATION.UNDEFINED
        stateFunction(clk2) {
            init(init)
            infinite.block {
                case ({ prepareReset }) { reset() }
                case ({ !wait && emptyOF.zero }) {
                    state.instant { wait = true }
                    getOperation()
                    state.instant { operation = operationOF() }
                    case ({ operation == NOP }) { state.println { "NOP" } }
                    case ({ operation.id in ADD_B.id..ADD_A.id }) { add({ operation }, false) }
                    case ({ operation == ADI_d8 }) { add({ ADD_A }, true) }
                    case ({ operation.id in ADC_B.id..ADC_A.id }) { adc({ operation }, false) }
                    case ({ operation == ACI_d8 }) { adc({ ADC_A }, true) }
                    case ({ operation.id in SUB_B.id..SUB_A.id }) { sub({ operation }, false) }
                    case ({ operation == SUI_d8 }) { sub({ SUB_A }, true) }
                    case ({ operation.id in SBB_B.id..SBB_A.id }) { sbb({ operation }, false) }
                    case ({ operation == SBI_d8 }) { sbb({ SBB_A }, true) }
                    case ({ operation.id in XRA_B.id..XRA_A.id }) { xra({ operation }, false) }
                    case ({ operation == XRI_d8 }) { xra({ XRA_A }, true) }
                    case ({ operation.id in ORA_B.id..ORA_A.id }) { ora({ operation }, false) }
                    case ({ operation == ORI_d8 }) { ora({ ORA_A }, true) }
                    case ({ operation.id in ANA_B.id..ANA_A.id }) { ana({ operation }, false) }
                    case ({ operation == ANI_d8 }) { ana({ ANA_A }, true) }
                    case ({ operation.id in CMP_B.id..CMP_A.id }) { cmp({ operation }, false) }
                    case ({ operation == CPI_d8 }) { cmp({ CMP_A }, true) }
                    case ({ operation == DAD_B }) { dad(REGISTER.BC) }
                    case ({ operation == DAD_D }) { dad(REGISTER.DE) }
                    case ({ operation == DAD_H }) { dad(REGISTER.HL) }
                    case ({ operation == DAD_SP }) { dad(REGISTER.SP) }
                    case ({ operation.id in MOV_B_B.id..MOV_A_A.id }) { mov({ operation }) }
                    case ({ operation == MVI_A_d8 }) { mvi(REGISTER.A) }
                    case ({ operation == MVI_B_d8 }) { mvi(REGISTER.B) }
                    case ({ operation == MVI_C_d8 }) { mvi(REGISTER.C) }
                    case ({ operation == MVI_D_d8 }) { mvi(REGISTER.D) }
                    case ({ operation == MVI_E_d8 }) { mvi(REGISTER.E) }
                    case ({ operation == MVI_H_d8 }) { mvi(REGISTER.H) }
                    case ({ operation == MVI_L_d8 }) { mvi(REGISTER.L) }
                    case ({ operation == MVI_M_d8 }) { mvi(REGISTER.HL) }
                    case ({ operation == SPHL }) { mov(REGISTER.HL, REGISTER.SP) }
                    case ({ operation == STA_a16 }) { sta(REGISTER.A) }
                    case ({ operation == STAX_B }) { sta(REGISTER.A, REGISTER.BC) }
                    case ({ operation == STAX_D }) { sta(REGISTER.A, REGISTER.DE) }
                    case ({ operation == SHLD_a16 }) { sta(REGISTER.HL) }
                    case ({ operation == LDA_a16 }) { lda(REGISTER.A) }
                    case ({ operation == LDAX_B }) { lda(REGISTER.A, REGISTER.BC) }
                    case ({ operation == LDAX_D }) { lda(REGISTER.A, REGISTER.DE) }
                    case ({ operation == LHLD_a16 }) { lda(REGISTER.HL) }
                    case ({ operation == LXI_B_d16 }) {
                        mvi(REGISTER.C)
                        mvi(REGISTER.B)
                    }
                    case ({ operation == LXI_H_d16 }) {
                        mvi(REGISTER.L)
                        mvi(REGISTER.H)
                    }
                    case ({ operation == LXI_SP_d16 }) {
                        mvi(REGISTER.TL)
                        mvi(REGISTER.TH)
                        mov(REGISTER.THL, REGISTER.SP)
                    }
                    case ({ operation == INR_A }) { inc(REGISTER.A) }
                    case ({ operation == INR_B }) { inc(REGISTER.B) }
                    case ({ operation == INR_C }) { inc(REGISTER.C) }
                    case ({ operation == INR_D }) { inc(REGISTER.D) }
                    case ({ operation == INR_E }) { inc(REGISTER.E) }
                    case ({ operation == INR_H }) { inc(REGISTER.H) }
                    case ({ operation == INR_L }) { inc(REGISTER.L) }
                    case ({ operation == INX_B }) { inc(REGISTER.BC) }
                    case ({ operation == INX_D }) { inc(REGISTER.DE) }
                    case ({ operation == INX_H }) { inc(REGISTER.HL) }
                    case ({ operation == INX_SP }) { inc(REGISTER.SP) }
                    case ({ operation == DCR_A }) { dcr(REGISTER.A) }
                    case ({ operation == DCR_B }) { dcr(REGISTER.B) }
                    case ({ operation == DCR_C }) { dcr(REGISTER.C) }
                    case ({ operation == DCR_D }) { dcr(REGISTER.D) }
                    case ({ operation == DCR_E }) { dcr(REGISTER.E) }
                    case ({ operation == DCR_H }) { dcr(REGISTER.H) }
                    case ({ operation == DCR_L }) { dcr(REGISTER.L) }
                    case ({ operation == DCX_B }) { dcr(REGISTER.BC) }
                    case ({ operation == DCX_D }) { dcr(REGISTER.DE) }
                    case ({ operation == DCX_H }) { dcr(REGISTER.HL) }
                    case ({ operation == DCX_SP }) { dcr(REGISTER.SP) }
                    case ({ operation == RAL }) { shift(false, true) }
                    case ({ operation == RAR }) { shift(true, false) }
                    case ({ operation == RLC }) { shift(false, false) }
                    case ({ operation == RRC }) { shift(true, false) }
                    case ({ operation == XCHG }) { swap(REGISTER.DE, REGISTER.HL) }
                    case ({ operation == JMP_a16 }) { jump() }
                    //  BUG: IN case function. Upgrade to caseOf.of...of.otherwise fixed this bug
                    case ({ operation == JZ_a16 }) {
                        case({ flag()[6] == ONE }) { jump() }
                        nop()
                    }
                    case ({ operation == JNZ_a16 }) {
                        case({ flag()[6] == ZERO }) { jump() }
                        nop()
                    }
                    case ({ operation == JP_a16 }) {
                        case({ flag()[7] == ONE }) { jump() }
                        nop()
                    }
                    case ({ operation == JM_a16 }) {
                        case({ flag()[7] == ZERO }) { jump() }
                        nop()
                    }
                    case ({ operation == JC_a16 }) {
                        case({ flag()[0] == ONE }) { jump() }
                        nop()
                    }
                    case ({ operation == JNC_a16 }) {
                        case({ flag()[0] == ZERO }) { jump() }
                        nop()
                    }
                    case ({ operation == JPE_a16 }) {
                        case({ flag()[2] == ONE }) { jump() }
                        nop()
                    }
                    case ({ operation == JPO_a16 }) {
                        case({ flag()[2] == ZERO }) { jump() }
                        nop()
                    }
                    otherwise { state.println { "Skipped operation $operation" } }
                    sleep(1)
                }
                otherwise { sleep(1) }
            }
        }
    }
}

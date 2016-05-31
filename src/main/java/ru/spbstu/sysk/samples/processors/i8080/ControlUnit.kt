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
        case.of { operation == A }.state {
            registerRF(REGISTER.A)
        }.of { operation == B }.state {
            registerRF(REGISTER.B)
        }.of { operation == C }.state {
            registerRF(REGISTER.C)
        }.of { operation == D }.state {
            registerRF(REGISTER.D)
        }.of { operation == E }.state {
            registerRF(REGISTER.E)
        }.of { operation == H }.state {
            registerRF(REGISTER.H)
        }.of { operation == L }.state {
            registerRF(REGISTER.L)
        }.of { operation == M }.state {
            registerRF(REGISTER.HL)
        }
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
        caseOf { operation.id }.ofIn(MOV_A_B.id..MOV_A_A.id).state {
            registerRF(REGISTER.A)
        }.ofIn(MOV_B_B.id..MOV_B_A.id).state {
            registerRF(REGISTER.B)
        }.ofIn(MOV_C_B.id..MOV_C_A.id).state {
            registerRF(REGISTER.C)
        }.ofIn(MOV_D_B.id..MOV_D_A.id).state {
            registerRF(REGISTER.D)
        }.ofIn(MOV_E_B.id..MOV_E_A.id).state {
            registerRF(REGISTER.E)
        }.ofIn(MOV_H_B.id..MOV_H_A.id).state {
            registerRF(REGISTER.H)
        }.ofIn(MOV_L_B.id..MOV_L_A.id).state {
            registerRF(REGISTER.L)
        }.ofIn(MOV_M_B.id..MOV_M_A.id).state {
            registerRF(REGISTER.HL)
        }
        state.instant { commandRF(WRITE_ADDRESS) }
        case.of { operation()[2, 0] == MOV_A_A()[2, 0] }.state {
            registerRF(REGISTER.A)
        }.of { operation()[2, 0] == MOV_A_B()[2, 0] }.state {
            registerRF(REGISTER.B)
        }.of { operation()[2, 0] == MOV_A_C()[2, 0] }.state {
            registerRF(REGISTER.C)
        }.of { operation()[2, 0] == MOV_A_D()[2, 0] }.state {
            registerRF(REGISTER.D)
        }.of { operation()[2, 0] == MOV_A_E()[2, 0] }.state {
            registerRF(REGISTER.E)
        }.of { operation()[2, 0] == MOV_A_H()[2, 0] }.state {
            registerRF(REGISTER.H)
        }.of { operation()[2, 0] == MOV_A_L()[2, 0] }.state {
            registerRF(REGISTER.L)
        }.of { operation()[2, 0] == MOV_A_M()[2, 0] }.state {
            registerRF(REGISTER.HL)
        }
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
                case.of ({ prepareReset }) {
                    reset()
                }.of ({ !wait && emptyOF.zero }) {
                    state.instant { wait = true }
                    getOperation()
                    state.instant { operation = operationOF() }
                    caseOf { operation.id }.of(NOP.id) {
                        state.println { "NOP" }
                    }.ofIn(ADD_B.id..ADD_A.id) {
                        add({ operation }, false)
                    }.of(ADI_d8.id) {
                        add({ ADD_A }, true)
                    }.ofIn (ADC_B.id..ADC_A.id) {
                        adc({ operation }, false)
                    }.of(ACI_d8.id) {
                        adc({ ADC_A }, true)
                    }.ofIn (SUB_B.id..SUB_A.id) {
                        sub({ operation }, false)
                    }.of(SUI_d8.id) {
                        sub({ SUB_A }, true)
                    }.ofIn (SBB_B.id..SBB_A.id) {
                        sbb({ operation }, false)
                    }.of(SBI_d8.id) {
                        sbb({ SBB_A }, true)
                    }.ofIn (XRA_B.id..XRA_A.id) {
                        xra({ operation }, false)
                    }.of(XRI_d8.id) {
                        xra({ XRA_A }, true)
                    }.ofIn (ORA_B.id..ORA_A.id) {
                        ora({ operation }, false)
                    }.of(ORI_d8.id) {
                        ora({ ORA_A }, true)
                    }.ofIn (ANA_B.id..ANA_A.id) {
                        ana({ operation }, false)
                    }.of(ANI_d8.id) {
                        ana({ ANA_A }, true)
                    }.ofIn (CMP_B.id..CMP_A.id) {
                        cmp({ operation }, false)
                    }.of(CPI_d8.id) {
                        cmp({ CMP_A }, true)
                    }.of(DAD_B.id) {
                        dad(REGISTER.BC)
                    }.of(DAD_D.id) {
                        dad(REGISTER.DE)
                    }.of(DAD_H.id) {
                        dad(REGISTER.HL)
                    }.of(DAD_SP.id) {
                        dad(REGISTER.SP)
                    }.ofIn (MOV_B_B.id..MOV_A_A.id) {
                        mov({ operation })
                    }.of(MVI_A_d8.id) {
                        mvi(REGISTER.A)
                    }.of(MVI_B_d8.id) {
                        mvi(REGISTER.B)
                    }.of(MVI_C_d8.id) {
                        mvi(REGISTER.C)
                    }.of(MVI_D_d8.id) {
                        mvi(REGISTER.D)
                    }.of(MVI_E_d8.id) {
                        mvi(REGISTER.E)
                    }.of(MVI_H_d8.id) {
                        mvi(REGISTER.H)
                    }.of(MVI_L_d8.id) {
                        mvi(REGISTER.L)
                    }.of(MVI_M_d8.id) {
                        mvi(REGISTER.HL)
                    }.of(SPHL.id) {
                        mov(REGISTER.HL, REGISTER.SP)
                    }.of(STA_a16.id) {
                        sta(REGISTER.A)
                    }.of(STAX_B.id) {
                        sta(REGISTER.A, REGISTER.BC)
                    }.of(STAX_D.id) {
                        sta(REGISTER.A, REGISTER.DE)
                    }.of(SHLD_a16.id) {
                        sta(REGISTER.HL)
                    }.of(LDA_a16.id) {
                        lda(REGISTER.A)
                    }.of(LDAX_B.id) {
                        lda(REGISTER.A, REGISTER.BC)
                    }.of(LDAX_D.id) {
                        lda(REGISTER.A, REGISTER.DE)
                    }.of(LHLD_a16.id) {
                        lda(REGISTER.HL)
                    }.of(LXI_B_d16.id) {
                        mvi(REGISTER.C)
                        mvi(REGISTER.B)
                    }.of(LXI_H_d16.id) {
                        mvi(REGISTER.L)
                        mvi(REGISTER.H)
                    }.of(LXI_SP_d16.id) {
                        mvi(REGISTER.TL)
                        mvi(REGISTER.TH)
                        mov(REGISTER.THL, REGISTER.SP)
                    }.of(INR_A.id) {
                        inc(REGISTER.A)
                    }.of(INR_B.id) {
                        inc(REGISTER.B)
                    }.of(INR_C.id) {
                        inc(REGISTER.C)
                    }.of(INR_D.id) {
                        inc(REGISTER.D)
                    }.of(INR_E.id) {
                        inc(REGISTER.E)
                    }.of(INR_H.id) {
                        inc(REGISTER.H)
                    }.of(INR_L.id) {
                        inc(REGISTER.L)
                    }.of(INX_B.id) {
                        inc(REGISTER.BC)
                    }.of(INX_D.id) {
                        inc(REGISTER.DE)
                    }.of(INX_H.id) {
                        inc(REGISTER.HL)
                    }.of(INX_SP.id) {
                        inc(REGISTER.SP)
                    }.of(DCR_A.id) {
                        dcr(REGISTER.A)
                    }.of(DCR_B.id) {
                        dcr(REGISTER.B)
                    }.of(DCR_C.id) {
                        dcr(REGISTER.C)
                    }.of(DCR_D.id) {
                        dcr(REGISTER.D)
                    }.of(DCR_E.id) {
                        dcr(REGISTER.E)
                    }.of(DCR_H.id) {
                        dcr(REGISTER.H)
                    }.of(DCR_L.id) {
                        dcr(REGISTER.L)
                    }.of(DCX_B.id) {
                        dcr(REGISTER.BC)
                    }.of(DCX_D.id) {
                        dcr(REGISTER.DE)
                    }.of(DCX_H.id) {
                        dcr(REGISTER.HL)
                    }.of(DCX_SP.id) {
                        dcr(REGISTER.SP)
                    }.of(RAL.id) {
                        shift(false, true)
                    }.of(RAR.id) {
                        shift(true, false)
                    }.of(RLC.id) {
                        shift(false, false)
                    }.of(RRC.id) {
                        shift(true, false)
                    }.of(XCHG.id) {
                        swap(REGISTER.DE, REGISTER.HL)
                    }.of(JMP_a16.id) {
                        jump()
                    }.of(JZ_a16.id) {
                        //  BUG: IN case function. Upgrade to caseOf.of...of.otherwise fixed this bug
                        case.of({ flag()[6] == ONE }) { jump() }
                        nop()
                    }.of(JNZ_a16.id) {
                        case.of({ flag()[6] == ZERO }) { jump() }
                        nop()
                    }.of(JP_a16.id) {
                        case.of({ flag()[7] == ONE }) { jump() }
                        nop()
                    }.of(JM_a16.id) {
                        case.of({ flag()[7] == ZERO }) { jump() }
                        nop()
                    }.of(JC_a16.id) {
                        case.of({ flag()[0] == ONE }) { jump() }
                        nop()
                    }.of(JNC_a16.id) {
                        case.of({ flag()[0] == ZERO }) { jump() }
                        nop()
                    }.of(JPE_a16.id) {
                        case.of({ flag()[2] == ONE }) { jump() }
                        nop()
                    }.of(JPO_a16.id) {
                        case.of({ flag()[2] == ZERO }) { jump() }
                        nop()
                    }.otherwise {
                        state.println { "Skipped operation $operation" }
                    }
                    sleep(1)
                }.otherwise {
                    sleep(1)
                }
            }
        }
    }
}

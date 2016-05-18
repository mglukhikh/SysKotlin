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
        state.instance {
            operation = initOperation()
            println(operation)
        }
        if (outside) getArgs()
        state {
            enRF(ONE)
            commandRF(SET_A)
            registerRF(REGISTER.A)
        }
        state.instance { commandRF(SET_B) }
        case { operation == A }.state { registerRF(REGISTER.A) }
        case { operation == B }.state { registerRF(REGISTER.B) }
        case { operation == C }.state { registerRF(REGISTER.C) }
        case { operation == D }.state { registerRF(REGISTER.D) }
        case { operation == E }.state { registerRF(REGISTER.E) }
        case { operation == H }.state { registerRF(REGISTER.H) }
        case { operation == L }.state { registerRF(REGISTER.L) }
        case { operation == M }.state { registerRF(REGISTER.HL) }
        if (outside) state.instance { outsideALU(ONE) }
        state {
            enALU(ONE)
            enRF(ZERO)
            operationALU(command)
        }
        if (outside) state.instance { outsideALU(ZERO) }
        state {
            enALU(ZERO)
        }
    }

    private fun StateContainer.mov(initOperation: () -> OPERATION) {
        var operation = OPERATION.UNDEFINED
        state.instance {
            operation = initOperation()
            println(operation)
        }
        state.instance {
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
        state.instance { commandRF(WRITE_ADDRESS) }
        case { operation()[2, 0] == MOV_A_A()[2, 0] }.state { registerRF(REGISTER.A) }
        case { operation()[2, 0] == MOV_A_B()[2, 0] }.state { registerRF(REGISTER.B) }
        case { operation()[2, 0] == MOV_A_C()[2, 0] }.state { registerRF(REGISTER.C) }
        case { operation()[2, 0] == MOV_A_D()[2, 0] }.state { registerRF(REGISTER.D) }
        case { operation()[2, 0] == MOV_A_E()[2, 0] }.state { registerRF(REGISTER.E) }
        case { operation()[2, 0] == MOV_A_H()[2, 0] }.state { registerRF(REGISTER.H) }
        case { operation()[2, 0] == MOV_A_L()[2, 0] }.state { registerRF(REGISTER.L) }
        case { operation()[2, 0] == MOV_A_M()[2, 0] }.state { registerRF(REGISTER.HL) }
        state.instance { enRF(ZERO) }
    }

    private fun StateContainer.mvi(register: REGISTER) {
        state.instance { println("MVI_${register}_d8") }
        getArgs()
        state {
            enRF(ONE)
            commandRF(WRITE_DATA)
            registerRF(register)
        }
        state.instance { enRF(ZERO) }
    }

    private fun StateContainer.wait(number: Int) {
        loop(1..number) {
            state { wait = true }
            loop ({ wait }) { sleep(1) }
        }
    }

    private fun StateContainer.sta(data: REGISTER, address: REGISTER? = null) {
        state.instance { println("STA") }
        state.instance {
            enRF(ONE)
            enDG(ONE)
            enAG(ONE)
        }
        if (address != null) {
            state {
                commandRF(READ_ADDRESS)
                registerRF(address)
            }
        } else {
            state.instance { enRF(ZERO) }
            mvi(REGISTER.TH)
            mvi(REGISTER.TL)
            state {
                enRF(ONE)
                commandRF(READ_ADDRESS)
                registerRF(REGISTER.THL)
            }
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
        state {
            enDG(ONE)
            enAG(ONE)
        }
        state.instance { wr(ONE) }
        wait(1)
        state.instance { wr(ZERO) }
        wait(1)
        state {
            enDG(ZERO)
            enAG(ZERO)
            wait = true
            allowOF(ONE)
        }
    }

    private fun StateContainer.lda(data: REGISTER, address: REGISTER? = null) {
        state.instance { println("LDA") }
        state.instance {
            enRF(ONE)
            enDG(ONE)
            enAG(ONE)
        }
        if (address != null) {
            state {
                commandRF(READ_ADDRESS)
                registerRF(address)
            }
        } else {
            state.instance { enRF(ZERO) }
            mvi(REGISTER.TH)
            mvi(REGISTER.TL)
            state {
                enRF(ONE)
                commandRF(READ_ADDRESS)
                registerRF(REGISTER.THL)
            }
        }
        state {
            enRF(ZERO)
            allowOF(ZERO)
        }
        loop ({ sleepOF.zero }) { sleep(1) }
        state.instance { dbin(ONE) }
        wait(1)
        state.instance { dbin(ZERO) }
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

    private fun StateContainer.sphl() {
        state.instance { println("SPHL") }
        state {
            enRF(ONE)
            commandRF(READ_ADDRESS)
            registerRF(REGISTER.HL)
        }
        state {
            commandRF(WRITE_ADDRESS)
            registerRF(REGISTER.SP)
        }
        state.instance { enRF(ZERO) }
    }

    private fun StateContainer.jmp(cond: () -> Boolean) {

    }

    private fun StateContainer.rtn(cond: () -> Boolean) {

    }

    private var wait = false

    init {
        function(clk1.posEdgeEvent) { wait = false }
        var operation = OPERATION.UNDEFINED
        stateFunction(clk2) {
            init {
                enOF(ZERO)
                enDG(ZERO)
                enAG(ZERO)
                enRF(ZERO)
                allowOF(ONE)
                wr(ZERO)
                dbin(ZERO)
                outsideALU(ZERO)
            }
            infinite.block {
                case ({ !wait && emptyOF.zero }) {
                    state.instance { wait = true }
                    getOperation()
                    state.instance { operation = operationOF() }
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
                    case ({ operation.id in MOV_B_B.id..MOV_A_A.id }) { mov({ operation }) }
                    case ({ operation == MVI_A_d8 }) { mvi(REGISTER.A) }
                    case ({ operation == MVI_B_d8 }) { mvi(REGISTER.B) }
                    case ({ operation == MVI_C_d8 }) { mvi(REGISTER.C) }
                    case ({ operation == MVI_D_d8 }) { mvi(REGISTER.D) }
                    case ({ operation == MVI_E_d8 }) { mvi(REGISTER.E) }
                    case ({ operation == MVI_H_d8 }) { mvi(REGISTER.H) }
                    case ({ operation == MVI_L_d8 }) { mvi(REGISTER.L) }
                    case ({ operation == MVI_M_d8 }) { mvi(REGISTER.HL) }
                    case ({ operation == SPHL }) { sphl() }
                    case ({ operation == STA_a16 }) { sta(REGISTER.A) }
                    case ({ operation == STAX_B }) { sta(REGISTER.A, REGISTER.BC) }
                    case ({ operation == STAX_D }) { sta(REGISTER.A, REGISTER.DE) }
                    case ({ operation == SHLD_a16 }) { sta(REGISTER.HL) }
                    case ({ operation == LDA_a16 }) { lda(REGISTER.A) }
                    case ({ operation == LDAX_B }) { lda(REGISTER.A, REGISTER.BC) }
                    case ({ operation == LDAX_D }) { lda(REGISTER.A, REGISTER.DE) }
                    case ({ operation == LHLD_a16 }) { lda(REGISTER.HL) }
                    case ({ operation == LXI_B_d16 }) {
                        mvi(REGISTER.B)
                        mvi(REGISTER.C)
                    }
                    case ({ operation == LXI_H_d16 }) {
                        mvi(REGISTER.H)
                        mvi(REGISTER.L)
                    }
                    case ({ operation == LXI_SP_d16 }) {
                        mvi(REGISTER.TH)
                        mvi(REGISTER.TL)
                        state {
                            enRF(ONE)
                            commandRF(READ_ADDRESS)
                            registerRF(REGISTER.THL)
                        }
                        state {
                            commandRF(WRITE_ADDRESS)
                            registerRF(REGISTER.SP)
                        }
                        state.instance { enRF(ZERO) }
                    }
                    otherwise { state.println { "Skipped operation $operation" } }
                    sleep(1)
                }
                otherwise { sleep(1) }
            }
        }
    }
}

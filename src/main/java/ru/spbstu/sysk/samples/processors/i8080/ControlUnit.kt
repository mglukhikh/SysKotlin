package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.StateContainer
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit.*
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
    val emptyOF = bitInput("emptyOF")
    val operationALU = output<COMMAND>("operationALU")
    val operationOF = input<OPERATION>("operationOF")
    val enOF = bitOutput("enOF")
    val enDG = bitOutput("enDG")
    val enAG = bitOutput("enAG")
    val enRF = bitOutput("enRF")


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

    private fun StateContainer.getArgs() = get(READ_DATA)

    private fun StateContainer.add(initOperation: () -> OPERATION, transfer: Boolean) {
        var operation = OPERATION.UNDEFINED
        state.instance {
            operation = initOperation()
            println("ADD: ${initOperation()}")
        }
        state {
            enRF(ONE)
            commandRF(SET_A)
            registerRF(REGISTER.A)
        }
        state.instance { commandRF(SET_B) }
        case { operation == ADD_A }.state { registerRF(REGISTER.A) }
        case { operation == ADD_B }.state { registerRF(REGISTER.B) }
        case { operation == ADD_C }.state { registerRF(REGISTER.C) }
        case { operation == ADD_D }.state { registerRF(REGISTER.D) }
        case { operation == ADD_E }.state { registerRF(REGISTER.E) }
        case { operation == ADD_H }.state { registerRF(REGISTER.H) }
        case { operation == ADD_L }.state { registerRF(REGISTER.L) }
        case { operation == ADD_M }.state { registerRF(REGISTER.HL) }
        state {
            enRF(ZERO)
            operationALU(if (transfer) ADC else ADD)
        }
    }

    private fun StateContainer.sub(initOperation: () -> OPERATION, transfer: Boolean) {
        var operation = OPERATION.UNDEFINED
        state.instance {
            operation = initOperation()
            println("SUB: ${initOperation()}")
        }
        state {
            enRF(ONE)
            commandRF(SET_A)
            registerRF(REGISTER.A)
        }
        state.instance { commandRF(SET_B) }
        case { operation == SUB_A }.state { registerRF(REGISTER.A) }
        case { operation == SUB_B }.state { registerRF(REGISTER.B) }
        case { operation == SUB_C }.state { registerRF(REGISTER.C) }
        case { operation == SUB_D }.state { registerRF(REGISTER.D) }
        case { operation == SUB_E }.state { registerRF(REGISTER.E) }
        case { operation == SUB_H }.state { registerRF(REGISTER.H) }
        case { operation == SUB_L }.state { registerRF(REGISTER.L) }
        case { operation == SUB_M }.state { registerRF(REGISTER.HL) }
        state {
            enRF(ZERO)
            operationALU(if (transfer) SBB else SUB)
        }
    }

    private fun StateContainer.xra(operation: () -> OPERATION) {
        state.println { "XRA: ${operation()}" }
    }

    private fun StateContainer.ora(operation: () -> OPERATION) {
        state.println { "ORA: ${operation()}" }
    }

    private fun StateContainer.ana(operation: () -> OPERATION) {
        state.println { "ANA: ${operation()}" }
    }

    private fun StateContainer.cmp(operation: () -> OPERATION) {
        state.println { "CMP: ${operation()}" }
    }

    private fun StateContainer.mov(initOperation: () -> OPERATION) {
        var operation = OPERATION.UNDEFINED
        state.instance {
            operation = initOperation()
            println("MOV: ${initOperation()}")
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

    private var mviId = 0L
    private fun StateContainer.mvi(operation: () -> OPERATION, register: () -> REGISTER) {
        state.instance { println("MVI: ${operation()}") }
        label("start mvi$mviId")
        sleep(1)
        case ({ emptyOF.zero }) { getArgs() }
        otherwise { jump("start mvi${mviId++}") }
        state {
            enRF(ONE)
            commandRF(WRITE_DATA)
            registerRF(register())
        }
        state.instance { enRF(ZERO) }
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
            }
            infinite.block {
                case ({ !wait && emptyOF.zero }) {
                    state.instance { wait = true }
                    getOperation()
                    state.instance { operation = operationOF() }
                    case ({ operation.id in ADD_B.id..ADD_A.id }) { add({ operation }, false) }
                    case ({ operation.id in ADC_B.id..ADC_A.id }) { add({ operation }, true) }
                    case ({ operation.id in SUB_B.id..SUB_A.id }) { sub({ operation }, false) }
                    case ({ operation.id in SBB_B.id..SBB_A.id }) { sub({ operation }, true) }
                    case ({ operation.id in XRA_B.id..XRA_A.id }) { xra({ operation }) }
                    case ({ operation.id in ORA_B.id..ORA_A.id }) { ora({ operation }) }
                    case ({ operation.id in ANA_B.id..ANA_A.id }) { ana({ operation }) }
                    case ({ operation.id in CMP_B.id..CMP_A.id }) { cmp({ operation }) }
                    case ({ operation.id in MOV_B_B.id..MOV_A_A.id }) { mov({ operation }) }
                    case ({ operation == MVI_A_d8 }) { mvi({ operation }, { REGISTER.A }) }
                    case ({ operation == MVI_B_d8 }) { mvi({ operation }, { REGISTER.B }) }
                    case ({ operation == MVI_C_d8 }) { mvi({ operation }, { REGISTER.C }) }
                    case ({ operation == MVI_D_d8 }) { mvi({ operation }, { REGISTER.D }) }
                    case ({ operation == MVI_E_d8 }) { mvi({ operation }, { REGISTER.E }) }
                    case ({ operation == MVI_H_d8 }) { mvi({ operation }, { REGISTER.H }) }
                    case ({ operation == MVI_L_d8 }) { mvi({ operation }, { REGISTER.L }) }
                    case ({ operation == MVI_M_d8 }) { mvi({ operation }, { REGISTER.HL }) }
                    otherwise { state.println { "Skipped operation $operation" } }
                    sleep(1)
                }
                otherwise { sleep(1) }
            }
        }
    }
}

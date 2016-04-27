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
    val emptyOF = bitInput("emptyOF")
    val operationALU = output<COMMAND>("operationALU")
    val operationOF = input<SysUnsigned>("operationOF")
    val enOF = bitOutput("enOF")
    val enDG = bitOutput("enDG")
    val enAG = bitOutput("enAG")
    val enRF = bitOutput("enRF")

    private fun SysUnsigned.toOperation() = OPERATION[this.toInt()] ?: OPERATION.UNDEFINED

    private fun StateContainer.get() {
        state {
            enOF(ONE)
            commandOF(READ)
        }
        state.instance { enOF(ZERO) }
    }

    private fun StateContainer.add(operation: () -> OPERATION) {
        state.println { "ADD: ${operation()}" }
    }

    private fun StateContainer.adc(operation: () -> OPERATION) {
        state.println { "ADC: ${operation()}" }
    }

    private fun StateContainer.sub(operation: () -> OPERATION) {
        state.println { "SUB: ${operation()}" }
    }

    private fun StateContainer.sbb(operation: () -> OPERATION) {
        state.println { "SBB: ${operation()}" }
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

    private fun StateContainer.mov(operation: () -> OPERATION) {
        state.println { "MOV: ${operation()}" }
    }

    private fun StateContainer.mvi(operation: () -> OPERATION) {
        state.println { "MVI: ${operation()}" }
    }

    private var wait = false

    init {
        function(clk1.posEdgeEvent) { wait = false }
        var operation = OPERATION.UNDEFINED
        stateFunction(clk2) {
            state.instance {
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
                    get()
                    state.instance { operation = operationOF().toOperation() }
                    case ({ operation.id in ADD_B.id..ADD_A.id }) { add({ operation }) }
                    case ({ operation.id in ADC_B.id..ADC_A.id }) { adc({ operation }) }
                    case ({ operation.id in SUB_B.id..SUB_A.id }) { sub({ operation }) }
                    case ({ operation.id in SBB_B.id..SBB_A.id }) { sbb({ operation }) }
                    case ({ operation.id in XRA_B.id..XRA_A.id }) { xra({ operation }) }
                    case ({ operation.id in ORA_B.id..ORA_A.id }) { ora({ operation }) }
                    case ({ operation.id in ANA_B.id..ANA_A.id }) { ana({ operation }) }
                    case ({ operation.id in CMP_B.id..CMP_A.id }) { cmp({ operation }) }
                    case ({ operation.id in MOV_B_B.id..MOV_A_A.id }) { mov({ operation }) }
                    case ({ operation.id in MVI_B_d8.id..MVI_A_d8.id }) { mvi({ operation }) }
                    otherwise { sleep(1) }
                }
                otherwise { sleep(1) }
            }
        }
    }
}

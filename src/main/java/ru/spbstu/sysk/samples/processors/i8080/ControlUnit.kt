package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.core.StateContainer
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.samples.processors.i8080.OPERATION.*

class ControlUnit(parent: SysModule) : SysModule("ControlUnit", parent) {

    val reset = bitInput("reset")
    val clk1 = bitInput("clk1")
    val clk2 = bitInput("clk2")
    val dbin = bitOutput("dbin")
    val wr = bitOutput("wr")

    val clk = bitOutput("clk")
    val registerRF = output<REGISTER>("registerRF")
    val commandRF = output<COMMAND>("commandRF")
    val commandOF = output<COMMAND>("commandOF")
    val operationALU = output<COMMAND>("operationALU")
    val operationOF = input<OPERATION>("operationOF")
    val enOF = bitOutput("enOF")
    val enDG = bitOutput("enDG")
    val enAG = bitOutput("enAG")
    val enRF = bitOutput("enRF")

    private var Reset = false
    private fun StateContainer.`Make a reset7`() =
            case ({ Reset }) {
                state.instance {
                    Reset = false
                    enRF(ONE)
                    enOF(ONE)
                    commandOF(COMMAND.RESET)
                    commandRF(COMMAND.RESET)
                }
                jump("start")
            }

    private fun StateContainer.loadCommand() {
        state {
            enAG(ONE)
            enDG(ONE)
            enRF(ONE)
            registerRF(REGISTER.PC)
            commandRF(COMMAND.READ_ADDRESS)
            dbin(ONE)
        }
        sleep(1)
        state {
            enOF(ONE)
            commandOF(COMMAND.WRITE)
            enAG(ZERO)
            enDG(ZERO)
            dbin(ZERO)
            commandRF(COMMAND.INC)
        }
        state {
            enRF(ZERO)
            commandOF(COMMAND.READ)
        }
        state.instance {
            enOF(ZERO)
        }
    }

    private fun StateContainer.add() {
        state.println { "ADD: ${operationOF()}" }
    }

    private fun StateContainer.adc() {
        state.println { "ADC: ${operationOF()}" }
    }

    init {
        function(clk1.defaultEvent) { clk(clk1()) }
        function(reset.posEdgeEvent) { Reset = true }
        stateFunction(clk1) {
            label("start")
            state.instance {
                enOF(ZERO)
                enRF(ZERO)
                enDG(ZERO)
                enAG(ZERO)
                dbin(ZERO)
                wr(ZERO)
            }
            infinite.block {
                `Make a reset7`()
                loadCommand()
                state.instance { println(operationOF()) }
                case ({ operationOF() == NOP }) { sleep(1) }
                case ({ operationOF().id in ADD_B.id..ADD_A.id }) { add() }
                case ({ operationOF().id in ADC_B.id..ADC_A.id }) { adc() }
                otherwise { sleep(1) }
            }
        }
    }
}

package ru.spbstu.sysk.samples.stack

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit.ONE
import ru.spbstu.sysk.data.SysBit.ZERO
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.SysUnsigned
import ru.spbstu.sysk.data.integer.unsigned
import ru.spbstu.sysk.samples.stack.Opcode.*

class Machine(
        name: String,
        private val addrWidth: Int,
        dataWidth: Int,
        parent: SysModule
) : SysModule(name, parent) {

    private val undefined = SysInteger.uninitialized(dataWidth)

    private val stack = integerMemory("stack", addrWidth, dataWidth)
    private var top = undefined
    private var second = undefined
    private var size = -2
    private val address: SysUnsigned
        get() = unsigned(addrWidth, if (size >= 0) size else 0)
    private val limit = stack.lastAddress()

    val din = input<SysInteger>("din")
    val dout = output<SysInteger>("din")
    val opcode = input<Opcode>("opcode")
    val clk = bitInput("clk")

    private var stackEn by signalWriter("en", stack.en)
    private var stackWr by signalWriter("wr", stack.wr)
    private var stackIn by signalWriter("in", stack.din)
    private var stackAddr by signalWriter("addr", stack.addr)
    private val stackOut by signalReader("out", stack.dout)

    init {
        clk.bind(stack.clk)

        stateFunction(clk) {
            init {
                dout(top)
                stackEn = ONE
                stackWr = ZERO
                stackAddr = address
            }
            infiniteLoop {
                case({ opcode() == NOP }) {
                    stackWr = ZERO
                    stackAddr = address
                }
                case({ opcode() == UNDEFINED }) {
                    stackWr = ZERO
                    stackAddr = address
                    dout(undefined)
                    top = undefined
                    second = undefined
                }
                case({ opcode() == PUSH }) {
                    top = din()
                    second = top
                    stackIn = second
                    stackWr = if (size >= 0) ONE else ZERO
                    stackAddr = address
                    dout(din())
                    if (size < limit) size++
                }
                otherwise {
                    val result = when (opcode()) {
                        PLUS -> top + second
                        MINUS -> second - top
                        TIMES -> top * second
                        DIV -> second / top
                        POP -> second
                        else -> undefined
                    }
                    top = result
                    dout(result)
                    stackAddr = address
                    stackWr = ZERO
                    second = stackOut
                    if (size > -2) size--
                }
            }
        }
    }
}
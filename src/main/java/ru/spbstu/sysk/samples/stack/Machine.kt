package ru.spbstu.sysk.samples.stack

import ru.spbstu.sysk.channels.SysSignalRead
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysInteger
import ru.spbstu.sysk.data.SysUnsigned
import ru.spbstu.sysk.samples.stack.Opcode.*

class Machine(
        name: String,
        private val addrWidth: Int,
        private val dataWidth: Int,
        parent: SysModule
) : SysModule(name, parent) {

    private val stack = integerMemory("stack", addrWidth, dataWidth)
    private var top = SysInteger(dataWidth, 0)
    private var second = SysInteger(dataWidth, 0)
    private var size = -2
    private val limit = stack.lastAddress()

    val din = input<SysInteger>("din")
    val dout = output<SysInteger>("din")
    val opcode = input<Opcode>("opcode")
    val clk = bitInput("clk")

    private var stackEn by signalWriter("en", stack.en)
    private var stackIn by signalWriter("in", stack.din)
    private var stackAddr by signalWriter("addr", stack.addr)
    private val stackOut by signalReader("out", stack.dout)

    init {
        clk.bind(stack.clk)

        stateFunction(clk) {
            init {
                dout(top)
                stackEn = SysBit.ZERO
            }
            infiniteLoop {
                case({ opcode() == NOP }) {

                }
                case({ opcode() == PUSH }) {
                    top = din()
                    second = top
                    stackIn = second
                    stackEn = if (size >= 0) SysBit.ONE else SysBit.ZERO
                    stackAddr = SysUnsigned.valueOf(addrWidth, size)
                    size++
                }
            }
        }
    }
}
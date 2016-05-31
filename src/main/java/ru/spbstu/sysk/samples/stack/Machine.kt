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
    private val writeAddress: SysUnsigned
        get() = unsigned(addrWidth, if (size >= 0) size else 0)
    private val readAddress: SysUnsigned
        get() = unsigned(addrWidth, if (size >= 1) size - 1 else 0)
    private val limit = stack.lastAddress()

    val din = input<SysInteger>("din")
    val dout = output<SysInteger>("dout")
    val opcode = input<Opcode>("opcode")
    val clk = bitInput("clk")

    private var stackEn by bitSignalWriter("en", stack.en)
    private var stackWr by bitSignalWriter("wr", stack.wr)
    private var stackIn by signalWriter("in", stack.din)
    private var stackAddr by signalWriter("addr", stack.addr)
    private val stackOut by signalReader("out", stack.dout)

    init {
        stack.clk.bind(clk)

        stateFunction(clk) {
            init {
                dout(top)
                stackEn = ONE
                stackWr = ZERO
                stackAddr = readAddress
            }
            infinite.block {
                caseOf { opcode() }.of (NOP).state {
                    stackWr = ZERO
                    stackAddr = readAddress
                }.of (UNDEFINED).state {
                    stackWr = ZERO
                    stackAddr = readAddress
                    dout(undefined)
                    top = undefined
                    second = undefined
                }.of (PUSH).state {
                    stackIn = second
                    second = top
                    top = din()
                    stackWr = if (size >= 0) ONE else ZERO
                    stackAddr = writeAddress
                    dout(din())
                    if (size < limit) size++
                }.otherwise {
                    state {
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
                        if (size > -2) size--
                        stackAddr = readAddress
                        stackWr = ZERO
                    }
                    state {
                        second = stackOut
                    }
                }
            }
        }
    }
}
package ru.spbstu.sysk.samples.processors.simpleCPU

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.integer
import java.util.*
import ru.spbstu.sysk.samples.processors.simpleCPU.Command.*

/** This class not describes the operation of the real CPU. It is only needed for the test. */
internal class CPU(
        commands: Queue<CommandWithArgument> = LinkedList(),
        A: Long, B: Long, name: String, parent: SysModule
) : SysModule(name, parent) {

    internal data class CommandWithArgument(val cmd: Command, val arg: SysInteger? = null) {
        constructor(cmd: Command, arg: Int): this(cmd, integer(Capacity.ADDRESS, arg))
    }

    val dataPort = busPort<SysBit>(Capacity.DATA, "data")
    val addressPort = busPort<SysBit>(Capacity.ADDRESS, "address")
    val commandPort = busPort<SysBit>(Capacity.COMMAND, "command")

    private var register = arrayOf(SysInteger(Capacity.DATA, A), SysInteger(Capacity.DATA, B))
    private var currentRegister = 0
    private var command = CommandWithArgument(NULL)

    companion object Static {
        val waitStop = SysWait.Time(10)
        val waitRem = SysWait.Time(5)
        val waitDiv = SysWait.Time(5)
        val waitMul = SysWait.Time(5)
        val waitSub = SysWait.Time(5)
        val waitAdd = SysWait.Time(5)
        val waitPush = SysWait.Time(4)
        val waitPull = SysWait.Time(3)
        val waitResponse = SysWait.Time(3)
        val waitPrint = SysWait.Time(2)
        val waitNext = SysWait.Time(2)
        val waitSave = SysWait.Time(2)
        val waitUpdate = SysWait.Time(1)
        val waitDisable = SysWait.Time(1)
    }

    private val startRem = event("rem")
    private val startDiv = event("div")
    private val startMul = event("mul")
    private val startSub = event("sub")
    private val startAdd = event("add")
    private val startPush = event("push")
    private val startPull = event("pull")
    private val startResponse = event("response")
    private val startPrint = event("print")
    private val startNext = event("next")
    private val startSave = event("save")
    private val startDisable = event("disable")
    private val startUpdate = event("update")
    private val startStop = event("stop")

    private val disable: (SysWait) -> SysWait = {
        for (i in 0..(Capacity.DATA - 1)) dataPort(SysBit.Z, i)
        for (i in 0..(Capacity.ADDRESS - 1)) addressPort(SysBit.Z, i)
        for (i in 0..(Capacity.COMMAND - 1)) commandPort(SysBit.Z, i)
        startDisable
    }

    private val add: (SysWait) -> SysWait = {
        register[currentRegister] = register[0] + register[1]
        startAdd
    }

    private val sub: (SysWait) -> SysWait = {
        register[currentRegister] = register[0] - register[1]
        startSub
    }

    private val mul: (SysWait) -> SysWait = {
        register[currentRegister] = (register[0] * register[1]).truncate(Capacity.DATA)
        startMul
    }

    private val div: (SysWait) -> SysWait = {
        register[currentRegister] = register[0] / register[1]
        startDiv
    }

    private val rem: (SysWait) -> SysWait = {
        register[currentRegister] = register[0] % register[1]
        startRem
    }

    private val push: (SysWait) -> SysWait = {
        val address = command.arg!!
        for (i in 0..(Capacity.DATA - 1)) dataPort(register[currentRegister][i], i)
        for (i in 0..(Capacity.ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(Capacity.COMMAND - 1)) commandPort(PUSH[i], i)
        startPush
    }

    private val pull: (SysWait) -> SysWait = {
        val address = command.arg!!
        for (i in 0..(Capacity.ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(Capacity.COMMAND - 1)) commandPort(PULL[i], i)
        startPull
    }

    private val save: (SysWait) -> SysWait = {
        register[currentRegister] = SysInteger(Array(Capacity.DATA, { dataPort[it] }))
        startSave
    }

    private val next: (SysWait) -> SysWait = {
        ++currentRegister
        currentRegister %= Capacity.REGISTER
        startNext
    }

    private val print: (SysWait) -> SysWait = {
        println("$this.register.A: ${register[0]}")
        println("$this.register.B: ${register[1]}")
        startPrint
    }

    private val response: (SysWait) -> SysWait = {
        val address = command.arg!!
        for (i in 0..(Capacity.ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(Capacity.COMMAND - 1)) commandPort(RESPONSE[i], i)
        startResponse
    }

    private val stop: (SysWait) -> SysWait = {
        scheduler.stop()
        SysWait.Never
    }

    private val update: (SysWait) -> SysWait = {
        command = commands.element()
        commands.remove()
        when (command.cmd) {
            ADD -> {
                startAdd.happens(waitAdd)
                startUpdate.happens(waitAdd + waitUpdate)
            }
            SUB -> {
                startSub.happens(waitSub)
                startUpdate.happens(waitSub + waitUpdate)
            }
            MUL -> {
                startMul.happens(waitMul)
                startUpdate.happens(waitMul + waitUpdate)
            }
            DIV -> {
                startDiv.happens(waitDiv)
                startUpdate.happens(waitDiv + waitUpdate)
            }
            REM -> {
                startRem.happens(waitRem)
                startUpdate.happens(waitRem + waitUpdate)
            }
            PUSH -> {
                startPush.happens(waitPush)
                startDisable.happens(waitPush + RAM.waitWrite + waitDisable)
                startUpdate.happens(waitPush + RAM.waitWrite + waitDisable + waitUpdate)
            }
            PULL -> {
                startPull.happens(waitPull)
                startDisable.happens(waitPull + RAM.waitRead + waitDisable)
                startSave.happens(waitPull + RAM.waitRead + waitSave)
                startUpdate.happens(waitPull + RAM.waitRead +
                        if (waitSave > waitDisable) waitSave else waitDisable + waitUpdate)
            }
            NEXT -> {
                startNext.happens(waitNext)
                startUpdate.happens(waitNext + waitUpdate)
            }
            PRINT -> {
                startPrint.happens(waitPrint)
                startUpdate.happens(waitPrint + waitUpdate)
            }
            RESPONSE -> {
                startResponse.happens(waitResponse)
                startDisable.happens(waitResponse + RAM.waitPrint + waitDisable)
                startUpdate.happens(waitResponse + RAM.waitPrint + waitDisable + waitUpdate)
            }
            STOP -> {
                startStop.happens(waitStop)
            }
            NULL -> {}
        }
        startUpdate
    }

    init {
        function(update, SysWait.Initialize, true)
        function(add, startAdd, false)
        function(sub, startSub, false)
        function(mul, startMul, false)
        function(div, startDiv, false)
        function(rem, startRem, false)
        function(push, startPush, false)
        function(disable, startDisable, false)
        function(pull, startPull, false)
        function(save, startSave, false)
        function(next, startNext, false)
        function(print, startPrint, false)
        function(response, startResponse, false)
        function(stop, startStop, false)
    }
}
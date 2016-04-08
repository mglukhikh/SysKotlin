package ru.spbstu.sysk.samples.processors.simpleCPU

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysInteger
import java.util.*
import ru.spbstu.sysk.samples.processors.simpleCPU.MainConstants.CAPACITY
import ru.spbstu.sysk.samples.processors.simpleCPU.MainConstants.COMMAND
import ru.spbstu.sysk.samples.processors.simpleCPU.RAM

/** This class not describes the operation of the real CPU. He only needed for the test. */
internal class CPU constructor(
        commands: Queue<Command> = LinkedList<CPU.Command>(),
        A: Long, B: Long, name: String, parent: SysModule
) : SysModule(name, parent) {

    internal data class Command constructor(val name: SysInteger, val arg: SysInteger? = null)

    val dataPort = busPort<SysBit>(CAPACITY.DATA, "data")
    val addressPort = busPort<SysBit>(CAPACITY.ADDRESS, "address")
    val commandPort = busPort<SysBit>(CAPACITY.COMMAND, "command")

    private var register: Array<SysInteger> = arrayOf(SysInteger(CAPACITY.DATA, A), SysInteger(CAPACITY.DATA, B))
    private var currentRegister = 0
    private var command = Command(COMMAND.NULL)

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
        for (i in 0..(CAPACITY.DATA - 1)) dataPort(SysBit.Z, i)
        for (i in 0..(CAPACITY.ADDRESS - 1)) addressPort(SysBit.Z, i)
        for (i in 0..(CAPACITY.COMMAND - 1)) commandPort(SysBit.Z, i)
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
        register[currentRegister] = (register[0] * register[1]).truncate(CAPACITY.DATA)
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
        for (i in 0..(CAPACITY.DATA - 1)) dataPort(register[currentRegister][i], i)
        for (i in 0..(CAPACITY.ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(CAPACITY.COMMAND - 1)) commandPort(COMMAND.PUSH[i], i)
        startPush
    }

    private val pull: (SysWait) -> SysWait = {
        val address = command.arg!!
        for (i in 0..(CAPACITY.ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(CAPACITY.COMMAND - 1)) commandPort(COMMAND.PULL[i], i)
        startPull
    }

    private val save: (SysWait) -> SysWait = {
        register[currentRegister] = SysInteger(Array(CAPACITY.DATA, { dataPort[it] }))
        startSave
    }

    private val next: (SysWait) -> SysWait = {
        ++currentRegister
        currentRegister %= CAPACITY.REGISTER
        startNext
    }

    private val print: (SysWait) -> SysWait = {
        println("$this.register.A: ${register[0]}")
        println("$this.register.B: ${register[1]}")
        startPrint
    }

    private val response: (SysWait) -> SysWait = {
        val address = command.arg!!
        for (i in 0..(CAPACITY.ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(CAPACITY.COMMAND - 1)) commandPort(COMMAND.RESPONSE[i], i)
        startResponse
    }

    private val stop: (SysWait) -> SysWait = {
        scheduler.stop()
        SysWait.Never
    }

    private val update: (SysWait) -> SysWait = {
        command = commands.element()
        commands.remove()
        when (command.name) {
            COMMAND.ADD -> {
                startAdd.happens(waitAdd)
                startUpdate.happens(waitAdd + waitUpdate)
            }
            COMMAND.SUB -> {
                startSub.happens(waitSub)
                startUpdate.happens(waitSub + waitUpdate)
            }
            COMMAND.MUL -> {
                startMul.happens(waitMul)
                startUpdate.happens(waitMul + waitUpdate)
            }
            COMMAND.DIV -> {
                startDiv.happens(waitDiv)
                startUpdate.happens(waitDiv + waitUpdate)
            }
            COMMAND.REM -> {
                startRem.happens(waitRem)
                startUpdate.happens(waitRem + waitUpdate)
            }
            COMMAND.PUSH -> {
                startPush.happens(waitPush)
                startDisable.happens(waitPush + RAM.waitWrite + waitDisable)
                startUpdate.happens(waitPush + RAM.waitWrite + waitDisable + waitUpdate)
            }
            COMMAND.PULL -> {
                startPull.happens(waitPull)
                startDisable.happens(waitPull + RAM.waitRead + waitDisable)
                startSave.happens(waitPull + RAM.waitRead + waitSave)
                startUpdate.happens(waitPull + RAM.waitRead + if (waitSave > waitDisable) waitSave else waitDisable + waitUpdate)
            }
            COMMAND.NEXT -> {
                startNext.happens(waitNext)
                startUpdate.happens(waitNext + waitUpdate)
            }
            COMMAND.PRINT -> {
                startPrint.happens(waitPrint)
                startUpdate.happens(waitPrint + waitUpdate)
            }
            COMMAND.RESPONSE -> {
                startResponse.happens(waitResponse)
                startDisable.happens(waitResponse + RAM.waitPrint + waitDisable)
                startUpdate.happens(waitResponse + RAM.waitPrint + waitDisable + waitUpdate)
            }
            COMMAND.STOP -> {
                startStop.happens(waitStop)
            }
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
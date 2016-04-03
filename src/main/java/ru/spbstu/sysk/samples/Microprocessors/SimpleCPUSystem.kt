package ru.spbstu.sysk.samples.microprocessors

import org.junit.Test
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.core.*
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.data.*
import ru.spbstu.sysk.data.integer.SysInteger
import java.util.*

internal val CAPACITY_DATA = 32
internal val CAPACITY_ADDRESS = 32
internal val CAPACITY_COMMAND = 5
internal val CAPACITY_RAM = 256
internal val CAPACITY_REGISTER = 2

internal val NULL = SysInteger(CAPACITY_COMMAND, 0)
internal val ADD = SysInteger(CAPACITY_COMMAND, 1)
internal val SUB = SysInteger(CAPACITY_COMMAND, 2)
internal val MUL = SysInteger(CAPACITY_COMMAND, 3)
internal val DIV = SysInteger(CAPACITY_COMMAND, 4)
internal val REM = SysInteger(CAPACITY_COMMAND, 5)
internal val PUSH = SysInteger(CAPACITY_COMMAND, 6)
internal val PULL = SysInteger(CAPACITY_COMMAND, 7)
internal val NEXT = SysInteger(CAPACITY_COMMAND, 8)
internal val PRINT = SysInteger(CAPACITY_COMMAND, 9)
internal val RESPONSE = SysInteger(CAPACITY_COMMAND, 10)
internal val STOP = SysInteger(CAPACITY_COMMAND, 11)

class SimpleCPUSystem : SysTopModule() {

    init {
        var commands: Queue<CPU.Command> = LinkedList()
        commands.add(CPU.Command(PRINT))
        commands.add(CPU.Command(PUSH, SysInteger(CAPACITY_ADDRESS, 16)))
        commands.add(CPU.Command(NEXT))
        commands.add(CPU.Command(PUSH, SysInteger(CAPACITY_ADDRESS, 272)))
        commands.add(CPU.Command(NEXT))
        commands.add(CPU.Command(ADD))
        commands.add(CPU.Command(RESPONSE, SysInteger(CAPACITY_ADDRESS, 16)))
        commands.add(CPU.Command(RESPONSE, SysInteger(CAPACITY_ADDRESS, 272)))
        commands.add(CPU.Command(PRINT))
        commands.add(CPU.Command(PUSH, SysInteger(CAPACITY_ADDRESS, CAPACITY_RAM - 1)))
        commands.add(CPU.Command(RESPONSE, SysInteger(CAPACITY_ADDRESS, CAPACITY_RAM - 1)))
        commands.add(CPU.Command(NEXT))
        commands.add(CPU.Command(PULL, SysInteger(CAPACITY_ADDRESS, 16)))
        commands.add(CPU.Command(PRINT))
        commands.add(CPU.Command(DIV))
        commands.add(CPU.Command(PRINT))
        commands.add(CPU.Command(STOP))
        val ram_1 = RAM(CAPACITY_RAM, CAPACITY_RAM * 0, "RAM#1", this)
        val ram_2 = RAM(CAPACITY_RAM, CAPACITY_RAM * 1, "RAM#2", this)
        val cpu = CPU(commands, 123, 352, "CPU", this)
        val dataBus = bitBus(CAPACITY_DATA, "dataBus")
        val addressBus = bitBus(CAPACITY_ADDRESS, "addressBus")
        val commandBus = bitBus(CAPACITY_COMMAND, "commandBus")
        bind(cpu.dataPort to dataBus, cpu.addressPort to addressBus, cpu.commandPort to commandBus,
                ram_1.dataPort to dataBus, ram_1.addressPort to addressBus, ram_1.commandPort to commandBus,
                ram_2.dataPort to dataBus, ram_2.addressPort to addressBus, ram_2.commandPort to commandBus)
    }

    @Test
    fun show() = start(1(S))
}

/** This class not describes the operation of the real RAM. He only needed for the test. */
internal class RAM constructor(
        val capacity: Int, val firstAddress: Int, name: String, parent: SysModule
)
: SysModule(name, parent) {

    val dataPort = busPort<SysBit>(CAPACITY_DATA, "data")
    val addressPort = busPort<SysBit>(CAPACITY_ADDRESS, "address")
    val commandPort = busPort<SysBit>(CAPACITY_COMMAND, "command")

    private val memory = Array(capacity, { SysInteger(CAPACITY_DATA, 0) })

    companion object Static {
        val waitWrite = SysWait.Time(4)
        val waitRead = SysWait.Time(3)
        val waitPrint = SysWait.Time(2)
        val waitEmitUpdate = SysWait.Time(1)
        val waitDisable = SysWait.Time(1)
    }

    private val startWrite = event("write")
    private val startRead = event("read")
    private val startPrint = event("print")
    private val startEmitUpdate = event("emit")
    private val startDisable = event("disable")
    private val startUpdate = event("update")

    private val disable: (SysWait) -> SysWait = {
        for (i in 0..(CAPACITY_DATA - 1)) dataPort(SysBit.Z, i)
        for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort(SysBit.Z, i)
        for (i in 0..(CAPACITY_COMMAND - 1)) commandPort(SysBit.Z, i)
        startDisable
    }

    private val write: (SysWait) -> SysWait = {
        val data = SysInteger(Array(CAPACITY_DATA, { dataPort[it] }))
        val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
//        if ((address.value > firstAddress) && (address.value < (firstAddress + capacity))) {
//            memory[address.value.toInt() - firstAddress] = data
//        }
        startWrite
    }

    private val read: (SysWait) -> SysWait = {
        val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
//        if ((address.value >= firstAddress) && (address.value < (firstAddress + capacity))) {
//            for (i in 0..(CAPACITY_DATA - 1))
//                dataPort(memory[address.value.toInt() - firstAddress][i], i)
//        }
        startRead
    }

    private val print: (SysWait) -> SysWait = {
        val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
        if (address.value.toInt() >= firstAddress && address.value.toInt() < firstAddress + capacity) {
            println("$this.value: ${memory[address.value.toInt() - firstAddress]}")
            println("$this.address: $address")
        }
        startPrint
    }

    private val emitUpdate: (SysWait) -> SysWait = {
        startUpdate.happens()
        startEmitUpdate
    }

    private val update: (SysWait) -> SysWait = {
        val command = SysInteger(Array(CAPACITY_COMMAND, { commandPort[it] }))
        when (command) {
            PUSH -> {
                startWrite.happens(waitWrite)
                startEmitUpdate.happens(waitEmitUpdate + waitWrite)
                startUpdate
            }
            PULL -> {
                startRead.happens(waitRead)
                startDisable.happens(waitRead + CPU.waitSave + waitDisable)
                startEmitUpdate.happens(waitRead + CPU.waitSave + waitDisable + waitEmitUpdate)
                startUpdate
            }
            RESPONSE -> {
                startPrint.happens(waitPrint)
                startEmitUpdate.happens(waitEmitUpdate + waitPrint)
                startUpdate
            }
            else -> commandPort.defaultEvent
        }
    }

    init {
        function(emitUpdate, SysWait.Initialize, true)
        function(update, startUpdate, false)
        function(disable, startDisable, false)
        function(write, startWrite, false)
        function(read, startRead, false)
        function(print, startPrint, false)
    }
}

/** This class not describes the operation of the real CPU. He only needed for the test. */
internal class CPU constructor(
        protected val commands: Queue<Command> = LinkedList<CPU.Command>(),
        private val A: Long, private val B: Long, name: String, parent: SysModule
) : SysModule(name, parent) {

    internal data class Command constructor(val name: SysInteger, val arg: SysInteger? = null)

    val dataPort = busPort<SysBit>(CAPACITY_DATA, "data")
    val addressPort = busPort<SysBit>(CAPACITY_ADDRESS, "address")
    val commandPort = busPort<SysBit>(CAPACITY_COMMAND, "command")

    private var register: Array<SysInteger> = arrayOf(SysInteger(CAPACITY_DATA, A), SysInteger(CAPACITY_DATA, B))
    private var currentRegister = 0
    private var command = Command(NULL)

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
        for (i in 0..(CAPACITY_DATA - 1)) dataPort(SysBit.Z, i)
        for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort(SysBit.Z, i)
        for (i in 0..(CAPACITY_COMMAND - 1)) commandPort(SysBit.Z, i)
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
        register[currentRegister] = (register[0] * register[1]).truncate(CAPACITY_DATA)
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
        for (i in 0..(CAPACITY_DATA - 1)) dataPort(register[currentRegister][i], i)
        for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(CAPACITY_COMMAND - 1)) commandPort(PUSH[i], i)
        startPush
    }

    private val pull: (SysWait) -> SysWait = {
        val address = command.arg!!
        for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(CAPACITY_COMMAND - 1)) commandPort(PULL[i], i)
        startPull
    }

    private val save: (SysWait) -> SysWait = {
        register[currentRegister] = SysInteger(Array(CAPACITY_DATA, { dataPort[it] }))
        startSave
    }

    private val next: (SysWait) -> SysWait = {
        ++currentRegister
        currentRegister %= CAPACITY_REGISTER
        startNext
    }

    private val print: (SysWait) -> SysWait = {
        println("$this.register.A: ${register[0]}")
        println("$this.register.B: ${register[1]}")
        startPrint
    }

    private val response: (SysWait) -> SysWait = {
        val address = command.arg!!
        for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort(address[i], i)
        for (i in 0..(CAPACITY_COMMAND - 1)) commandPort(RESPONSE[i], i)
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
                startUpdate.happens(waitPull + RAM.waitRead + if (waitSave > waitDisable) waitSave else waitDisable + waitUpdate)
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
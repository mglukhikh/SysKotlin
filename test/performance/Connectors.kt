package performance

import org.junit.Test
import sysk.*
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

class Connectors {
    internal object Empty {}

    /** This class not describes the operation of the real RAM. He only needed for the test. */
    internal class RAM : SysModule {

        public val dataPort = busPort<SysWireState>("data")
        public val addressPort = busPort<SysWireState>("address")
        public val commandPort = busPort<SysWireState>("command")

        val capacity: Int
        val firstAddress: Int
        private val memory: Array<SysInteger>

        public val waitWrite: Long = 4
        public val waitRead: Long = 3
        public val waitPrint: Long = 2
        public val waitEmitUpdate: Long = 1
        public val waitDisable: Long = 1

        public val startWrite = event("write")
        public val startRead = event("read")
        public val startPrint = event("print")
        public val startEmitUpdate = event("emit")
        public val startDisable = event("disable")
        public val startUpdate = event("update")

        constructor(capacity: Int, firstAddress: Int, name: String, parent: SysModule) : super(name, parent) {
            this.capacity = capacity
            this.firstAddress = firstAddress
            this.memory = Array(capacity, { SysInteger(CAPACITY_DATA, 0) })

            function(emitUpdate, SysWait.Initialize, true)
            function(update, startUpdate, false)
            function(disable, startDisable, false)
            function(write, startWrite, false)
            function(read, startRead, false)
            function(print, startPrint, false)
        }

        constructor(ignored: Empty) : super("Empty", SysTopModule("Empty")) {
            capacity = 0
            firstAddress = 0
            memory = Array(capacity, { SysInteger(CAPACITY_DATA, 0) })
        }

        private val disable: (SysWait) -> SysWait = {
            for (i in 0..(CAPACITY_DATA - 1)) dataPort.set(SysWireState.Z, i)
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(SysWireState.Z, i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(SysWireState.Z, i)
            startDisable
        }

        private val write: (SysWait) -> SysWait = {
            val data = SysInteger(Array(CAPACITY_DATA, { dataPort[it] }))
            val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
            if ((address.value > firstAddress) && (address.value < (firstAddress + capacity))) {
                memory[address.value.toInt() - firstAddress] = data
            }
            startWrite
        }

        private val read: (SysWait) -> SysWait = {
            val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
            if ((address.value >= firstAddress) && (address.value < (firstAddress + capacity))) {
                for (i in 0..(CAPACITY_DATA - 1))
                    dataPort.set(memory[address.value.toInt() - firstAddress][i], i)
            }
            startRead
        }

        private val print: (SysWait) -> SysWait = {
            val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
            if ((address.value >= firstAddress) && (address.value < (firstAddress + capacity))) {
                println("$name value: ${memory[address.value.toInt() - firstAddress]}{${address.value}}")
            }
            startPrint
        }

        private val emitUpdate: (SysWait) -> SysWait = {
            startUpdate.happens()
            startEmitUpdate
        }

        private val update: (SysWait) -> SysWait = {
            val command = SysInteger(Array(CAPACITY_COMMAND, { commandPort[it] }))
//            println("$name command $command")
            when (command) {
                PUSH -> {
                    startWrite.happens(waitWrite)
                    startEmitUpdate.happens(waitEmitUpdate + waitWrite)
                    startUpdate
                }
                PULL -> {
                    startRead.happens(waitRead)
                    /** Todo: without CPU(Empty) */
                    startDisable.happens(waitRead + CPU(Empty).waitSave + waitDisable)
                    startEmitUpdate.happens(waitRead + CPU(Empty).waitSave + waitDisable + waitEmitUpdate)
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
    }

    /** This class not describes the operation of the real CPU. He only needed for the test. */
    internal class CPU : SysModule {

        internal class Command constructor(public val name: SysInteger, public val arg: SysInteger? = null) {}

        public val dataPort = busPort<SysWireState>("data")
        public val addressPort = busPort<SysWireState>("address")
        public val commandPort = busPort<SysWireState>("command")

        protected val commands: Queue<CPU.Command>
        private var register: Array<SysInteger>
        private var currentRegister = 0
        private var command = Command(NULL)

        public val waitRem: Long = 5
        public val waitDiv: Long = 5
        public val waitMul: Long = 5
        public val waitSub: Long = 5
        public val waitAdd: Long = 5
        public val waitPush: Long = 4
        public val waitPull: Long = 3
        public val waitResponse: Long = 3
        public val waitPrint: Long = 2
        public val waitNext: Long = 2
        public val waitSave: Long = 2
        public val waitUpdate: Long = 1
        public val waitDisable: Long = 1

        public val startRem = event("rem")
        public val startDiv = event("div")
        public val startMul = event("mul")
        public val startSub = event("sub")
        public val startAdd = event("add")
        public val startPush = event("push")
        public val startPull = event("pull")
        public val startResponse = event("response")
        public val startPrint = event("print")
        public val startNext = event("next")
        public val startSave = event("save")
        public val startDisable = event("disable")
        public val startUpdate = event("update")

        constructor(commands: Queue<CPU.Command> = LinkedList(), A: Long, B: Long, name: String, parent: SysModule
        ) : super(name, parent) {
            this.commands = commands
            this.register = arrayOf(SysInteger(CAPACITY_DATA, A), SysInteger(CAPACITY_DATA, B))

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
        }

        constructor(ignored: Empty) : super("Empty", SysTopModule("Empty")) {
            this.commands = LinkedList<Command>()
            this.register = arrayOf()
        }

        private val disable: (SysWait) -> SysWait = {
            for (i in 0..(CAPACITY_DATA - 1)) dataPort.set(SysWireState.Z, i)
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(SysWireState.Z, i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(SysWireState.Z, i)
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
            register[currentRegister] = register[0] * register[1]
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
            for (i in 0..(CAPACITY_DATA - 1)) dataPort.set(register[currentRegister][i], i)
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(address[i], i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(PUSH[i], i)
            startPush
        }

        private val pull: (SysWait) -> SysWait = {
            val address = command.arg!!
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(address[i], i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(PULL[i], i)
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
            var message = StringBuilder("$name register: ")
            register.forEach { message = message.append("$it ") }
            println(message)
            startPrint
        }

        private val response: (SysWait) -> SysWait = {
            val address = command.arg!!
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(address[i], i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(RESPONSE[i], i)
            startResponse
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
                    /** Todo: without RAM(Empty) */
                    startDisable.happens(waitPush + RAM(Empty).waitWrite + waitDisable)
                    startUpdate.happens(waitPush + RAM(Empty).waitWrite + waitDisable + waitUpdate)
                }
                PULL -> {
                    startPull.happens(waitPull)
                    /** Todo: without RAM(Empty) */
                    startDisable.happens(waitPull + RAM(Empty).waitRead + waitDisable)
                    startSave.happens(waitPull + RAM(Empty).waitRead + waitSave)
                    startUpdate.happens(waitPull + RAM(Empty).waitRead + if (waitSave > waitDisable) waitSave else waitDisable + waitUpdate)
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
                    /** Todo: without RAM(Empty) */
                    startDisable.happens(waitResponse + RAM(Empty).waitPrint + waitDisable)
                    startUpdate.happens(waitResponse + RAM(Empty).waitPrint + waitDisable + waitUpdate)
                }
                STOP -> {
                    scheduler.stop()
                }
            }
            startUpdate
        }
    }

    internal object TopModule : SysTopModule("Connectors", SysScheduler()) {
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
            commands.add(CPU.Command(MUL))
            commands.add(CPU.Command(PRINT))
            commands.add(CPU.Command(STOP))
            val ram_1 = RAM(CAPACITY_RAM, CAPACITY_RAM * 0, "RAM #1", this)
            val ram_2 = RAM(CAPACITY_RAM, CAPACITY_RAM * 1, "RAM #2", this)
            val cpu = CPU(commands, 123, 352, "CPU", this)
            val dataBus = wireBus("dataBus")
            val addressBus = wireBus("addressBus")
            val commandBus = wireBus("commandBus")
            for (i in 0..(CAPACITY_DATA - 1)) dataBus.addWire()
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressBus.addWire()
            for (i in 0..(CAPACITY_COMMAND - 1)) commandBus.addWire()
            cpu.dataPort.bind(dataBus)
            cpu.addressPort.bind(addressBus)
            cpu.commandPort.bind(commandBus)
            ram_1.dataPort.bind(dataBus)
            ram_1.addressPort.bind(addressBus)
            ram_1.commandPort.bind(commandBus)
            ram_2.dataPort.bind(dataBus)
            ram_2.addressPort.bind(addressBus)
            ram_2.commandPort.bind(commandBus)
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}

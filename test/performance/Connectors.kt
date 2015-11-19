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

        public val waitWrite = SysWait.Time(4)
        public val waitRead = SysWait.Time(3)
        public val waitPrint = SysWait.Time(2)
        public val waitEmit = SysWait.Time(1)
        public val waitDisable = SysWait.Time(1)

        constructor(capacity: Int, firstAddress: Int, name: String, parent: SysModule) : super(name, parent) {
            this.capacity = capacity
            this.firstAddress = firstAddress
            this.memory = Array(capacity, { SysInteger(CAPACITY_DATA, 0) })
            function(emitUpdate, SysWait.Initialize, true)
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
            SysWait.Never
        }

        private val write: (SysWait) -> SysWait = {
            val data = SysInteger(Array(CAPACITY_DATA, { dataPort[it] }))
            val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
            if ((address.value > firstAddress) && (address.value < (firstAddress + capacity))) {
                memory[address.value.toInt() - firstAddress] = data
            }
            SysWait.Never
        }

        private val read: (SysWait) -> SysWait = {
            val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
            if ((address.value >= firstAddress) && (address.value < (firstAddress + capacity))) {
                for (i in 0..(CAPACITY_DATA - 1))
                    dataPort.set(memory[address.value.toInt() - firstAddress][i], i)
            }
            SysWait.Never
        }

        private val print: (SysWait) -> SysWait = {
            val address = SysInteger(Array(CAPACITY_ADDRESS, { addressPort[it] }))
            if ((address.value >= firstAddress) && (address.value < (firstAddress + capacity))) {
                println("$name value: ${memory[address.value.toInt() - firstAddress]}{${address.value}}")
            }
            SysWait.Never
        }

        private val emitUpdate: (SysWait) -> SysWait = {
            function(update, commandPort.defaultEvent, false)
            SysWait.Never
        }

        private val update: (SysWait) -> SysWait = {
            val command = SysInteger(Array(CAPACITY_COMMAND, { commandPort[it] }))
            when (command) {
                PUSH -> {
                    function(write, waitWrite, false)
                    function(emitUpdate, waitEmit + waitWrite, false)
                }
                PULL -> {
                    function(read, waitRead, false)
                    /** Todo: without CPU(Empty) */
                    function(disable, waitRead + CPU(Empty).waitSave + waitDisable, false)
                    function(emitUpdate, waitRead + CPU(Empty).waitSave + waitDisable + waitEmit, false)
                }
                RESPONSE -> {
                    function(print, waitPrint, false)
                    function(emitUpdate, waitEmit + waitPrint, false)
                }
                else -> function(update, commandPort.defaultEvent, false)
            }
            SysWait.Never
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

        public val waitRem = SysWait.Time(5)
        public val waitDiv = SysWait.Time(5)
        public val waitMul = SysWait.Time(5)
        public val waitSub = SysWait.Time(5)
        public val waitAdd = SysWait.Time(5)
        public val waitPush = SysWait.Time(4)
        public val waitPull = SysWait.Time(3)
        public val waitResponse = SysWait.Time(3)
        public val waitPrint = SysWait.Time(2)
        public val waitNext = SysWait.Time(2)
        public val waitSave = SysWait.Time(2)
        public val waitUpdate = SysWait.Time(1)
        public val waitDisable = SysWait.Time(1)

        constructor(commands: Queue<CPU.Command> = LinkedList(), A: Long, B: Long, name: String, parent: SysModule
        ) : super(name, parent) {
            this.commands = commands
            this.register = arrayOf(SysInteger(CAPACITY_DATA, A), SysInteger(CAPACITY_DATA, B))
            function(update, SysWait.Initialize, true)
        }

        constructor(ignored: Empty) : super("Empty", SysTopModule("Empty")) {
            this.commands = LinkedList<Command>()
            this.register = arrayOf()
        }

        private val disable: (SysWait) -> SysWait = {
            for (i in 0..(CAPACITY_DATA - 1)) dataPort.set(SysWireState.Z, i)
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(SysWireState.Z, i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(SysWireState.Z, i)
            SysWait.Never
        }

        private val add: (SysWait) -> SysWait = {
            register[currentRegister] = register[0] + register[1]
            SysWait.Never
        }

        private val sub: (SysWait) -> SysWait = {
            register[currentRegister] = register[0] - register[1]
            SysWait.Never
        }

        private val mul: (SysWait) -> SysWait = {
            register[currentRegister] = register[0] * register[1]
            SysWait.Never
        }

        private val div: (SysWait) -> SysWait = {
            register[currentRegister] = register[0] / register[1]
            SysWait.Never
        }

        private val rem: (SysWait) -> SysWait = {
            register[currentRegister] = register[0] % register[1]
            SysWait.Never
        }

        private val push: (SysWait) -> SysWait = {
            val address = command.arg!!
            for (i in 0..(CAPACITY_DATA - 1)) dataPort.set(register[currentRegister][i], i)
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(address[i], i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(PUSH[i], i)
            SysWait.Never
        }

        private val pull: (SysWait) -> SysWait = {
            val address = command.arg!!
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(address[i], i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(PULL[i], i)
            SysWait.Never
        }

        private val save: (SysWait) -> SysWait = {
            register[currentRegister] = SysInteger(Array(CAPACITY_DATA, { dataPort[it] }))
            SysWait.Never
        }

        private val next: (SysWait) -> SysWait = {
            ++currentRegister
            currentRegister %= CAPACITY_REGISTER
            SysWait.Never
        }

        private val print: (SysWait) -> SysWait = {
            var message = StringBuilder("$name register: ")
            register.forEach { message = message.append("$it ") }
            println(message)
            SysWait.Never
        }

        private val response: (SysWait) -> SysWait = {
            val address = command.arg!!
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(address[i], i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(RESPONSE[i], i)
            SysWait.Never
        }

        private val update: (SysWait) -> SysWait = {
            command = commands.element()
            commands.remove()
            when (command.name) {
                ADD -> {
                    function(add, waitAdd, false)
                    function(update, waitAdd + waitUpdate, false)
                }
                SUB -> {
                    function(sub, waitSub, false)
                    function(update, waitSub + waitUpdate, false)
                }
                MUL -> {
                    function(mul, waitMul, false)
                    function(update, waitMul + waitUpdate, false)
                }
                DIV -> {
                    function(div, waitDiv, false)
                    function(update, waitDiv + waitUpdate, false)
                }
                REM -> {
                    function(rem, waitRem, false)
                    function(update, waitRem + waitUpdate, false)
                }
                PUSH -> {
                    function(push, waitPush, false)
                    /** Todo: without RAM(Empty) */
                    function(disable, waitPush + RAM(Empty).waitWrite + waitDisable, false)
                    function(update, waitPush + RAM(Empty).waitWrite + waitDisable + waitUpdate, false)
                }
                PULL -> {
                    function(pull, waitPull, false)
                    /** Todo: without RAM(Empty) */
                    function(disable, waitPull + RAM(Empty).waitRead + waitDisable, false)
                    function(save, waitPull + RAM(Empty).waitRead + waitSave, false)
                    function(update, waitPull + RAM(Empty).waitRead
                            + if (waitSave > waitDisable) waitSave else waitDisable
                            + waitUpdate, false)
                }
                NEXT -> {
                    function(next, waitNext, false)
                    function(update, waitNext + waitUpdate, false)
                }
                PRINT -> {
                    function(print, waitPrint, false)
                    function(update, waitPrint + waitUpdate, false)
                }
                RESPONSE -> {
                    function(response, waitResponse, false)
                    /** Todo: without RAM(Empty) */
                    function(update, waitResponse + RAM(Empty).waitPrint + waitDisable + waitUpdate, false)
                    function(disable, waitResponse + RAM(Empty).waitPrint + waitDisable, false)
                }
                STOP -> {
                    scheduler.stop()
                }
            }
            SysWait.Never
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

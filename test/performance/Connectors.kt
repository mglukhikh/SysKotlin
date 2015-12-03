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
    internal class Hub constructor(
            public val capacity: Int, name: String, parent: SysModule)
    : SysModule(name, parent) {

        private var currentInput = 0

        public val inputs = Array(capacity, { fifoInput<AssertModule.Status>("input_$it") })
        public val output = fifoOutput<AssertModule.Status>("output")

        public val waitListen = SysWait.Time(1)

        private val startListen = event("listen")
        private val inputEvents = SysWait.OneOf(Array<SysWait>(inputs.size, { inputs[it].defaultEvent }).toList())

        private val listen: (SysWait) -> SysWait = {
            var wait = false
            for (i in 0..inputs.lastIndex) {
                if (!inputs[i].empty) {
                    output.value = inputs[i].value
                    output.push = SysWireState.ZERO
                    output.push = SysWireState.ONE
                    inputs[i].pop = SysWireState.ZERO
                    inputs[i].pop = SysWireState.ONE
                    currentInput = i
                    startListen.happens(waitListen)
                    wait = true
                    break
                }
            }
            if (wait) startListen
            else inputEvents
        }

        init {
            function(listen, inputEvents, false)
        }
    }

    internal class AssertModule constructor(
            name: String, parent: SysModule)
    : SysModule(name, parent) {

        internal open class Status : SysPackable {

            public val description: Array<SysInteger>
            public val parent: SysModule?

            override val undefined: Boolean
                get() = (parent != null)

            constructor(description: Array<SysInteger>, parent: SysModule) {
                this.description = description
                this.parent = parent
            }

            constructor() {
                description = arrayOf()
                parent = null
            }

            override public fun toString() = "Status ${parent!!.name}: ${toString(description)}"

            private fun toString(array: Array<SysInteger>): String {
                val str = StringBuilder()
                for (int in array) str.append("${int.toString()} ")
                return str.toString()
            }
        }

        public val logPort = fifoInput<Status>("log")

        private val expectedDescription: MutableList<Array<SysInteger>> = LinkedList()

        companion object Static {
            public val waitSave = SysWait.Time(3)
        }

        private val startSave = event("save")
        private val finishSave = event("save")

        private val save: (SysWait) -> SysWait = {
            assert(expectedDescription[expectedDescription.lastIndex].size == logPort.value.description.size)
            for (i in 0..expectedDescription[expectedDescription.lastIndex].lastIndex)
                assert(expectedDescription[expectedDescription.lastIndex][i].equals(logPort.value.description[i]))
            expectedDescription.removeAt(expectedDescription.lastIndex)
            logPort.pop = SysWireState.ZERO
            logPort.pop = SysWireState.ONE
            finishSave.happens()
            startSave
        }

        private val listen: (SysWait) -> SysWait = {
            if (!logPort.empty) {
                startSave.happens(waitSave)
                finishSave
            } else {
                logPort.defaultEvent
            }
        }

        init {
            expectedDescription.add(arrayOf(SysInteger(CAPACITY_DATA, 475), SysInteger(CAPACITY_DATA, 3)))
            expectedDescription.add(arrayOf(SysInteger(CAPACITY_DATA, 475), SysInteger(CAPACITY_DATA, 123)))
            expectedDescription.add(arrayOf(SysInteger(CAPACITY_DATA, 475), SysInteger(CAPACITY_ADDRESS, 255)))
            expectedDescription.add(arrayOf(SysInteger(CAPACITY_DATA, 475), SysInteger(CAPACITY_DATA, 352)))
            expectedDescription.add(arrayOf(SysInteger(CAPACITY_DATA, 352), SysInteger(CAPACITY_ADDRESS, 272)))
            expectedDescription.add(arrayOf(SysInteger(CAPACITY_DATA, 123), SysInteger(CAPACITY_ADDRESS, 16)))
            expectedDescription.add(arrayOf(SysInteger(CAPACITY_DATA, 123), SysInteger(CAPACITY_DATA, 352)))
            function(listen, logPort.defaultEvent, false)
            function(save, startSave, false)
        }
    }

    /** This class not describes the operation of the real RAM. He only needed for the test. */
    internal class RAM constructor(
            public val capacity: Int, public val firstAddress: Int, name: String, parent: SysModule
    )
    : SysModule(name, parent) {

        public val dataPort = busPort<SysWireState>("data")
        public val addressPort = busPort<SysWireState>("address")
        public val commandPort = busPort<SysWireState>("command")
        public val logPort = fifoOutput<AssertModule.Status>("log")

        private val memory = Array(capacity, { SysInteger(CAPACITY_DATA, 0) })

        companion object Static {
            public val waitWrite = SysWait.Time(4)
            public val waitRead = SysWait.Time(3)
            public val waitPrint = SysWait.Time(2)
            public val waitEmitUpdate = SysWait.Time(1)
            public val waitDisable = SysWait.Time(1)
        }

        private val startWrite = event("write")
        private val startRead = event("read")
        private val startPrint = event("print")
        private val startEmitUpdate = event("emit")
        private val startDisable = event("disable")
        private val startUpdate = event("update")

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
            if (address.value.toInt() >= firstAddress && address.value.toInt() < firstAddress + capacity) {
                logPort.value = AssertModule.Status(arrayOf(memory[address.value.toInt() - firstAddress], address), this)
                logPort.push = SysWireState.ZERO
                logPort.push = SysWireState.ONE
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
                    startDisable.happens(waitRead + CPU.Static.waitSave + waitDisable)
                    startEmitUpdate.happens(waitRead + CPU.Static.waitSave + waitDisable + waitEmitUpdate)
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
            protected val commands: Queue<CPU.Command> = linkedListOf(),
            private val A: Long, private val B: Long, name: String, parent: SysModule
    ) : SysModule(name, parent) {

        internal class Command constructor(public val name: SysInteger, public val arg: SysInteger? = null) {}

        public val dataPort = busPort<SysWireState>("data")
        public val addressPort = busPort<SysWireState>("address")
        public val commandPort = busPort<SysWireState>("command")
        public val logPort = fifoOutput<AssertModule.Status>("log")

        private var register: Array<SysInteger> = arrayOf(SysInteger(CAPACITY_DATA, A), SysInteger(CAPACITY_DATA, B))
        private var currentRegister = 0
        private var command = Command(NULL)

        companion object Static {
            public val waitStop = SysWait.Time(10)
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
            logPort.value = AssertModule.Status(register, this)
            logPort.push = SysWireState.ZERO
            logPort.push = SysWireState.ONE
            startPrint
        }

        private val response: (SysWait) -> SysWait = {
            val address = command.arg!!
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressPort.set(address[i], i)
            for (i in 0..(CAPACITY_COMMAND - 1)) commandPort.set(RESPONSE[i], i)
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
                    startDisable.happens(waitPush + RAM.Static.waitWrite + waitDisable)
                    startUpdate.happens(waitPush + RAM.Static.waitWrite + waitDisable + waitUpdate)
                }
                PULL -> {
                    startPull.happens(waitPull)
                    startDisable.happens(waitPull + RAM.Static.waitRead + waitDisable)
                    startSave.happens(waitPull + RAM.Static.waitRead + waitSave)
                    startUpdate.happens(waitPull + RAM.Static.waitRead + if (waitSave > waitDisable) waitSave else waitDisable + waitUpdate)
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
                    startDisable.happens(waitResponse + RAM.Static.waitPrint + waitDisable)
                    startUpdate.happens(waitResponse + RAM.Static.waitPrint + waitDisable + waitUpdate)
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
            commands.add(CPU.Command(DIV))
            commands.add(CPU.Command(PRINT))
            commands.add(CPU.Command(STOP))
            val ram_1 = RAM(CAPACITY_RAM, CAPACITY_RAM * 0, "RAM #1", this)
            val ram_2 = RAM(CAPACITY_RAM, CAPACITY_RAM * 1, "RAM #2", this)
            val cpu = CPU(commands, 123, 352, "CPU", this)
            val hub = Hub(3, "hub", this)
            val dataBus = wireBus("dataBus")
            val addressBus = wireBus("addressBus")
            val commandBus = wireBus("commandBus")
            val assertModule = AssertModule("AM", this)
            val logFifoRam_1 = asynchronousFifo(10, "logFifo", AssertModule.Status())
            val logFifoRam_2 = asynchronousFifo(10, "logFifo", AssertModule.Status())
            val logFifoCpu = asynchronousFifo(10, "logFifo", AssertModule.Status())
            val logFifoHub = asynchronousFifo(10, "logFifo", AssertModule.Status())
            for (i in 0..(CAPACITY_DATA - 1)) dataBus.addWire()
            for (i in 0..(CAPACITY_ADDRESS - 1)) addressBus.addWire()
            for (i in 0..(CAPACITY_COMMAND - 1)) commandBus.addWire()
            bind(cpu.dataPort to dataBus, cpu.addressPort to addressBus, cpu.commandPort to commandBus,
                 ram_1.dataPort to dataBus, ram_1.addressPort to addressBus, ram_1.commandPort to commandBus,
                 ram_2.dataPort to dataBus, ram_2.addressPort to addressBus, ram_2.commandPort to commandBus)
            bind(cpu.logPort to logFifoCpu, ram_1.logPort to logFifoRam_1, ram_2.logPort to logFifoRam_2,
                 assertModule.logPort to logFifoHub,
                 hub.inputs[0] to logFifoCpu, hub.inputs[1] to logFifoRam_1, hub.inputs[2] to logFifoRam_2,
                 hub.output to logFifoHub)
        }
    }

    @Test
    fun show() {
        TopModule.start()
    }
}

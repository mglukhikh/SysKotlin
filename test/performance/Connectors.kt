package performance

import org.junit.Test
import sysk.*
import java.util.*

internal val CAPACITY_DATA = 32;
internal val CAPACITY_ADDRESS = 32;
internal val CAPACITY_COMMAND = 2;

class Connectors {
    /** This class not describes the operation of the real RAM. He only needed for the test. */
    internal class RAM constructor(
            val capacity: Int, val firstAdress: Int, name: String, scheduler: SysScheduler, parent: SysModule? = null
    ) : SysModule(name, scheduler, parent) {

        private val dataPort = SysBusPort<SysWireState>(name, null, null)
        private val addressPort = SysBusPort<SysWireState>(name, null, null)
        private val commandPort = SysBusPort<SysWireState>(name, null, null)

        private val data = Array(capacity, { SysInteger(CAPACITY_DATA, 0) })

        private var waiting = true;

        public val writeWait = SysWait.Time(3)
        private val write = function({
            if (waiting) {
                /** Todo: operation read value from the bus */
                waiting = false;
                writeWait
            }
            else {
                /** Todo: operation put value to the memory */
                waiting = true;
                commandPort.defaultEvent
            }
        }, commandPort.defaultEvent, false)


        public val readWait = SysWait.Time(2)
        private val read = function({
            if (waiting) {
                /** Todo: operation write value to the bus */
                waiting = false;
                readWait
            }
            else {
                /** Todo: operation get value from the memory */
                waiting = true;
                commandPort.defaultEvent
            }
        }, commandPort.defaultEvent, false)
    }

    /** This class not describes the operation of the real CPU. He only needed for the test. */
    internal class CPU constructor(
            protected var commands: Queue<String> = LinkedList(), A: Long, B: Long,
            name: String, scheduler: SysScheduler, parent: SysModule? = null
    ) : SysModule(name, scheduler, parent) {

        private val dataPort = SysBusPort<SysWireState>(name, null, null)
        private val addressPort = SysBusPort<SysWireState>(name, null, null)
        private val commandPort = SysBusPort<SysWireState>(name, null, null)

        private var registerA: SysInteger = SysInteger(CAPACITY_DATA, A)
        private var registerB: SysInteger = SysInteger(CAPACITY_DATA, B)

        public val waitAdd = SysWait.Time(5)
        public val waitGet = SysWait.Time(1)
        public val waitPut = SysWait.Time(1)
        public val waitPrint = SysWait.Time(10)

        private val execute = function({
            val command = commands.element()
            commands.remove()
            when(command) {
                "add" -> {
                    /** Todo: operation add */
                    waitAdd
                }
                "put A to ram_1" -> {
                    /** Todo: operation put */
                    waitPut
                }
                "put A to ram_2" -> {
                    /** Todo: operation put */
                    waitPut
                }
                "put B to ram_1" -> {
                    /** Todo: operation put */
                    waitPut
                }
                "put B to ram_2" -> {
                    /** Todo: operation put */
                    waitPut
                }
                "get A from ram_1" -> {
                    /** Todo: operation get */
                    waitGet
                }
                "get A from ram_2" -> {
                    /** Todo: operation get */
                    waitGet
                }
                "get B from ram_1" -> {
                    /** Todo: operation get */
                    waitGet
                }
                "get B from ram_2" -> {
                    /** Todo: operation get */
                    waitGet
                }
                "print" -> {
                    println("Status registers CPU ${name} A: ${registerA} B: ${registerB}")
                    waitPrint
                }
                "stop" -> {
                    scheduler.stop()
                    SysWait.Never
                }
                else -> throw IllegalArgumentException("Mistake in test.")
            }
        }, initialize = true)
    }

    @Test
    fun show() {
        val scheduler = SysScheduler()
        val ram_1 = RAM(255, 0x0, "ram_1", scheduler, null)
        val ram_2 = RAM(255, 0x100, "ram_2", scheduler, null)
        val ram_3 = RAM(255, 0x200, "ram_3", scheduler, null)
        var commands: Queue<String> = LinkedList()
        commands.add("print")
        commands.add("add")
        commands.add("print")
        commands.add("put A to ram_2")
        commands.add("add")
        commands.add("print")
        commands.add("put A to ram_1")
        commands.add("get B from ram_2")
        commands.add("print")
        commands.add("get A from ram_2")
        commands.add("print")
        commands.add("add")
        commands.add("print")
        commands.add("stop")
        val cpu = CPU(commands, 123, 352, "cpu", scheduler, null)
        scheduler.start()
    }
}
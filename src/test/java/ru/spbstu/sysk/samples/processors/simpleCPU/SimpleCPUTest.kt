package ru.spbstu.sysk.samples.processors.simpleCPU;

import org.junit.Test
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.S
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.samples.processors.simpleCPU.CPU.CommandWithArgument
import ru.spbstu.sysk.samples.processors.simpleCPU.Command.*
import java.util.*

class SimpleCPUTest : SysTopModule() {

    init {
        val commands: Queue<CommandWithArgument> = LinkedList()
        commands.add(CommandWithArgument(PRINT))
        commands.add(CommandWithArgument(PUSH, 16))
        commands.add(CommandWithArgument(NEXT))
        commands.add(CommandWithArgument(PUSH, 272))
        commands.add(CommandWithArgument(NEXT))
        commands.add(CommandWithArgument(ADD))
        commands.add(CommandWithArgument(RESPONSE, 16))
        commands.add(CommandWithArgument(RESPONSE, 272))
        commands.add(CommandWithArgument(PRINT))
        commands.add(CommandWithArgument(PUSH, Capacity.RAM - 1))
        commands.add(CommandWithArgument(RESPONSE, Capacity.RAM - 1))
        commands.add(CommandWithArgument(NEXT))
        commands.add(CommandWithArgument(PULL, 16))
        commands.add(CommandWithArgument(PRINT))
        commands.add(CommandWithArgument(DIV))
        commands.add(CommandWithArgument(PRINT))
        commands.add(CommandWithArgument(STOP))
        val ram_1 = RAM(Capacity.RAM, Capacity.RAM * 0, "RAM#1", this)
        val ram_2 = RAM(Capacity.RAM, Capacity.RAM * 1, "RAM#2", this)
        val cpu = CPU(commands, 123, 352, "CPU", this)
        val dataBus = bitBus(Capacity.DATA, "dataBus")
        val addressBus = bitBus(Capacity.ADDRESS, "addressBus")
        val commandBus = bitBus(Capacity.COMMAND, "commandBus")
        bind(cpu.dataPort to dataBus, cpu.addressPort to addressBus, cpu.commandPort to commandBus,
                ram_1.dataPort to dataBus, ram_1.addressPort to addressBus, ram_1.commandPort to commandBus,
                ram_2.dataPort to dataBus, ram_2.addressPort to addressBus, ram_2.commandPort to commandBus)
    }

    @Test
    fun show() = start(1(S))
}
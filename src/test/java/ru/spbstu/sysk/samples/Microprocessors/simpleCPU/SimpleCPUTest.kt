package ru.spbstu.sysk.samples.microprocessors.simpleCPU;

import org.junit.Test
import ru.spbstu.sysk.channels.bind
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.samples.microprocessors.simpleCPU.MainConstants.CAPACITY
import ru.spbstu.sysk.samples.microprocessors.simpleCPU.MainConstants.COMMAND
import java.util.*

class SimpleCPUTest : SysTopModule() {

    init {
        var commands: Queue<CPU.Command> = LinkedList()
        commands.add(CPU.Command(COMMAND.PRINT))
        commands.add(CPU.Command(COMMAND.PUSH, SysInteger(CAPACITY.ADDRESS, 16)))
        commands.add(CPU.Command(COMMAND.NEXT))
        commands.add(CPU.Command(COMMAND.PUSH, SysInteger(CAPACITY.ADDRESS, 272)))
        commands.add(CPU.Command(COMMAND.NEXT))
        commands.add(CPU.Command(COMMAND.ADD))
        commands.add(CPU.Command(COMMAND.RESPONSE, SysInteger(CAPACITY.ADDRESS, 16)))
        commands.add(CPU.Command(COMMAND.RESPONSE, SysInteger(CAPACITY.ADDRESS, 272)))
        commands.add(CPU.Command(COMMAND.PRINT))
        commands.add(CPU.Command(COMMAND.PUSH, SysInteger(CAPACITY.ADDRESS, CAPACITY.RAM - 1)))
        commands.add(CPU.Command(COMMAND.RESPONSE, SysInteger(CAPACITY.ADDRESS, CAPACITY.RAM - 1)))
        commands.add(CPU.Command(COMMAND.NEXT))
        commands.add(CPU.Command(COMMAND.PULL, SysInteger(CAPACITY.ADDRESS, 16)))
        commands.add(CPU.Command(COMMAND.PRINT))
        commands.add(CPU.Command(COMMAND.DIV))
        commands.add(CPU.Command(COMMAND.PRINT))
        commands.add(CPU.Command(COMMAND.STOP))
        val ram_1 = RAM(CAPACITY.RAM, CAPACITY.RAM * 0, "RAM#1", this)
        val ram_2 = RAM(CAPACITY.RAM, CAPACITY.RAM * 1, "RAM#2", this)
        val cpu = CPU(commands, 123, 352, "CPU", this)
        val dataBus = bitBus(CAPACITY.DATA, "dataBus")
        val addressBus = bitBus(CAPACITY.ADDRESS, "addressBus")
        val commandBus = bitBus(CAPACITY.COMMAND, "commandBus")
        bind(cpu.dataPort to dataBus, cpu.addressPort to addressBus, cpu.commandPort to commandBus,
                ram_1.dataPort to dataBus, ram_1.addressPort to addressBus, ram_1.commandPort to commandBus,
                ram_2.dataPort to dataBus, ram_2.addressPort to addressBus, ram_2.commandPort to commandBus)
    }

    @Test
    fun show() = start(1(S))
}
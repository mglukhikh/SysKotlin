package ru.spbstu.sysk.samples.microprocessors.simpleCPU

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.integer.SysInteger
import ru.spbstu.sysk.data.integer.SysLongInteger
import ru.spbstu.sysk.samples.microprocessors.simpleCPU.MainConstants.CAPACITY
import ru.spbstu.sysk.samples.microprocessors.simpleCPU.MainConstants.COMMAND

/** This class not describes the operation of the real RAM. He only needed for the test. */
internal class RAM constructor(
        val capacity: Int, val firstAddress: Int, name: String, parent: SysModule
)
: SysModule(name, parent) {

    val dataPort = busPort<SysBit>(CAPACITY.DATA, "data")
    val addressPort = busPort<SysBit>(CAPACITY.ADDRESS, "address")
    val commandPort = busPort<SysBit>(CAPACITY.COMMAND, "command")

    private val memory = Array(capacity, { SysInteger(CAPACITY.DATA, 0) })

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
        for (i in 0..(CAPACITY.DATA - 1)) dataPort(SysBit.Z, i)
        for (i in 0..(CAPACITY.ADDRESS - 1)) addressPort(SysBit.Z, i)
        for (i in 0..(CAPACITY.COMMAND - 1)) commandPort(SysBit.Z, i)
        startDisable
    }

    private val write: (SysWait) -> SysWait = {
        val data = SysInteger(Array(CAPACITY.DATA, { dataPort[it] }))
        val address = SysLongInteger(Array(CAPACITY.ADDRESS, { addressPort[it] }))
        if ((address.value > firstAddress) && (address.value < (firstAddress + capacity))) {
            memory[address.value.toInt() - firstAddress] = data
        }
        startWrite
    }

    private val read: (SysWait) -> SysWait = {
        val address = SysLongInteger(Array(CAPACITY.ADDRESS, { addressPort[it] }))
        if ((address.value >= firstAddress) && (address.value < (firstAddress + capacity))) {
            for (i in 0..(CAPACITY.DATA - 1))
                dataPort(memory[address.value.toInt() - firstAddress][i], i)
        }
        startRead
    }

    private val print: (SysWait) -> SysWait = {
        val address = SysLongInteger(Array(CAPACITY.ADDRESS, { addressPort[it] }))
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
        val command = SysInteger(Array(CAPACITY.COMMAND, { commandPort[it] }))
        when (command) {
            COMMAND.PUSH -> {
                startWrite.happens(waitWrite)
                startEmitUpdate.happens(waitEmitUpdate + waitWrite)
                startUpdate
            }
            COMMAND.PULL -> {
                startRead.happens(waitRead)
                startDisable.happens(waitRead + CPU.waitSave + waitDisable)
                startEmitUpdate.happens(waitRead + CPU.waitSave + waitDisable + waitEmitUpdate)
                startUpdate
            }
            COMMAND.RESPONSE -> {
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
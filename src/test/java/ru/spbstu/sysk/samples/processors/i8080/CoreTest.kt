package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Ignore
import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.unsigned
import ru.spbstu.sysk.data.SysBit.*

class CoreTest : SysTopModule() {

    private val core = Core(this)
    private val ram = memory("memory", CAPACITY.ADDRESS, unsigned(CAPACITY.DATA, 0))

    private val data = signal("data", unsigned(CAPACITY.DATA, 0))
    private val address = signal("address", unsigned(CAPACITY.ADDRESS, 0))
    private val clk1 = clock("clock", 20(FS))
    private val clk2 = clock("clock", 2(FS))
    private val reset = bitSignal("reset")
    private val wr = bitSignal("wr")
    private val dbin = bitSignal("dbin")
    private val enRAM = bitSignal("enRAM")
    private val wrRAM = bitSignal("wrRAM")

    init {
        core.data bind data
        core.address bind address
        core.reset bind reset
        core.clk1 bind clk1
        core.clk2 bind clk2
        core.wr bind wr
        core.dbin bind dbin
        ram.din bind data
        ram.dout bind data
        ram.addr bind address
        ram.clk bind clk1
        ram.en bind enRAM
        ram.wr bind wrRAM

        ram.load {
            when (it) {
                0L -> OPERATION.ADD_C.toSysUnsigned()
                1L -> OPERATION.ADD_D.toSysUnsigned()
                2L -> OPERATION.ADC_B.toSysUnsigned()
                3L -> OPERATION.INR_E.toSysUnsigned()
                4L -> OPERATION.MOV_B_E.toSysUnsigned()
                5L -> OPERATION.MOV_B_L.toSysUnsigned()
                6L -> OPERATION.ADC_A.toSysUnsigned()
                7L -> OPERATION.ADD_H.toSysUnsigned()
                8L -> OPERATION.ORA_L.toSysUnsigned()
                else -> OPERATION.NOP.toSysUnsigned()
            }
        }

        enRAM(ZERO)
        wrRAM(ZERO)
        function(wr.defaultEvent or dbin.defaultEvent) {
            if (wr.one && !dbin.one) {
                enRAM(ONE)
                wr(ONE)
            } else if (!wr.one && dbin.one) {
                enRAM(ONE)
                wr(ZERO)
            } else enRAM(ZERO)
        }

        stateFunction(clk1) {
            sleep(50)
            stop(scheduler)
        }
    }

    @Test
    fun show() = start()
}
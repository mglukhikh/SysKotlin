package ru.spbstu.sysk.samples.processors.i8080

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
        ram.wr bind wr

        ram.load {
            when (it) {
                0L -> OPERATION.ADD_C()
                1L -> OPERATION.ADD_D()
                2L -> OPERATION.ADC_B()
                3L -> OPERATION.MVI_B_d8()
                4L -> unsigned(CAPACITY.DATA, 123)
                5L -> OPERATION.MOV_B_M()
                6L -> OPERATION.ADC_A()
                7L -> OPERATION.ADD_H()
                8L -> OPERATION.ORA_L()
                else -> OPERATION.NOP()
            }
        }

        enRAM(ZERO)

        function(wr.defaultEvent or dbin.defaultEvent) {
            enRAM(wr.value xor dbin.value)
        }

        stateFunction(clk1) {
            sleep(50)
            stop(scheduler)
        }
    }

    @Test
    fun show() = start()
}
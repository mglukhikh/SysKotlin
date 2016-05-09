package ru.spbstu.sysk.samples.processors.i8080

import org.junit.Test
import ru.spbstu.sysk.core.SysTopModule
import ru.spbstu.sysk.core.TimeUnit.*
import ru.spbstu.sysk.core.invoke
import ru.spbstu.sysk.data.integer.unsigned
import ru.spbstu.sysk.data.SysBit.*
import ru.spbstu.sysk.samples.processors.i8080.OPERATION.*

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
                0L -> MVI_D_d8()
                1L -> unsigned(CAPACITY.DATA, 12)
                2L -> MVI_E_d8()
                3L -> unsigned(CAPACITY.DATA, 25)
                4L -> MOV_E_A()
                5L -> ADD_D()
                6L -> MOV_A_E()
                else -> NOP()
            }
        }

        enRAM(ZERO)

        function(wr.defaultEvent or dbin.defaultEvent, false) {
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
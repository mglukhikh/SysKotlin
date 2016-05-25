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
    private val ram = unsignedMemory("memory", CAPACITY.ADDRESS, CAPACITY.DATA)

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
            var i = -1L
            when (it) {
                ++i -> MVI_D_d8()
                ++i -> unsigned(CAPACITY.DATA, 12)
                ++i -> MVI_E_d8()
                ++i -> unsigned(CAPACITY.DATA, 25)
                ++i -> MOV_E_A()
                ++i -> ADD_D()
                ++i -> MOV_A_E()
                ++i -> MOV_E_H()
                ++i -> SUI_d8()
                ++i -> unsigned(CAPACITY.DATA, 24)

                ++i -> STA_a16()
                ++i -> unsigned(CAPACITY.DATA, 0)
                ++i -> unsigned(CAPACITY.DATA, 255)

                ++i -> STA_a16()
                ++i -> unsigned(CAPACITY.DATA, 40)
                ++i -> unsigned(CAPACITY.DATA, 40)
                ++i -> MVI_A_d8()
                ++i -> unsigned(CAPACITY.DATA, 123)
                ++i -> LDA_a16()
                ++i -> unsigned(CAPACITY.DATA, 40)
                ++i -> unsigned(CAPACITY.DATA, 40)
                ++i -> MVI_H_d8()
                ++i -> unsigned(CAPACITY.DATA, 1)
                ++i -> MVI_L_d8()
                ++i -> unsigned(CAPACITY.DATA, 211)
                ++i -> SPHL()
                ++i -> MVI_H_d8()
                ++i -> unsigned(CAPACITY.DATA, 8)
                ++i -> MVI_L_d8()
                ++i -> unsigned(CAPACITY.DATA, 138)
                ++i -> DAD_SP()

                ++i -> MOV_H_A()
                ++i -> STA_a16()
                ++i -> unsigned(CAPACITY.DATA, 1)
                ++i -> unsigned(CAPACITY.DATA, 255)
                ++i -> MOV_L_A()
                ++i -> STA_a16()
                ++i -> unsigned(CAPACITY.DATA, 2)
                ++i -> unsigned(CAPACITY.DATA, 255)

                ++i -> INX_H()
                ++i -> INX_H()
                ++i -> INX_H()
                ++i -> DCR_H()
                ++i -> MOV_H_A()
                ++i -> STA_a16()
                ++i -> unsigned(CAPACITY.DATA, 3)
                ++i -> unsigned(CAPACITY.DATA, 255)
                ++i -> MOV_L_A()
                ++i -> STA_a16()
                ++i -> unsigned(CAPACITY.DATA, 4)
                ++i -> unsigned(CAPACITY.DATA, 255)

                ++i -> MVI_A_d8()
                ++i -> unsigned(CAPACITY.DATA, 147)
                ++i -> MVI_B_d8()
                ++i -> unsigned(CAPACITY.DATA, 121)
                ++i -> ADD_B()
                ++i -> RAL()
                ++i -> STA_a16()
                ++i -> unsigned(CAPACITY.DATA, 5)
                ++i -> unsigned(CAPACITY.DATA, 255)
                else -> NOP()
            }
        }

        enRAM(ZERO)

        function(wr.defaultEvent or dbin.defaultEvent, false) {
            enRAM(wr.value xor dbin.value)
        }

        stateFunction(clk1) {
            sleep(250)
            state {
                val mask = unsigned(CAPACITY.ADDRESS, 255) shl 8
                assert(ram[mask + 0].toInt() == 13)
                assert(ram[mask + 2].toInt() + (ram[mask + 1].toInt() shl 8) == 2653)
                assert(ram[mask + 4].toInt() + (ram[mask + 3].toInt() shl 8) == 2400)
                assert(ram[mask + 5].toInt() == 25)
                scheduler.stop()
            }
        }
    }

    @Test
    fun show() = start()
}
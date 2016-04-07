package ru.spbstu.sysk.samples.processors.simpleCPU

import ru.spbstu.sysk.data.integer.SysInteger

object MainConstants {
    object CAPACITY {
        val DATA = 32
        val ADDRESS = 32
        val COMMAND = 5
        val RAM = 256
        val REGISTER = 2
    }

    object COMMAND {
        val NULL = SysInteger(CAPACITY.COMMAND, 0)
        val ADD = SysInteger(CAPACITY.COMMAND, 1)
        val SUB = SysInteger(CAPACITY.COMMAND, 2)
        val MUL = SysInteger(CAPACITY.COMMAND, 3)
        val DIV = SysInteger(CAPACITY.COMMAND, 4)
        val REM = SysInteger(CAPACITY.COMMAND, 5)
        val PUSH = SysInteger(CAPACITY.COMMAND, 6)
        val PULL = SysInteger(CAPACITY.COMMAND, 7)
        val NEXT = SysInteger(CAPACITY.COMMAND, 8)
        val PRINT = SysInteger(CAPACITY.COMMAND, 9)
        val RESPONSE = SysInteger(CAPACITY.COMMAND, 10)
        val STOP = SysInteger(CAPACITY.COMMAND, 11)
    }
}






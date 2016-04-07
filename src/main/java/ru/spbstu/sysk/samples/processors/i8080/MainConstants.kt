package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.data.integer.unsigned

object MainConstants {
    object OPERATION {
        val ADD = unsigned(3, 0)
        val SUB = unsigned(3, 1)
        val MUL = unsigned(3, 2)
        val DIV = unsigned(3, 3)
        val REM = unsigned(3, 4)
        val SHL = unsigned(3, 5)
        val SHR = unsigned(3, 6)
    }

    object CAPACITY {
        val DATA = 8
        val ADDRESS = 16
    }

    object VALUE {
        val MAX_DATA = 255
        val MAX_ADDRESS = 65535
    }

    object COMMAND {
        val STORAGE = unsigned(3, 0)
        val WRITE = unsigned(3, 1)
        val READ = unsigned(3, 2)
        val INC = unsigned(3, 3)
        val DEC = unsigned(3, 4)
        val SHL = unsigned(3, 5)
        val SHR = unsigned(3, 6)
        val RESET = unsigned(3, 7)

        val READ_FRONT = unsigned(3, 1)
        val READ_BACK = unsigned(3, 2)
        val WRITE_FRONT = unsigned(3, 3)
        val WRITE_BACK = unsigned(3, 4)
    }

    object REGISTER {
        val A = unsigned(4, 0)
        val Flag = unsigned(4, 1)
        val B = unsigned(4, 2)
        val C = unsigned(4, 3)
        val D = unsigned(4, 4)
        val E = unsigned(4, 5)
        val H = unsigned(4, 6)
        val L = unsigned(4, 7)

        val PSW = unsigned(4, 0)
        val BC = unsigned(4, 2)
        val DE = unsigned(4, 4)
        val HL = unsigned(4, 6)
        val PC = unsigned(4, 8)
        val SP = unsigned(4, 10)
    }
}

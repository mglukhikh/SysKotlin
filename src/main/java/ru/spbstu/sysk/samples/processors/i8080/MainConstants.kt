package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.data.integer.unsigned

object MainConstants {
    object CAPACITY {
        const val DATA = 8
        const val ADDRESS = 16
        const val COMMAND = 3
        const val REGISTER = 4
    }

    object VALUE {
        val MAX_DATA = 255
        val MAX_ADDRESS = 65535
    }

    object COMMAND {
        val UNDEFINED = unsigned(CAPACITY.COMMAND, 0)
        val WRITE = unsigned(CAPACITY.COMMAND, 1)
        val READ = unsigned(CAPACITY.COMMAND, 2)
        val INC = unsigned(CAPACITY.COMMAND, 3)
        val DEC = unsigned(CAPACITY.COMMAND, 4)
        val SHL = unsigned(CAPACITY.COMMAND, 5)
        val SHR = unsigned(CAPACITY.COMMAND, 6)
        val RESET = unsigned(CAPACITY.COMMAND, 7)

        val READ_FRONT = unsigned(CAPACITY.COMMAND, 1)
        val READ_BACK = unsigned(CAPACITY.COMMAND, 2)
        val WRITE_FRONT = unsigned(CAPACITY.COMMAND, 3)
        val WRITE_BACK = unsigned(CAPACITY.COMMAND, 4)

        val ADD = unsigned(CAPACITY.COMMAND, 1)
        val SUB = unsigned(CAPACITY.COMMAND, 2)
        val MUL = unsigned(CAPACITY.COMMAND, 3)
        val DIV = unsigned(CAPACITY.COMMAND, 4)
        /** val SHL = unsigned(CAPACITY.COMMAND, 5) */
        /** val SHR = unsigned(CAPACITY.COMMAND, 6) */
        val REM = unsigned(CAPACITY.COMMAND, 7)
    }

    object REGISTER {
        val UNDEFINED = unsigned(CAPACITY.REGISTER, 0)
        val A = unsigned(CAPACITY.REGISTER, 1)
        val Flag = unsigned(CAPACITY.REGISTER, 2)
        val B = unsigned(CAPACITY.REGISTER, 3)
        val C = unsigned(CAPACITY.REGISTER, 4)
        val D = unsigned(CAPACITY.REGISTER, 5)
        val E = unsigned(CAPACITY.REGISTER, 6)
        val H = unsigned(CAPACITY.REGISTER, 7)
        val L = unsigned(CAPACITY.REGISTER, 8)

        val PSW = unsigned(CAPACITY.REGISTER, 1)
        val BC = unsigned(CAPACITY.REGISTER, 3)
        val DE = unsigned(CAPACITY.REGISTER, 4)
        val HL = unsigned(CAPACITY.REGISTER, 7)
        val PC = unsigned(CAPACITY.REGISTER, 9)
        val SP = unsigned(CAPACITY.REGISTER, 11)
    }
}

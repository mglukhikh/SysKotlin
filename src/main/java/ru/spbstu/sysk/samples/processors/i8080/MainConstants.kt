package ru.spbstu.sysk.samples.processors.i8080

import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysDataCompanion

object CAPACITY {
    const val DATA = 8
    const val ADDRESS = 16
    const val COMMAND = 3
    const val REGISTER = 4
}

object VALUE {
    const val MAX_DATA = 255
    const val MAX_ADDRESS = 65535
}

enum class COMMAND : SysData {

    UNDEFINED,
    WRITE,
    READ,
    RESET,

    READ_FRONT,
    READ_BACK,
    WRITE_FRONT,
    WRITE_BACK,

    ADD,
    SUB,
    MUL,
    DIV,
    INC,
    DEC,
    SHL,
    SHR,
    REM;

    val width = CAPACITY.COMMAND

    companion object : SysDataCompanion<COMMAND> {
        override val undefined: COMMAND
            get() = COMMAND.UNDEFINED
    }
}

enum class REGISTER : SysData {

    UNDEFINED,
    A, Flag,
    PSW,
    B, C,
    BC,
    D, E,
    DE,
    H, L,
    HL,
    PC,
    SP;

    val width = CAPACITY.REGISTER

    companion object : SysDataCompanion<REGISTER> {
        override val undefined: REGISTER
            get() = REGISTER.UNDEFINED
    }
}

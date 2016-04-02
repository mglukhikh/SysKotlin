package ru.spbstu.sysk.samples.stack

import ru.spbstu.sysk.data.SysData
import ru.spbstu.sysk.data.SysDataCompanion

enum class Opcode : SysData {
    NOP,
    PUSH,
    POP,
    PLUS,
    MINUS,
    TIMES,
    DIV;

    companion object : SysDataCompanion<Opcode> {
        override val undefined = NOP
    }
}
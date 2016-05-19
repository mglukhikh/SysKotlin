package ru.spbstu.sysk.samples.processors.simpleCPU

import ru.spbstu.sysk.data.integer.integer

enum class Command(index: Int) {
    NULL(0),
    ADD(1),
    SUB(2),
    MUL(3),
    DIV(4),
    REM(5),
    PUSH(6),
    PULL(7),
    NEXT(8),
    PRINT(9),
    RESPONSE(10),
    STOP(11);

    val value = integer(Capacity.COMMAND, index)

    operator fun get(i: Int) = value[i]
}
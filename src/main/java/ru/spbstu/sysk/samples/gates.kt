package ru.spbstu.sysk.samples

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.generics.SysBinaryBitModule
import ru.spbstu.sysk.generics.SysUnaryBitModule


class NOT(name: String, parent: SysModule) :
        SysUnaryBitModule<SysBit>({ !it }, name, parent)

class AND(name: String, parent: SysModule) :
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1 and x2 }, name, parent)

class NAND(name: String, parent: SysModule) :
        SysBinaryBitModule<SysBit>({ x1, x2 -> !(x1 and x2) }, name, parent)

class OR(name: String, parent: SysModule) :
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1 or x2 }, name, parent)

class NOR(name: String, parent: SysModule) :
        SysBinaryBitModule<SysBit>({ x1, x2 -> !(x1 or x2) }, name, parent)

class XOR(name: String, parent: SysModule) :
        SysBinaryBitModule<SysBit>({ x1, x2 -> x1 xor x2 }, name, parent)

class NXOR(name: String, parent: SysModule) :
        SysBinaryBitModule<SysBit>({ x1, x2 -> !(x1 xor x2) }, name, parent)

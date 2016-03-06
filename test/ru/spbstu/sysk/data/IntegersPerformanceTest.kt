package ru.spbstu.sysk.data


import org.junit.Test
import java.util.*

class IntegersPerformanceTest {

    @Test
    fun speedTest() {
        val size = 10000L
        val rand = Random(100)
        val longArr = rand.longs(size).toArray()
        val intArr = rand.ints(size).toArray()
        var a: SysInteger
        var b: SysInteger
        var x: SysBigInteger
        var z: SysBigInteger
        var result: Long

        var startTime = System.currentTimeMillis()
        for (i in longArr.indices) {
            a = SysInteger(64, longArr[i])
            b = SysInteger(64, longArr[longArr.lastIndex - i])

            a + b
            a - b
            a / b
            a * b
            a % b

        }
        result = System.currentTimeMillis() - startTime

        println("SysInteger    width 64   " + result / 1000 + "s" + result % 1000 + "ms")

        startTime = System.currentTimeMillis()
        for (i in longArr.indices) {
            x = SysBigInteger(64, longArr[i])
            z = SysBigInteger(64, longArr[longArr.lastIndex - i])

            x + z
            x - z
            x / z
            x * z
            x % z

        }
        result = System.currentTimeMillis() - startTime

        println("SysBigInteger width 64   " + result / 1000 + "s" + result % 1000 + "ms")
        startTime = System.currentTimeMillis()
        for (i in intArr.indices) {
            a = SysInteger(32, intArr[i])
            b = SysInteger(32, intArr[intArr.lastIndex - i])

            a + b
            a - b
            a / b
            a * b
            a % b

        }
        result = System.currentTimeMillis() - startTime

        println("SysInteger    width 32   " + result / 1000 + "s" + result % 1000 + "ms")

        startTime = System.currentTimeMillis()
        for (i in intArr.indices) {
            x = SysBigInteger(32, intArr[i])
            z = SysBigInteger(32, intArr[intArr.lastIndex - i])

            x + z
            x - z
            x / z
            x * z
            x % z

        }
        result = System.currentTimeMillis() - startTime

        println("SysBigInteger width 32   " + result / 1000 + "s" + result % 1000 + "ms")

    }


}
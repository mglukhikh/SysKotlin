package ru.spbstu.sysk.data


import org.junit.Ignore
import org.junit.Test
import java.util.*

class IntegersPerformanceTest {
    @Ignore("Ignored IntegerPerformanceTest")
    @Test
    fun speedTest() {
        val size = 10000L
        val rand = Random(100)
        val longArr = rand.longs(size).toArray()
        val intArr = rand.ints(size).toArray()
        var result: Long

        var startTime = System.currentTimeMillis()
        for (i in longArr.indices) {
            val a = SysLongInteger(64, longArr[i])
            val b = SysLongInteger(64, longArr[longArr.lastIndex - i])

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
            val a = SysBigInteger(64, longArr[i])
            val b = SysBigInteger(64, longArr[longArr.lastIndex - i])

            a + b
            a - b
            a / b
            a * b
            a % b

        }
        result = System.currentTimeMillis() - startTime

        println("SysBigInteger width 64   " + result / 1000 + "s" + result % 1000 + "ms")
        startTime = System.currentTimeMillis()
        for (i in intArr.indices) {
            val a = SysLongInteger(32, intArr[i])
            val b = SysLongInteger(32, intArr[intArr.lastIndex - i])

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
            val a = SysBigInteger(32, intArr[i])
            val b = SysBigInteger(32, intArr[intArr.lastIndex - i])

            a + b
            a - b
            a / b
            a * b
            a % b

        }
        result = System.currentTimeMillis() - startTime

        println("SysBigInteger width 32   " + result / 1000 + "s" + result % 1000 + "ms")

    }


}
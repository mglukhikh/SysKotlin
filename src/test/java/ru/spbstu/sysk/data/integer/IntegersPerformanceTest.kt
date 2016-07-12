package ru.spbstu.sysk.data.integer


import org.junit.Ignore
import org.junit.Test
import java.util.*

class IntegersPerformanceTest {
    @Ignore("Ignored IntegerPerformanceTest")
    @Test
    fun speedTest() {
        val size = 10000L
        val rand = Random(100)
        val longArr = rand.longs(size).filter { it >= 0L }.toArray()
        val intArr = rand.ints(size).filter { it >= 0L }.toArray()
        var result: Long

        var startTime = System.currentTimeMillis()
        for (i in longArr.indices) {
            val a = SysLongInteger.valueOf(64, longArr[i])
            val b = SysLongInteger.valueOf(64, longArr[longArr.lastIndex - i])

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
            val a = SysBigInteger.valueOf(64, longArr[i])
            val b = SysBigInteger.valueOf(64, longArr[longArr.lastIndex - i])

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
            val a = SysLongInteger.valueOf(32, intArr[i].toLong())
            val b = SysLongInteger.valueOf(32, intArr[intArr.lastIndex - i].toLong())

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
            val a = SysBigInteger.valueOf(32, intArr[i].toLong())
            val b = SysBigInteger.valueOf(32, intArr[intArr.lastIndex - i].toLong())

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
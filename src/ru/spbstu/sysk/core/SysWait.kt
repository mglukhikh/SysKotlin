package ru.spbstu.sysk.core

enum class TimeUnit(val femtoSeconds: Long) {
    FS(1),
    PS(1000),
    NS(1000000),
    US(1000000000),
    MS(1000000000000),
    S(1000000000000000);

    override fun toString() = name.toLowerCase()
}

sealed class SysWait {

    /** Special: something that never happens */
    object Never: SysWait() {

    }

    /** Special: initialization event that happens on start */
    object Initialize: SysWait() {

    }

    class Event internal constructor(
            val name: String, val scheduler: SysScheduler
    ): SysWait() {

        init {
            scheduler.register(this)
        }

        // Unfortunately notify() is in use in Java
        fun happens(delay: SysWait.Time = SysWait.Time(0)) {
            scheduler.happens(this, delay)
        }
    }

    abstract class Finder: SysWait() {
        abstract operator fun invoke(): Event?
    }

    class Time(val femtoSeconds: Long): SysWait(), Comparable<Time> {
        override fun compareTo(other: Time): Int =
                if (femtoSeconds > other.femtoSeconds) 1
                else if (femtoSeconds < other.femtoSeconds) -1
                else 0

        constructor(num: Int, tu: TimeUnit): this(tu.femtoSeconds * num)

        operator fun plus(other: Time) = Time(femtoSeconds + other.femtoSeconds)

        operator fun div(arg: Int) = Time(femtoSeconds / arg)

        override fun equals(other: Any?) = femtoSeconds == (other as? Time)?.femtoSeconds

        override fun hashCode() = femtoSeconds.toInt()

        override fun toString() = "'$femtoSeconds FS'"

        companion object {
            val INFINITY: Time = Time(Long.MAX_VALUE)
        }
    }

    class OneOf(val elements: List<SysWait>): SysWait() {
        constructor(vararg elements: SysWait): this(elements.toList())

        override fun or(other: SysWait): OneOf = OneOf(elements + other)

        override fun equals(other: Any?) = elements == (other as? OneOf)?.elements

        override fun hashCode() = elements.hashCode()
    }

    open fun or(other: SysWait): SysWait = if (other is OneOf) other.or(this) else OneOf(this, other)

    companion object {
        fun reduce(elements: List<SysWait>): SysWait {
            if (elements.isEmpty()) {
                return Never
            }
            else if (elements.size == 1) {
                return elements.first()
            }
            else {
                return SysWait.OneOf(elements)
            }
        }
    }
}

fun time(num: Int, tu: TimeUnit) = SysWait.Time(num, tu)
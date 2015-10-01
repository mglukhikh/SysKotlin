package sysk

enum class TimeUnit(val femtoSeconds: Long, val name: String) {
    FS(1, "fs"),
    PS(1000, "ps"),
    NS(1000000, "ns"),
    US(1000000000, "us"),
    MS(1000000000000, "ms"),
     S(1000000000000000, "s");

    override fun toString() = name
}

interface SysWait {

    /** Special: something that never happens */
    object Never: SysWait {

    }

    class Event internal constructor(name: String, parent: SysObject? = null): SysObject(name, parent), SysWait {

        init {
            SysScheduler.register(this)
        }

        // Unfortunately notify() is in use in Java
        fun happens() {
            SysScheduler.happens(this)
        }
    }

    interface Finder: SysWait {
        fun invoke(): Event?
    }

    data class Time(val femtoSeconds: Long): SysWait, Comparable<Time> {
        override fun compareTo(other: Time): Int =
                if (femtoSeconds > other.femtoSeconds) 1
                else if (femtoSeconds < other.femtoSeconds) -1
                else 0

        constructor(num: Int, tu: TimeUnit): this(tu.femtoSeconds * num)

        fun plus(other: Time): Time = Time(femtoSeconds + other.femtoSeconds)

        fun numUnits(tu: TimeUnit): Double = femtoSeconds / tu.femtoSeconds.toDouble()

        companion object {
            val INFINITY: Time = Time(Long.MAX_VALUE)
        }
    }

    data class OneOf(public val elements: List<SysWait>): SysWait {
        constructor(vararg elements: SysWait): this(elements.toList())

        override fun or(other: SysWait): OneOf = OneOf(elements + other)
    }

    open fun or(other: SysWait): SysWait = if (other is OneOf) other.or(this) else OneOf(this, other)

    companion object {
        fun reduce(elements: List<SysWait>): SysWait {
            if (elements.isEmpty()) {
                return Never
            }
            else if (elements.size() == 1) {
                return elements.first()
            }
            else {
                return SysWait.OneOf(elements)
            }
        }
    }
}

fun time(num: Int, tu: TimeUnit) = SysWait.Time(num, tu)
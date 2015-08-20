package sysk

public enum class TimeUnit(public val femtoSeconds: Long, public val name: String) {
    FS(1, "fs"),
    PS(1000, "ps"),
    NS(1000000, "ns"),
    US(1000000000, "us"),
    MS(1000000000000, "ms"),
     S(1000000000000000, "s");

    override fun toString() = name
}

public interface SysWait {

    /** Special: something that never happens */
    public object Never: SysWait {

    }

    internal class Event(name: String, parent: SysObject? = null): SysObject(name, parent), SysWait {

        init {
            SysScheduler.register(this)
        }

        // Unfortunately notify() is in use in Java
        fun happens() {
            SysScheduler.happens(this)
        }
    }

    internal interface Finder: SysWait {
        fun invoke(): Event?
    }

    public data class Time(public val femtoSeconds: Long): SysWait, Comparable<Time> {
        override fun compareTo(other: Time): Int =
                if (femtoSeconds > other.femtoSeconds) 1
                else if (femtoSeconds < other.femtoSeconds) -1
                else 0

        public constructor(num: Int, tu: TimeUnit): this(tu.femtoSeconds * num)

        public fun plus(other: Time): Time = Time(femtoSeconds + other.femtoSeconds)

        public fun numUnits(tu: TimeUnit): Double = femtoSeconds / tu.femtoSeconds.toDouble()

        companion object {
            public val INFINITY: Time = Time(Long.MAX_VALUE)
        }
    }

    public data class OneOf(public val elements: List<SysWait>): SysWait {
        public constructor(vararg elements: SysWait): this(elements.toList())

        override public fun or(other: SysWait): OneOf = OneOf(elements + other)
    }

    open public fun or(other: SysWait): SysWait = if (other is OneOf) other.or(this) else OneOf(this, other)

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
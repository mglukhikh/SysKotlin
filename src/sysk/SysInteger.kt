package sysk

/** Width of integer / unsigned / ... */
public annotation class width(val value: Int)

/**
 * Immutable class for a fixed-width integer, width can be from 0 to 64, inclusively.
 * Value is stored in a long integer.
 * Width can be checked statically (in future) if type usage is annotated by @width annotation
 */
public class SysInteger(public val width: Int, public val value: Long) {
    init {
        if (!checkWidth()) {
            throw IllegalArgumentException()
        }
        if (minValue() > value || value > maxValue()) {
            throw IllegalArgumentException()
        }
    }

    /** Construct from given long value setting minimal possible width */
    public constructor(value: Long): this(widthByValue(value), value)

    /** Construct from given int value setting maximal possible width */
    public constructor(value: Int): this(widthByValue(value.toLong()), value.toLong())

    /** Increase width to the given */
    public fun extend(width: Int): SysInteger = SysInteger(width, value)

    /** Decrease width to the given with value truncation */
    public fun truncate(width: Int): SysInteger {
        val widthByValue = widthByValue(value)
        if (width >= widthByValue) return extend(width)
        if (width < 0) throw IllegalArgumentException()
        val truncated = value shr (widthByValue - width)
        return SysInteger(width, truncated)
    }

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    public fun plus(arg: SysInteger): SysInteger {
        val resWidth = Math.max(width, arg.width)
        return SysInteger(Math.max(resWidth + 1, MAX_WIDTH), value + arg.value).truncate(resWidth)
    }

    /** Multiplies arg to this integer, with result width is sum of argument's width */
    public fun times(arg: SysInteger): SysInteger {
        return SysInteger(width + arg.width, value * arg.value)
    }

    /** Extracts a single bit, accessible as [i] */
    public fun get(i: Int): Boolean {
        if (i < 0 || i >= width) throw IndexOutOfBoundsException()
        return (value and (1L shl i)) != 0L
    }

    /** Extracts a range of bits, accessible as [j,i] */
    public fun get(j: Int, i: Int): SysInteger {
        if (j < i) throw IllegalArgumentException()
        if (j >= width || i < 0) throw IndexOutOfBoundsException()
        var result = value shr i
        return SysInteger(result).truncate(j - i + 1)
    }

    override public fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? SysInteger)?.let{ width == it.width && value == it.value } ?: false
    }

    override public fun hashCode(): Int {
        var result = 13
        result += 19 * value.hashCode()
        result += 19 * width
        return result
    }

    override public fun toString(): String {
        return "$value[$width]"
    }

    private fun minValue(): Long {
        if (width == 0) return 0
        if (width == MAX_WIDTH) return Long.MIN_VALUE;
        return - (1L shl (width - 1))
    }

    private fun maxValue(): Long {
        if (width == 0) return 0
        if (width == MAX_WIDTH) return Long.MAX_VALUE
        return (1L shl (width - 1)) - 1
    }

    private fun checkWidth(): Boolean {
        if (width < 0 || width > MAX_WIDTH) return false
        return true
    }

    companion object {

        public val MAX_WIDTH: Int = 64

        private fun widthByValue(value: Long): Int {
            var result = 1
            var current = value
            while (current > 0 || current < -1) {
                result++
                current = current shr 1
            }
            return result
        }
    }
}

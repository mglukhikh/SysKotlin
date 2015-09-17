package sysk

import org.junit.Test

public class SysIntegerTest {

    @Test
    fun first() {
        val x: @Width(4) SysInteger = SysInteger(5)
        val y: @Width(6) SysInteger = SysInteger(9).extend(6)
        assert(y.width == 6)
        val z: @Width(6) SysInteger = x + y
        assert(z.width == 6)
        assert(z == SysInteger(6, 14), { z })
        assert(z[2])
        assert(z[4,1] == SysInteger(7))
        val v: @Width(12) SysInteger = y * z
        assert(v.width == 12)
        assert(v == SysInteger(12, 126))
    }
}
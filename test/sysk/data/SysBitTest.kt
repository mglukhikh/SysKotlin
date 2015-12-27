package sysk.data

import org.junit.Test

public class SysBitTest {

    @Test
    fun base() {
        assert(SysBit.ZERO.zero)
        assert(SysBit.ONE.one)
        assert(SysBit.X.x)
    }

    @Test
    fun notTest() {
        assert((!SysBit.ZERO).one)
        assert((!SysBit.ONE).zero)
        assert((!SysBit.X).x)
        assert((!SysBit.Z).z)
    }

    @Test
    fun or() {
        for (arg in SysBit.values()) {
            assert(SysBit.ONE.or(arg).one)
            assert(arg.or(SysBit.ONE).one)
            if (!arg.one) {
                assert(SysBit.X.or(arg).x)
                assert(arg.or(SysBit.X).x)
            }
        }
        assert(SysBit.ZERO.or(SysBit.ZERO).zero)
    }


    @Test
    fun and() {
        for (arg in SysBit.values()) {
            assert(SysBit.ZERO.and(arg).zero)
            assert(arg.and(SysBit.ZERO).zero)
            if (!arg.zero) {
                assert(SysBit.X.and(arg).x)
                assert(arg.and(SysBit.X).x)
            }
        }
        assert(SysBit.ONE.and(SysBit.ONE).one)
    }

    @Test
    fun wiredAnd() {
        for (arg in SysBit.values()) {
            assert(SysBit.Z.wiredAnd(arg) == arg)
            assert(arg.wiredAnd(SysBit.Z) == arg)
        }
        assert(SysBit.ONE.wiredAnd(SysBit.ONE).one)
        assert(SysBit.ZERO.wiredAnd(SysBit.ZERO).zero)
    }
}
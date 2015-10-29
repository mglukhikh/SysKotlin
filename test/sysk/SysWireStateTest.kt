package sysk

import org.junit.Test

public class SysWireStateTest {

    @Test
    fun base() {
        assert(SysWireState.ZERO.zero)
        assert(SysWireState.ONE.one)
        assert(SysWireState.X.x)
    }

    @Test
    fun notTest() {
        assert((!SysWireState.ZERO).one)
        assert((!SysWireState.ONE).zero)
        assert((!SysWireState.X).x)
        assert((!SysWireState.Z).z)
    }

    @Test
    fun or() {
        for (arg in SysWireState.values) {
            assert(SysWireState.ONE.or(arg).one)
            assert(arg.or(SysWireState.ONE).one)
            if (!arg.one) {
                assert(SysWireState.X.or(arg).x)
                assert(arg.or(SysWireState.X).x)
            }
        }
        assert(SysWireState.ZERO.or(SysWireState.ZERO).zero)
    }


    @Test
    fun and() {
        for (arg in SysWireState.values) {
            assert(SysWireState.ZERO.and(arg).zero)
            assert(arg.and(SysWireState.ZERO).zero)
            if (!arg.zero) {
                assert(SysWireState.X.and(arg).x)
                assert(arg.and(SysWireState.X).x)
            }
        }
        assert(SysWireState.ONE.and(SysWireState.ONE).one)
    }

    @Test
    fun wiredAnd() {
        for (arg in SysWireState.values) {
            assert(SysWireState.Z.wiredAnd(arg) == arg)
            assert(arg.wiredAnd(SysWireState.Z) == arg)
        }
        assert(SysWireState.ONE.wiredAnd(SysWireState.ONE).one)
        assert(SysWireState.ZERO.wiredAnd(SysWireState.ZERO).zero)
    }
}
package ru.spbstu.sysk.data

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysWait
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SysPort<IF : SysInterface> internal constructor(
        name: String, private val scheduler: SysScheduler, parent: SysObject? = null, sysInterface: IF? = null
) : SysObject(name, parent) {

    init {
        scheduler.register(this)
    }

    protected var bound: IF? = null
        private set

    val isBound: Boolean
        get() = (bound != null)

    var sealed: Boolean = false
        private set

    fun seal() {
        assert(scheduler.stopRequested) { "Impossible to seal the port while running the scheduler" }
        assert(!isBound) { "Port $name is already bound to $bound" }
        sealed = true
    }

    init {
        sysInterface?.let { bind(it) }
    }

    fun bind(port: SysPort<IF>): Unit = bind(port())

    infix fun bind(sysInterface: IF) {
        assert(scheduler.stopRequested) { "Impossible to bind the port while running the scheduler" }
        assert(!isBound) { "Port $name is already bound to $bound" }
        assert(!sealed) { "Port $name is already sealed"}
        bound = sysInterface
        sysInterface.register(this)
    }

    fun to(port: SysPort<IF>): Pair<SysPort<IF>, IF> = Pair(this, port())

    fun to(sysInterface: IF): Pair<SysPort<IF>, IF> = Pair(this, sysInterface)

    open operator fun invoke(): IF {
        assert(isBound) { "Port $name is not bound" }
        return bound!!
    }

    val defaultEvent: SysWait.Finder = object : SysWait.Finder() {
        override operator fun invoke() = this@SysPort().defaultEvent
    }

}

fun <IF : SysInterface> bind(vararg pairs: Pair<SysPort<IF>, IF>) {
    for (pair in pairs) {
        pair.first.bind(pair.second)
    }
}

fun <IF : SysInterface> bindArrays(vararg pairs: Pair<Array<out SysPort<IF>>, Array<out IF>>) {
    for (pair in pairs) {
        pair.first.forEachIndexed { i, sysPort -> sysPort.bind(pair.second[i]) }
    }
}

open class SysInput<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, signalRead: SysSignalRead<T>? = null, private val defaultValue: T? = null
) : SysPort<SysSignalRead<T>>(name, scheduler, parent, signalRead) {

    val value: T
        get() {
            if (isBound) {
                return bound!!.value
            } else {
                if (defaultValue == null) throw IllegalStateException("Port $name is not bound")
                return defaultValue
            }
        }
}

class SysBitInput internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, signalRead: SysBitRead? = null
) : SysInput<SysBit>(name, scheduler, parent, signalRead), SysEdged {

    override val posEdgeEvent: SysWait.Finder = object : SysWait.Finder() {
        override fun invoke() = this@SysBitInput().posEdgeEvent
    }

    override val negEdgeEvent: SysWait.Finder = object : SysWait.Finder() {
        override fun invoke() = this@SysBitInput().negEdgeEvent
    }

    val zero: Boolean
        get() = value.zero

    val one: Boolean
        get() = value.one

    val x: Boolean
        get() = value.x

    override operator fun invoke(): SysBitRead {
        return (super.invoke() as? SysBitRead) ?: throw AssertionError("Bit port $name is not bound to bit read interface")
    }
}

open class SysOutput<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, signalWrite: SysSignalWrite<T>? = null
) : SysPort<SysSignalWrite<T>>(name, scheduler, parent, signalWrite) {

    var value: T
        get() = throw UnsupportedOperationException("Signal read is not supported for output port")
        set(value) {
            if (!sealed) {
                if (!isBound) throw IllegalStateException("Port $name is not bound")
                bound!!.value = value
            }
        }
}

open class ReadOnlyPort<T : SysData>(
        open val inp: SysInput<T>
) : ReadOnlyProperty<SysModule, T> {
    override fun getValue(thisRef: SysModule, property: KProperty<*>) = inp.value
}

class ReadOnlyBitPort(
        override val inp: SysBitInput
) : ReadOnlyPort<SysBit>(inp)

class ReadWritePort<T: SysData>(
        val out: SysOutput<T>
) : ReadWriteProperty<SysModule, T> {
    override fun getValue(thisRef: SysModule, property: KProperty<*>) = out.value

    override fun setValue(thisRef: SysModule, property: KProperty<*>, value: T) {
        out.value = value
    }
}



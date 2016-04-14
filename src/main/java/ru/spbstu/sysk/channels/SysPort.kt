package ru.spbstu.sysk.channels

import ru.spbstu.sysk.core.SysModule
import ru.spbstu.sysk.core.SysObject
import ru.spbstu.sysk.core.SysScheduler
import ru.spbstu.sysk.core.SysWait
import ru.spbstu.sysk.data.SysBit
import ru.spbstu.sysk.data.SysData
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SysPort<IF : SysInterface> internal constructor(
        name: String, private val scheduler: SysScheduler, parent: SysObject? = null, sysInterface: IF? = null
) : SysObject(name, parent) {

    init {
        scheduler.register(this)
    }

    private var boundPort: SysPort<IF>? = null

    protected var bound: IF? = null
        private set

    val isBound: Boolean
        get() = bound != null || boundPort != null

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

    private fun bindCheck() {
        assert(scheduler.stopRequested) { "Impossible to bind the port while running the scheduler" }
        assert(!isBound) { "Port $name is already bound to ${bound()}" }
        assert(!sealed) { "Port $name is already sealed" }
    }

    infix fun bind(port: SysPort<IF>) {
        port.bound()?.let { bind(it) }
        if (bound == null) {
            bindCheck()
            boundPort = port
        }
    }

    infix open fun bind(sysInterface: IF) {
        bindCheck()
        bound = sysInterface
        sysInterface.register(this)
    }

    fun to(port: SysPort<IF>): Pair<SysPort<IF>, IF?> = Pair(this, port.bound())

    fun to(sysInterface: IF): Pair<SysPort<IF>, IF> = Pair(this, sysInterface)

    open protected fun bound(): IF? = bound ?: boundPort?.bound()

    val defaultEvent: SysWait.Finder = object : SysWait.Finder() {
        override operator fun invoke() = this@SysPort.bound()?.defaultEvent ?: throw AssertionError("Port $name is not bound so has yet no default event")
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

open class SysReadWritePort<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, signal: SysSignal<T>? = null,
        private val defaultValue: T? = null
) : SysPort<SysSignal<T>>(name, scheduler, parent, signal) {

    var value: T
        get() {
            if (isBound) {
                return bound()!!.value
            } else {
                if (defaultValue == null) throw IllegalStateException("Port $name is not bound")
                return defaultValue
            }
        }
        set(value) {
            if (!sealed) {
                if (!isBound) throw IllegalStateException("Port $name is not bound")
                bound()!!.value = value
            }
        }

    operator fun invoke() = value

    operator fun invoke(value: T) {
        this.value = value
    }
}

open class SysInput<T : SysData> internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, signalRead: SysSignalRead<T>? = null, private val defaultValue: T? = null
) : SysPort<SysSignalRead<T>>(name, scheduler, parent, signalRead) {

    val value: T
        get() {
            if (isBound) {
                return bound()!!.value
            } else {
                if (defaultValue == null) throw IllegalStateException("Port $name is not bound")
                return defaultValue
            }
        }

    operator fun invoke() = value
}

class SysBitInput internal constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null, signalRead: SysBitRead? = null
) : SysInput<SysBit>(name, scheduler, parent, signalRead), SysEdged {

    override val posEdgeEvent: SysWait.Finder = object : SysWait.Finder() {
        override fun invoke() =
                bound()?.posEdgeEvent
                ?: throw AssertionError("Port $name is not bound so does not have positive edge event yet")
    }

    override val negEdgeEvent: SysWait.Finder = object : SysWait.Finder() {
        override fun invoke() =
                bound()?.negEdgeEvent
                ?: throw AssertionError("Port $name is not bound so does not have negative edge event yet")
    }

    val zero: Boolean
        get() = value.zero

    val one: Boolean
        get() = value.one

    val x: Boolean
        get() = value.x

    override fun bound() = super.bound()?.let {
        (it as? SysBitRead) ?: throw AssertionError("Port $name is bound to $it : ${it.javaClass} which is not a bit read interface")
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
                bound()!!.value = value
            }
        }

    operator fun invoke(value: T) {
        this.value = value
    }
}

open class PortReader<T : SysData>(
        open protected val inp: SysInput<T>
) : ReadOnlyProperty<SysModule, T> {
    override fun getValue(thisRef: SysModule, property: KProperty<*>) = inp()
}

class BitPortReader(
        override val inp: SysBitInput
) : PortReader<SysBit>(inp)

open class PortWriter<T : SysData>(
        open protected val out: SysOutput<T>
) : ReadWriteProperty<SysModule, T> {
    override fun getValue(thisRef: SysModule, property: KProperty<*>) = out.value

    override fun setValue(thisRef: SysModule, property: KProperty<*>, value: T) {
        out(value)
    }
}



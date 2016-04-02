package ru.spbstu.sysk.core

import ru.spbstu.sysk.channels.*
import ru.spbstu.sysk.data.*
import java.util.*

open class SysModule internal constructor(
        name: String, val scheduler: SysScheduler, parent: SysModule? = null
) : SysObject(name, parent) {

    constructor(name: String, parent: SysModule) : this(name, parent.scheduler, parent)

    private val functions: MutableList<SysFunction> = ArrayList()

    // A set of helper functions

    protected val currentTime: SysWait.Time
        get() = scheduler.currentTime

    protected class SysModuleFunction(
            private val f: (SysWait) -> Any,
            sensitivities: SysWait,
            initialize: Boolean
    ) : SysFunction(sensitivities, initialize = initialize) {
        override fun run(event: SysWait): SysWait = run(event, wait(), f)
    }

    protected fun function(run: (SysWait) -> Any,
                           sensitivities: SysWait = SysWait.Never, initialize: Boolean = true): SysFunction {
        return SysModuleFunction(run, sensitivities, initialize).register()
    }

    protected fun function(sensitivities: SysWait = SysWait.Never, initialize: Boolean = true,
                           run: (SysWait) -> Unit = {}) =
            function({ run(it) }, sensitivities, initialize)

    protected fun function(clock: SysEdged, positive: Boolean = true,
                           initialize: Boolean = true,
                           run: (SysWait) -> Unit = {}) =
            function(if (positive) clock.posEdgeEvent else clock.negEdgeEvent,
                    initialize, run)

    protected fun stateFunction(sensitivities: SysWait = SysWait.Never,
                                init: SysStateFunction.() -> Unit): SysStateFunction {
        val result = SysStateFunction(sensitivities)
        result.init()
        return result.register()
    }

    protected fun stateFunction(clock: SysEdged, positive: Boolean = true,
                                init: SysStateFunction.() -> Unit) =
            stateFunction(if (positive) clock.posEdgeEvent else clock.negEdgeEvent, init)

    private fun <T : SysFunction> T.register(): T {
        scheduler.register(this)
        functions.add(this)
        return this
    }

    protected fun <T : SysData> port(name: String, defaultValue: T? = null, signal: SysSignal<T>? = null) =
            SysReadWritePort(name, scheduler, this, signal, defaultValue)

    protected fun <T : SysData> input(name: String, defaultValue: T? = null, signalRead: SysSignalRead<T>? = null): SysInput<T> =
            SysInput(name, scheduler, this, signalRead, defaultValue)

    protected fun bitInput(name: String, signalRead: SysBitRead? = null): SysBitInput =
            SysBitInput(name, scheduler, this, signalRead)

    protected fun <T : SysData> portReader(input: SysInput<T>) = PortReader<T>(input)

    protected fun bitPortReader(bitInput: SysBitInput) = BitPortReader(bitInput)

    protected fun <T : SysData> output(name: String, signalWrite: SysSignalWrite<T>? = null): SysOutput<T> =
            SysOutput(name, scheduler, this, signalWrite)

    protected fun <T : SysData> portWriter(output: SysOutput<T>) = PortWriter<T>(output)

    protected fun <T : SysData> signalStub(name: String, value: T): SysSignalStub<T> =
            SysSignalStub(name, value, scheduler, this)

    protected inline fun <reified T : SysData> signal(name: String): SysSignal<T> =
            signal(name, undefined<T>())

    protected fun <T : SysData> signal(name: String, startValue: T): SysSignal<T> =
            SysSignal(name, startValue, scheduler, this)

    protected inline fun <reified T : SysData> connector(
            name: String, writePort: SysOutput<T>, vararg readPorts: SysInput<T>
    ) = Connector(signal(name), writePort, *readPorts)

    protected inline fun <reified T : SysData> connector(
            name: String, vararg readPorts: SysInput<T>
    ) = Connector(signal(name), null, *readPorts)

    protected inline fun <reified T : SysData> signalReader(
            name: String, writePort: SysOutput<T>, vararg readPorts: SysInput<T>
    ) = SignalReader(signal(name), writePort, *readPorts)

    protected fun bitSignalReader(
            name: String, writePort: SysOutput<SysBit>, vararg readPorts: SysBitInput
    ) = SignalReader(bitSignal(name), writePort, *readPorts)

    protected inline fun <reified T : SysData> signalWriter(
            name: String, vararg readPorts: SysInput<T>
    ) = SignalWriter(signal(name), *readPorts)

    protected fun bitSignalWriter(
            name: String, vararg readPort: SysBitInput
    ) = SignalWriter(bitSignal(name), *readPort)

    protected fun bitSignal(name: String, startValue: SysBit = SysBit.X) =
            SysBitSignal(name, scheduler, startValue, this)

    protected fun clock(name: String, period: SysWait.Time, startValue: SysBit = SysBit.ZERO) =
            SysClock(name, period, scheduler, startValue)

    protected fun <T : SysData> register(name: String, startValue: T) =
            SysRegister(name, startValue, this)

    protected fun <T : SysData> memory(name: String, addrWidth: Int, defaultValue: T) =
            SysMemory(name, addrWidth, defaultValue, this)

    protected fun integerMemory(name: String, addrWidth: Int, dataWidth: Int) =
            SysIntegerMemory(name, addrWidth, dataWidth, this)

    protected fun <T : SysData> fifoOutput(name: String, fifo: SysFifo<T>? = null) =
            SysFifoOutput<T>(name, scheduler, this, fifo)

    protected fun <T : SysData> fifoInput(name: String, fifo: SysFifo<T>? = null) =
            SysFifoInput<T>(name, scheduler, this, fifo)

    protected fun <T : SysData> fifo(capacity: Int, name: String, startValue: T) =
            SysFifo<T>(capacity, name, startValue, scheduler, this)

    protected inline fun <reified T : SysData> fifo(capacity: Int, name: String) =
            fifo(capacity, name, undefined<T>())

    protected fun bitFifo(capacity: Int, name: String) =
            SysBitFifo(capacity, name, scheduler, this)

    protected fun <T : SysData> synchronousFifo(capacity: Int, name: String, startValue: T, positive: Boolean) =
            SysSynchronousFifo<T>(capacity, name, startValue, positive, scheduler, this)

    protected inline fun <reified T : SysData> synchronousFifo(capacity: Int, name: String, positive: Boolean) =
            synchronousFifo(capacity, name, undefined<T>(), positive)

    protected fun synchronousBitFifo(capacity: Int, name: String, positive: Boolean, startValue: SysBit = undefined()) =
            SysSynchronousBitFifo(capacity, name, positive, startValue, scheduler, this)

    protected fun <T : SysData> busPort(capacity: Int, name: String)
            = SysBusPort<T>(capacity, name, scheduler, this)

    protected fun bitBusPort(capacity: Int, name: String)
            = SysBitBusPort(capacity, name, scheduler, this)

    protected fun bitBus(capacity: Int, name: String) = SysBitBus(capacity, name, scheduler, this)

    protected fun event(name: String): SysWait.Event = SysWait.Event(name, scheduler)

    protected fun <T : Any> iterator(progression: Iterable<T>) = ResetIterator.create(progression)

    companion object {

        private fun run(event: SysWait, default: SysWait, f: (SysWait) -> Any) =
                (f(event) as? SysWait)?.let { it } ?: default

    }
}

open class SysTopModule(
        name: String = "top", scheduler: SysScheduler = SysScheduler()
) : SysModule(name, scheduler, null) {

    fun start(endTime: SysWait.Time = SysWait.Time.INFINITY) {
        scheduler.start(endTime)
    }
}
package sysk

import java.util.*

open class SysBus constructor(
        private val signals: ArrayList<SysSignal<Any?>>, name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    protected val changeEvent = SysWait.Event("changeEvent", scheduler, this)

    operator fun get(index: Int) = signals.get(index)

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
    }
}
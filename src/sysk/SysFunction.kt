package sysk

abstract class SysFunction internal constructor(
        val sensitivities: List<SysWait>, scheduler: SysScheduler, val initialize: Boolean = true
) {

    init {
        scheduler.register(this)
    }

    internal constructor(scheduler: SysScheduler, vararg sensitivities: SysWait, initialize: Boolean = true):
            this(sensitivities.toList(), scheduler, initialize)

    abstract fun run(initialization: Boolean = false): SysWait

    open fun wait(): SysWait = SysWait.reduce(sensitivities)
}

abstract class SysTriggeredFunction internal constructor(
        val trigger: SysWait, sensitivities: List<SysWait>, scheduler: SysScheduler, initialize: Boolean = true
): SysFunction(sensitivities + trigger, scheduler, initialize) {

    internal constructor(
            clock: SysClockedSignal,
            positive: Boolean,
            scheduler: SysScheduler,
            sensitivities: List<SysWait> = emptyList(),
            initialize: Boolean = true
    ): this(if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, scheduler, initialize)

    internal constructor(
            port: SysWireInput,
            positive: Boolean,
            scheduler: SysScheduler,
            sensitivities: List<SysWait> = emptyList(),
            initialize: Boolean = true
    ): this(if (positive) port.posEdgeEvent else port.negEdgeEvent, sensitivities, scheduler, initialize)

    final override fun wait(): SysWait = trigger
}
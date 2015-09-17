package sysk

internal abstract class SysFunction(val sensitivities: List<SysWait>, val initialize: Boolean = true) {

    init {
        SysScheduler.register(this)
    }

    constructor(vararg sensitivities: SysWait, initialize: Boolean = true): this(sensitivities.toList(), initialize)

    abstract fun run(initialization: Boolean = false): SysWait

    open fun wait(): SysWait = SysWait.reduce(sensitivities)
}

internal abstract class SysTriggeredFunction(val trigger: SysWait, sensitivities: List<SysWait>, initialize: Boolean = true):
        SysFunction(sensitivities + trigger, initialize) {

    constructor(clock: SysClockedSignal, positive: Boolean, sensitivities: List<SysWait> = emptyList(), initialize: Boolean = true):
            this(if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

    constructor(port: SysBooleanInput, positive: Boolean, sensitivities: List<SysWait> = emptyList(), initialize: Boolean = true):
            this(if (positive) port.posEdgeEvent else port.negEdgeEvent, sensitivities, initialize)

    final override fun wait(): SysWait = trigger
}
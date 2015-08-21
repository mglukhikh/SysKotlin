package sysk

internal abstract class SysFunction(public val sensitivities: List<SysWait>, public val initialize: Boolean = true) {

    init {
        SysScheduler.register(this)
    }

    public constructor(vararg sensitivities: SysWait, initialize: Boolean = true): this(sensitivities.toList(), initialize)

    abstract fun run(initialization: Boolean = false): SysWait

    public open fun wait(): SysWait = SysWait.reduce(sensitivities)
}

internal abstract class SysTriggeredFunction(public val trigger: SysWait, sensitivities: List<SysWait>, initialize: Boolean = true):
        SysFunction(sensitivities + trigger, initialize) {

    public constructor(clock: SysClockedSignal, positive: Boolean, sensitivities: List<SysWait> = emptyList(), initialize: Boolean = true):
            this(if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

    public constructor(port: SysBooleanInput, positive: Boolean, sensitivities: List<SysWait> = emptyList(), initialize: Boolean = true):
            this(if (positive) port.posEdgeEvent else port.negEdgeEvent, sensitivities, initialize)

    public final override fun wait(): SysWait = trigger
}
package sysk

abstract class SysFunction internal constructor(
        sensitivities: List<SysWait>, initialize: Boolean = true
) {

    val sensitivities = if (initialize) sensitivities + SysWait.Initialize else sensitivities

    internal constructor(vararg sensitivities: SysWait, initialize: Boolean = true):
            this(sensitivities.toList(), initialize)

    abstract fun run(event: SysWait): SysWait

    open fun wait(): SysWait = SysWait.reduce(sensitivities)
}

abstract class SysTriggeredFunction internal constructor(
        val trigger: SysWait, sensitivities: List<SysWait>, initialize: Boolean = true
): SysFunction(sensitivities + trigger, initialize) {

    internal constructor(
            clock: SysEdged,
            positive: Boolean,
            sensitivities: List<SysWait> = emptyList(),
            initialize: Boolean = true
    ): this(if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

    final override fun wait(): SysWait = trigger
}

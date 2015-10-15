package sysk

abstract class SysFunction internal constructor(
        val sensitivities: List<SysWait>, val initialize: Boolean = true
) {

    internal constructor(vararg sensitivities: SysWait, initialize: Boolean = true):
            this(sensitivities.toList(), initialize)

    abstract fun run(initialization: Boolean = false): SysWait

    open fun wait(): SysWait = SysWait.reduce(sensitivities)
}

abstract class SysTriggeredFunction internal constructor(
        val trigger: SysWait, sensitivities: List<SysWait>, initialize: Boolean = true
): SysFunction(sensitivities + trigger, initialize) {

    internal constructor(
            clock: SysClockedSignal,
            positive: Boolean,
            sensitivities: List<SysWait> = emptyList(),
            initialize: Boolean = true
    ): this(if (positive) clock.posEdgeEvent else clock.negEdgeEvent, sensitivities, initialize)

    internal constructor(
            port: SysWireInput,
            positive: Boolean,
            sensitivities: List<SysWait> = emptyList(),
            initialize: Boolean = true
    ): this(if (positive) port.posEdgeEvent else port.negEdgeEvent, sensitivities, initialize)

    final override fun wait(): SysWait = trigger
}

abstract class SysFunctionWithCounter internal constructor(
        trigger: SysWait,
        val init: () -> Unit,
        vararg val stages: () -> Unit
): SysTriggeredFunction(trigger, emptyList()) {

    var counter = 0

    override fun run(initialization: Boolean): SysWait {
        if (initialization) {
            init()
        }
        else {
            if (counter < stages.size()) {
                stages.get(counter)()
            }
            counter++
        }
        return wait()
    }
}
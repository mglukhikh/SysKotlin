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

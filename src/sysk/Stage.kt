package sysk

interface StageContainer {
    val stages: MutableList<Stage>

    var stageNumber: Int

    fun complete() = stageNumber >= stages.size

    fun wait(): SysWait

    fun stage(f: () -> Unit): Stage.Atomic {
        val result = Stage.Atomic {
            f()
            wait()
        }
        stages.add(result)
        return result
    }

    fun complexStage(init: Stage.Complex.() -> Unit): Stage.Complex {
        val result = Stage.Complex(wait(), linkedListOf())
        result.init()
        stages.add(result)
        return result
    }

    fun infiniteStage(f: () -> Unit): Stage.Infinite {
        val result = Stage.Infinite {
            f()
            wait()
        }
        stages.add(result)
        return result
    }

    fun run(event: SysWait): SysWait {
        if (!complete()) {
            stages[stageNumber].let {
                val result = it.run(event)
                if (it.complete()) {
                    stageNumber++
                }
                return result
            }
        }
        else return SysWait.Never
    }
}

sealed class Stage {

    abstract fun run(event: SysWait): SysWait

    abstract fun complete(): Boolean

    class Atomic internal constructor(private val f: () -> SysWait) : Stage() {
        override fun run(event: SysWait) = f()

        override fun complete() = true
    }

    class Complex internal constructor(
            private val sensitivities: SysWait,
            override val stages: MutableList<Stage>
    ) : Stage(), StageContainer {

        override fun wait() = sensitivities

        override var stageNumber = 0

        override fun run(event: SysWait) = super.run(event)

        override fun complete() = super.complete()
    }

    class Infinite internal constructor(private val f: () -> SysWait) : Stage() {
        override fun run(event: SysWait): SysWait = f()

        override fun complete() = false
    }
}

class StagedFunction private constructor(
        override val stages: MutableList<Stage>,
        private var initStage: Stage.Atomic? = null,
        sensitivities: SysWait = SysWait.Never
): SysFunction(sensitivities, initialize = true), StageContainer {
    internal constructor(sensitivities: SysWait = SysWait.Never):
            this(linkedListOf(), null, sensitivities)

    fun initStage(f: () -> Unit): Stage.Atomic {
        initStage = Stage.Atomic {
            f()
            wait()
        }
        return initStage!!
    }

    private fun init(event: SysWait): SysWait {
        // BUG: return infiniteStage?.run() ?: SysWait.Never, see KT-10142
        if (initStage == null) return wait()
        return initStage!!.run(event)
    }

    override var stageNumber = 0

    override fun run(event: SysWait): SysWait {
        if (event == SysWait.Initialize) {
            return init(event)
        }
        else {
            return super.run(event)
        }
    }
}

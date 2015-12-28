package ru.spbstu.sysk.core

interface StateContainer {
    val states: MutableList<State>

    var state: Int

    fun complete() = state >= states.size

    fun wait(): SysWait

    fun state(f: () -> Unit): State.Single {
        val result = State.Single {
            f()
            wait()
        }
        states.add(result)
        return result
    }

    fun block(init: State.Block.() -> Unit): State.Block {
        val result = State.Block(wait(), linkedListOf())
        result.init()
        states.add(result)
        return result
    }

    fun <T : Any> forEach(progression: Iterable<T>, init: State.Iterative<T>.() -> Unit): State.Iterative<T> {
        val result = State.Iterative(progression, wait(), linkedListOf())
        result.init()
        states.add(result)
        return result
    }

    fun infinite(f: () -> Unit): State.Infinite {
        val result = State.Infinite {
            f()
            wait()
        }
        states.add(result)
        return result
    }

    fun run(event: SysWait): SysWait {
        if (!complete()) {
            states[state].let {
                val result = it.run(event)
                if (it.complete()) {
                    state++
                }
                return result
            }
        }
        else return SysWait.Never
    }
}

sealed class State {

    abstract fun run(event: SysWait): SysWait

    abstract fun complete(): Boolean

    class Single internal constructor(private val f: () -> SysWait) : State() {
        override fun run(event: SysWait) = f()

        override fun complete() = true
    }

    class Block internal constructor(
            private val sensitivities: SysWait,
            override val states: MutableList<State>
    ) : State(), StateContainer {

        override fun wait() = sensitivities

        override var state = 0

        override fun run(event: SysWait) = super.run(event)

        override fun complete() = super.complete()
    }

    class Iterative<T : Any> internal constructor(
            val progression: Iterable<T>,
            private val sensitivities: SysWait,
            override val states: MutableList<State>
    ) : State(), StateContainer {
        override fun wait() = sensitivities

        override var state = 0

        private val iterator = progression.iterator()

        private var _it: T? = null

        val it: T
            get() = _it ?: throw AssertionError("Accessing loop iterator outside the loop")

        init {
            nextIteration()
        }

        private fun nextIteration() {
            if (iterator.hasNext()) {
                _it = iterator.next()
            }
        }

        override fun run(event: SysWait): SysWait {
            val result = super.run(event)
            if (super.complete()) {
                nextIteration()
                state = 0
            }
            return result
        }

        override fun complete() = !iterator.hasNext()
    }

    class Infinite internal constructor(private val f: () -> SysWait) : State() {
        override fun run(event: SysWait): SysWait = f()

        override fun complete() = false
    }
}

class SysStateFunction private constructor(
        override val states: MutableList<State>,
        private var initState: State.Single? = null,
        sensitivities: SysWait = SysWait.Never
): SysFunction(sensitivities, initialize = true), StateContainer {
    internal constructor(sensitivities: SysWait = SysWait.Never):
            this(linkedListOf(), null, sensitivities)

    fun init(f: () -> Unit): State.Single {
        initState = State.Single {
            f()
            wait()
        }
        return initState!!
    }

    private fun init(event: SysWait): SysWait {
        // BUG: return infiniteStage?.run() ?: SysWait.Never, see KT-10142
        if (initState == null) return wait()
        return initState!!.run(event)
    }

    override var state = 0

    override fun run(event: SysWait): SysWait {
        if (event == SysWait.Initialize) {
            return init(event)
        }
        else {
            return super.run(event)
        }
    }
}

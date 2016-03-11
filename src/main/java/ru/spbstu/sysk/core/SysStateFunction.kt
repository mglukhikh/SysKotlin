package ru.spbstu.sysk.core

import java.util.*

interface StateContainer {
    val states: MutableList<State>
    val labels: MutableMap<String, Int>

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

    fun sleep(number: Int): State.Function {
        if (number <= 0) {
            throw IllegalArgumentException("Impossible to sleep $number cycles.")
        }
        var memory = number
        val result = State.Function("sleep", { true }) {
            memory--
            if (memory > 0) state--
            else memory = number
            wait()
        }
        states.add(result)
        return result
    }

    fun label(name: String) {
        labels[name] = states.size
    }

    fun jump(labelName: String): State.Function {
        val result = State.Function("goTo", { false }) {
            state = -1
            labels.forEach { if (it.key == labelName) state = it.value }
            if (state == -1) throw IllegalArgumentException("label: $labelName not found")
            if (state < states.size) this.run(wait())
            wait()
        }
        states.add(result)
        return result
    }

    fun If(condition: () -> Boolean, init: StateContainer.() -> Unit): State.Function {
        val current = this.states.size
        this.init()
        val delta = this.states.size - current
        val func = State.Function("if", { false }) {
            val cond = condition()
            val Else: State.Function? = states.elementAtOrNull(state + delta + 1) as? State.Function
            if (Else != null && Else.name == "else") Else.args = cond
            if (!cond) state += delta
            if (++state < states.size) this.run(wait())
            wait()
        }
        states.add(current, func)
        return func
    }

    fun Else(init: StateContainer.() -> Unit): State.Function {
        val current = this.states.size
        this.init()
        val delta = this.states.size - current
        val func = State.Function("else", { false }) {
            val cond = (states[state] as State.Function).args as? Boolean ?: throw AssertionError()
            if (cond) state += delta
            if (++state < states.size) this.run(wait())
            wait()
        }
        states.add(current, func)
        return func
    }

    fun <T : Any> forEach(progression: Iterable<T>, init: State.Iterative<T>.() -> Unit): State.Iterative<T> {
        val result = State.Iterative(progression, wait(), LinkedList(), HashMap())
        result.init()
        states.add(result)
        return result
    }

    fun infiniteBlock(init: State.Iterative<Nothing>.() -> Unit): State.Iterative<Nothing> {
        val result = State.Iterative<Nothing>(wait(), LinkedList(), HashMap())
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
        if (complete()) state = 0
        states[state].let {
            val result = it.run(event)
            if (it.complete()) {
                state++
            }
            return result
        }
    }
}

sealed class State {

    abstract fun run(event: SysWait): SysWait

    abstract fun complete(): Boolean

    class Single internal constructor(private val f: () -> SysWait) : State() {
        override fun run(event: SysWait) = f()

        override fun complete() = true
    }

    class Iterative<T : Any> internal constructor(
            val progression: Iterable<T>?,
            private val sensitivities: SysWait,
            override val states: MutableList<State>,
            override val labels: MutableMap<String, Int>
    ) : State(), StateContainer {

        internal constructor(sensitivities: SysWait, states: MutableList<State>, labels: MutableMap<String, Int>) :
                this(null, sensitivities, states, labels)

        override fun wait() = sensitivities

        override var state = 0

        private val iterator = progression?.iterator()

        private var _it: T? = null

        val it: T
            get() = _it ?: throw AssertionError("Accessing loop iterator outside the loop")

        init {
            nextIteration()
        }

        private fun nextIteration() {
            if (iterator != null && iterator.hasNext()) {
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

        override fun complete() = iterator?.hasNext() == false
    }

    class Infinite internal constructor(private val f: () -> SysWait) : State() {
        override fun run(event: SysWait): SysWait = f()

        override fun complete() = false
    }

    class Function internal constructor(
            val name: String,
            private val comp: () -> Boolean,
            private val f: (SysWait) -> SysWait)
    : State() {
        var args: Any? = null

        override fun run(event: SysWait) = f(event)

        override fun complete() = comp()
    }
}

class SysStateFunction private constructor(
        override val states: MutableList<State>,
        override val labels: MutableMap<String, Int>,
        private var initState: State.Single? = null,
        sensitivities: SysWait = SysWait.Never
) : SysFunction(sensitivities, initialize = true), StateContainer {
    internal constructor(sensitivities: SysWait = SysWait.Never) :
    this(LinkedList(), HashMap(), null, sensitivities)

    fun init(f: () -> Unit): State.Single {
        initState = State.Single {
            f()
            wait()
        }
        return initState!!
    }

    private fun init(event: SysWait): SysWait = initState?.run(event) ?: wait()

    override var state = 0

    override fun run(event: SysWait): SysWait {
        if (event == SysWait.Initialize) {
            return init(event)
        } else {
            return super.run(event)
        }
    }
}

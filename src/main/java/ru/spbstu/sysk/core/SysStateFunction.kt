package ru.spbstu.sysk.core

import ru.spbstu.sysk.data.SysReference
import java.util.*

sealed class Label {
    class User internal constructor(val name: String) : Label() {
        override fun equals(other: Any?) = name == (other as? User)?.name

        override fun hashCode() = name.hashCode()
    }
    class Internal internal constructor() : Label()
}

interface StateContainer {
    val states: MutableList<State>
    val labels: MutableMap<Label, Int>

    var state: Int

    fun complete() = state >= states.size

    fun wait(): SysWait

    fun state(f: () -> Unit) {
        val result = State.Single {
            f()
            wait()
        }
        states.add(result)
    }

    fun sleep(number: Int) {
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
    }

    fun label(label: String) = labelInternal(Label.User(label))

    fun jump(label: String) = jumpInternal(Label.User(label))

    private fun labelInternal(label: Label) {
        labels[label] = states.size
    }

    private fun jumpInternal(label: Label) {
        val result = State.Function("jump", { false }) {
            state = labels[label] ?: throw IllegalArgumentException("label: $label not found")
            if (state < states.size) this.run(wait())
            wait()
        }
        states.add(result)
    }

    fun If(condition: () -> Boolean, init: StateContainer.() -> Unit) {
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
    }

    fun Else(init: StateContainer.() -> Unit) {
        val current = this.states.size
        this.init()
        val delta = this.states.size - current
        val func = State.Function("else", { false }) {
            val cond = (states[state] as State.Function).args as? Boolean
                    ?: throw AssertionError("Block 'If' expected before 'Else'")
            if (cond) state += delta
            if (++state < states.size) this.run(wait())
            wait()
        }
        states.add(current, func)
    }

    fun Continue() {
        states.add(State.Function("continue", { true }, {
            throw AssertionError("'Break' and 'Continue' are only allowed inside a loop")
        }))
    }

    fun Break() {
        states.add(State.Function("break", { true }, {
            throw AssertionError("'Break' and 'Continue' are only allowed inside a loop")
        }))
    }

    private fun toJump(name: String, mark: Any, self: StateContainer, begin: Int, end: Int) {
        if (begin > end) throw AssertionError()
        var i = begin
        while (i != end) {
            if ((self.states[i] as? State.Function)?.name == name) {
                self.states[i] = State.Function("jump", { false }) {
                    self.state = self.labels[mark] ?: throw AssertionError()
                    if (self.state < self.states.size) self.run(wait())
                    wait()
                }
            }
            ++i
        }
    }

    fun While(condition: () -> Boolean, init: StateContainer.() -> Unit) {
        val begin = Label.Internal()
        val end = Label.Internal()
        labelInternal(begin)
        If({ !condition() }) { jumpInternal(end) }
        val current = this.states.size
        this.init()
        toJump("break", end, this, current, this.states.size)
        toJump("continue", begin, this, current, this.states.size)
        jumpInternal(begin)
        labelInternal(end)
    }

    fun <T : Any> For(ref: SysReference<ResetIterator<T>>, progression: Iterable<T>, init: StateContainer.() -> Unit) {
        ref(ResetIterator.create(progression))
        For(ref()!!, init)
    }

    fun <T : Any> For(progression: Iterable<T>, init: StateContainer.() -> Unit) {
        For(ResetIterator.create(progression), init)
    }

    fun <T : Any> For(iterator: ResetIterator<T>, init: StateContainer.() -> Unit) {
        val begin = Label.Internal()
        val end = Label.Internal()
        val reset = State.Function("reset", { false }) {
            iterator.reset()
            if (++state < states.size) this.run(wait())
            wait()
        }
        states.add(reset)
        labelInternal(begin)
        If({
            var cond = iterator.hasNext()
            if (cond) iterator.next()
            !cond
        }) { jumpInternal(end) }
        val current = this.states.size
        this.init()
        toJump("break", end, this, current, this.states.size)
        toJump("continue", begin, this, current, this.states.size)
        jumpInternal(begin)
        labelInternal(end)
    }

    fun <T : Any> forEach(progression: Iterable<T>, init: StateContainer.() -> Unit) {
        For(progression, init)
    }

    fun infiniteBlock(init: StateContainer.() -> Unit) {
        While({true}, init)
    }

    fun infinite(f: () -> Unit) {
        val result = State.Function("infinite", { false }) {
            f()
            wait()
        }
        states.add(result)
    }

    fun run(event: SysWait): SysWait {
        if (!complete()) {
            states[state].let {
                val result = it.run(event)
                if (it.complete()) state++
                return result
            }
        } else return SysWait.Never
    }
}

sealed class State {

    abstract fun run(event: SysWait): SysWait

    abstract fun complete(): Boolean

    class Single internal constructor(private val f: () -> SysWait) : State() {
        override fun run(event: SysWait) = f()

        override fun complete() = true
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
        override val labels: MutableMap<Label, Int>,
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

class ResetIterator<T : Any> internal constructor(private var parent: Iterable<T>) : Iterator<T> {
    private var iterator = parent.iterator()

    var it: T? = null
        private set

    override fun next(): T {
        it = iterator.next()
        return it as T
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    internal fun reset() {
        iterator = parent.iterator()
    }

    companion object {
        fun <T : Any> create(parent: Iterable<T>) = ResetIterator(parent)
    }
}
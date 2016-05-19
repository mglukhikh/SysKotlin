package ru.spbstu.sysk.core

import java.util.*

sealed class Label {
    class User internal constructor(val name: String) : Label() {
        override fun equals(other: Any?) = name == (other as? User)?.name

        override fun hashCode() = name.hashCode()

        override fun toString() = name
    }

    class Internal internal constructor() : Label()
}

interface StateContainer {
    val states: MutableList<State>
    val labels: MutableMap<Label, Int>

    var currentState: Int

    fun complete() = currentState >= states.size

    fun wait(): SysWait

    fun stop(scheduler: SysScheduler) {
        val result = State.Single {
            scheduler.stop()
            wait()
        }
        states.add(result)
    }

    val state: State.Extends
        get() = State.Extends(this)

    fun state(f: () -> Unit) = state(1, f)

    fun state(clocks: Int, f: () -> Unit) {
        if (clocks < 1) throw IllegalArgumentException("Attempt to stay in state for $clocks clocks")
        val result = State.Single {
            f()
            wait()
        }
        states.add(result)
        for (i in 2..clocks) states.add(State.Single { wait() })
    }

    fun sleep(number: Int) {
        if (number < 0) throw IllegalArgumentException("Impossible to sleep $number cycles.")
        var memory = number
        val result = State.Function("sleep", { true }) {
            if (number == 0) {
                if (++currentState < states.size) this.run(wait())
                --currentState
            }
            --memory
            if (memory > 0) currentState--
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
        val result = State.JumpFunction {
            currentState = labels[label] ?: throw IllegalArgumentException("label: $label not found")
            if (currentState < states.size) this.run(wait())
            wait()
        }
        states.add(result)
    }

    fun case(condition: () -> Boolean) = State.Block(this, { init -> case(condition, init) })

    private fun caseBuilder(condition: () -> Boolean, init: StateContainer.() -> Unit, isCase: Boolean) {
        states.add(State.CaseFunction("blank", { wait() }))
        val current = this.states.size
        this.init()
        val delta = this.states.size - current
        val func = State.CaseFunction("if") {
            val prevCond = (states[currentState] as? State.CaseFunction)?.args as? Boolean
                    ?: if (isCase) false else throw AssertionError("Block 'case' expected before 'otherwise'")
            val cond = condition() && !prevCond
            val case = if (isCase) states.elementAtOrNull(currentState + delta + 1) as? State.CaseFunction else null
            if (case != null) case.args = cond || prevCond
            if (!cond) currentState += delta
            if (++currentState < states.size) this.run(wait())
            wait()
        }
        states[current - 1] = func
    }

    fun case(condition: () -> Boolean, init: StateContainer.() -> Unit) = caseBuilder(condition, init, true)

    val otherwise: State.Block
        get() = State.Block(this, { init -> otherwise(init) })

    fun otherwise(init: StateContainer.() -> Unit) = caseBuilder({ true }, init, false)

    fun continueLoop() {
        states.add(State.LoopJumpFunction("continue"))
    }

    fun breakLoop() {
        states.add(State.LoopJumpFunction("break"))
    }

    private fun toJump(name: String, mark: Any, self: StateContainer, begin: Int, end: Int) {
        if (begin > end) throw AssertionError()
        var i = begin
        while (i != end) {
            if ((self.states[i] as? State.LoopJumpFunction)?.name == name) {
                self.states[i] = State.JumpFunction {
                    self.currentState = self.labels[mark] ?: throw AssertionError()
                    if (self.currentState < self.states.size) self.run(wait())
                    wait()
                }
            }
            ++i
        }
    }

    fun <T : Any> loop(progression: Iterable<T>) = State.Block(this, { init -> loop(progression, init) })

    fun <T : Any> loop(iterator: ResetIterator<T>) = State.Block(this, { init -> loop(iterator, init) })

    fun loop(condition: () -> Boolean, init: StateContainer.() -> Unit) {
        val begin = Label.Internal()
        val end = Label.Internal()
        labelInternal(begin)
        case({ !condition() }) { jumpInternal(end) }
        val current = this.states.size
        this.init()
        toJump("break", end, this, current, this.states.size)
        toJump("continue", begin, this, current, this.states.size)
        jumpInternal(begin)
        labelInternal(end)
    }

    fun <T : Any> loop(progression: Iterable<T>, init: StateContainer.() -> Unit) {
        loop(ResetIterator.create(progression), init)
    }

    fun <T : Any> loop(iterator: ResetIterator<T>, init: StateContainer.() -> Unit) {
        val begin = Label.Internal()
        val end = Label.Internal()
        val reset = State.Function("reset", { false }) {
            iterator.reset()
            if (++currentState < states.size) this.run(wait())
            wait()
        }
        states.add(reset)
        labelInternal(begin)
        case({
            val cond = iterator.hasNext()
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

    val infinite: State.Block
        get() = State.Block(this, { init -> loop({ true }, init) })

    fun run(event: SysWait): SysWait {
        if (!complete()) {
            states[currentState].let {
                val result = it.run(event)
                if (it.complete()) currentState++
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

    class LoopJumpFunction(name: String) : Function(
            name,
            { true },
            { throw AssertionError("'Break' and 'Continue' are only allowed inside a loop") }
    )

    class JumpFunction(f: (SysWait) -> SysWait) : Function("jump", { false }, f)

    open class CaseFunction(name: String, f: (SysWait) -> SysWait) : Function(name, { false }, f)

    class Block(private val container: StateContainer, private val function: StateContainer.(StateContainer.() -> Unit) -> Unit) {

        fun block(init: StateContainer.() -> Unit) = container.function(init)

        fun state(f: () -> Unit) = container.function({ state(f) })
    }

    class Extends(private val container: StateContainer) {

        fun print(string: () -> String) {
            val result = State.Single {
                print(string())
                container.wait()
            }
            container.states.add(result)
        }

        fun println(string: () -> String) {
            val result = State.Single {
                println(string())
                container.wait()
            }
            container.states.add(result)
        }

        fun instance(f: () -> Unit) {
            val result = State.Single {
                f()
                if (++container.currentState < container.states.size) container.run(container.wait())
                --container.currentState
                container.wait()
            }
            container.states.add(result)
        }
    }


    open class Function internal constructor(
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

    override var currentState = 0

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

    lateinit var it: T
        private set

    override fun next(): T {
        it = iterator.next()
        return it
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
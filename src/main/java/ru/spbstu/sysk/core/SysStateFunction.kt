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

    var next: Boolean

    fun next() {
        next = true
    }

    fun nextState() {
        ++currentState
        next()
    }

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
        if (clocks > 1) sleepInternal(clocks - 1)
    }

    fun sleep(clock: Int) {
        if (clock == 0) nopInternal("sleep")
        else sleepInternal(clock)
    }

    private fun sleepInternal(clocks: Int) {
        if (clocks < 0) throw IllegalArgumentException("Impossible to sleep $clocks cycles.")
        var memory = clocks
        var complete = false
        val result = State.Function("sleep", { complete }) {
            --memory
            complete = (memory == 0)
            if (complete) memory = clocks
            wait()
        }
        states.add(result)
    }

    fun nopInternal(name: String) {
        val result = State.Function(name, { false }) {
            nextState()
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
            next()
            wait()
        }
        states.add(result)
    }

    var id: Long
    private fun uniqueName() = "caseFamily${++id}"

    fun <T : Any> case(obj: () -> T) = State.Switch(this, obj, uniqueName()) {
        container, condition, init, isCase, familyName ->
        container.caseBuilder(condition, init, isCase, familyName)
    }

    val case: State.Case
        get() = State.Case(this, uniqueName()) { container, condition, init, isCase, familyName ->
            container.caseBuilder(condition, init, isCase, familyName)
        }

    private fun caseBuilder(condition: () -> Boolean, init: StateContainer.() -> Unit, isCase: Boolean, familyName: String) {
        states.add(State.CaseFunction("blank", { wait() }))
        val current = this.states.size
        this.init()
        val delta = this.states.size - current
        val func = State.CaseFunction(familyName) {
            val prevCond = (states[currentState] as? State.CaseFunction)?.args as? Boolean
                    ?: if (isCase) false else throw AssertionError("Block 'case' expected before 'otherwise'")
            val cond = condition() && !prevCond
            val case = if (isCase) states.elementAtOrNull(currentState + delta + 1) as? State.CaseFunction else null
            if (case != null && case.name == familyName) case.args = cond || prevCond
            if (!cond) currentState += delta
            nextState()
            wait()
        }
        states[current - 1] = func
    }

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
                    self.next()
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
        case.of({ !condition() }) { jumpInternal(end) }
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
            nextState()
            wait()
        }
        states.add(reset)
        labelInternal(begin)
        case.of({
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
        var result: SysWait = SysWait.Never
        if (!complete()) {
            var complete: Boolean
            do {
                next = false
                val foo = states[currentState]
                result = foo.run(event)
                complete = foo.complete()
            } while (next && !complete())
            if (complete) ++currentState
        }
        return result
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

    class Case(
            private val container: StateContainer,
            private val familyName: String,
            private val case: (StateContainer, () -> Boolean, StateContainer.() -> Unit, Boolean, String) -> Unit
    ) {

        fun of(condition: () -> Boolean)
                = CaseBlock(container, { init -> case(container, condition, init, true, familyName) }, this)

        fun of(condition: () -> Boolean, init: StateContainer.() -> Unit): Case {
            case(container, condition, init, true, familyName)
            return this
        }

        val otherwise: Block
            get() = Block(container, { init -> case(container, { true }, init, false, familyName) })

        fun otherwise(init: StateContainer.() -> Unit) {
            case(container, { true }, init, false, familyName)
        }
    }

    class Switch<T : Any>(
            private val container: StateContainer,
            private val obj: () -> T,
            private val familyName: String,
            private val case: (StateContainer, () -> Boolean, StateContainer.() -> Unit, Boolean, String) -> Unit
    ) {

        fun of(obj: T)
                = SwitchBlock(container, { init -> case(container, { obj().equals(obj) }, init, true, familyName) }, this)

        fun of(obj: T, init: StateContainer.() -> Unit): Switch<T> {
            case(container, { obj().equals(obj) }, init, true, familyName)
            return this
        }

        fun <R : Comparable<T>> inside(range: Iterable<R>)
                = SwitchBlock(container, { init -> case(container, { inRange(obj(), range) }, init, true, familyName) }, this)

        fun <R : Comparable<T>> inside(range: Iterable<R>, init: StateContainer.() -> Unit): Switch<T> {
            case(container, { inRange(obj(), range) }, init, true, familyName)
            return this
        }

        private fun <Type : Any, R : Comparable<Type>> inRange(obj: Type, range: Iterable<R>)
                = (range.first().compareTo(obj) <= 0) && (range.last().compareTo(obj) >= 0)

        val otherwise: Block
            get() = Block(container, { init -> case(container, { true }, init, false, familyName) })

        fun otherwise(init: StateContainer.() -> Unit) {
            case(container, { true }, init, false, familyName)
        }
    }

    class CaseBlock(
            private val container: StateContainer,
            private val function: StateContainer.(StateContainer.() -> Unit) -> Unit,
            private val parent: Case
    ) {
        fun block(init: StateContainer.() -> Unit): Case {
            Block(container, function).block(init)
            return parent
        }

        fun state(f: () -> Unit): Case {
            Block(container, function).state(f)
            return parent
        }
    }

    class SwitchBlock<T : Any>(
            private val container: StateContainer,
            private val function: StateContainer.(StateContainer.() -> Unit) -> Unit,
            private val parent: Switch<T>
    ) {

        fun block(init: StateContainer.() -> Unit): Switch<T> {
            Block(container, function).block(init)
            return parent
        }

        fun state(f: () -> Unit): Switch<T> {
            Block(container, function).state(f)
            return parent
        }
    }

    class Block(
            private val container: StateContainer,
            private val function: StateContainer.(StateContainer.() -> Unit) -> Unit) {

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

        fun instant(f: () -> Unit) {
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

    override var next = false

    override var id = 0L

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
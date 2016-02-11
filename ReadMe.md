# Requirements

* Concise, including not too much words
* Light-weight, including not too many primitives inside
* Deterministic, no "unknown" behaviour
* Same behaviour as synthesized HDL
* Easily analyzable, no complex stuff like macros and so
** Thus, higher-order functions are required instead
* Single-source, no additional scripts or config files to describe synthesis process

# Kotlin comparison

* Concise
* Safe
* Versatile (higher-order functions included)
* Interoperable
* Easy to read

# Version 0.0.1

Planned at ~ 25 Dec 2015.

Delayed to 29 Feb 2015.

TODO

* Features
  * function builder (mikhail) -- done partially
    * universal syntax for state & block
    * syntax for if / else state
    * syntax for loops / continue / break
    * syntax for jumps to a different state
  * memory module (veronika) -- failed
  * integers
    * SysBigInteger (valentin) -- done partially
      * functions for dealing with just ints / floats / etc.
      * base class for SysInteger & SysBigInteger
      * remove code duplication etc.
    * SysFloat (valentin) -- not started
    * SysFixed (valentin) -- not started
  * delayed happens (?) (sergei)
  * ports
    * default values (sergei) -- done
    * stub signals (sergei) -- done
    * check binding (mikhail / sergei) -- not started
  * reset (?)
* Tests
  * FIFO / buses (sergei) -- done
  * memory module (veronika) -- failed
  * register + integer (mikhail) -- done
  * port bindings check 
  * Some large sample(s) (mikhail or sergei) -- not started
    * memory & cache
    * multiplier pool
    * some simple CPU
    * some simple interface
* Refactorings (mikhail)
  * wireInput / input etc. (make more or less the same) -- cancelled (bitInput now)
  * remove triggeredFunction -- done
  * function / stagedFunction together -- cancelled (function / stateFunction now)
  * do something with signal.value (mikhail) -- in process
    * rename to something? -- NO
    * use read / write? -- ???
    * try delegates? -- YES
    * use set / reset? -- ???
  * SysData (sergei / mikhail) -- almost done
    * remove all starting values of signals / registers
* Project organization
  * Maven
  * Kotlin tests

# Design Notes

## Tracing

Signals, ports, possibly also local variables...

## Arithmetic

SysInteger = fixed width integer. Width is known at runtime and (possibly) can be annotated at compile time. 
All bit operations are supported, like my[2] or even my[5..3]. Concatenation is supported (my cat your?).

SysFloat, SysDouble = simple float / double wrappers with possible access to mantissa and exponent as SysInteger.

TriState = { 0, 1, X }. Everything can have X state (including "Booleans", "Signals", "Integers" and so).

Types like Int or Bool are allowed but considered non-synthesizable (?)

## Data Storage

Kotlin arrays. Can be annotated by @FlipFlop or @RegisterFile.

SysMemory = module which simulates memory behaviour and is converted to memory during synthesis.

## Data Channels

SysSignal. X state is possible for Boolean signal, any bit of Integer signal, etc.

SysFifo.

## Loops

Can be annotated with @Unroll or @Pipeline(stages). 
In the latter case, @Stage annotation can be used inside the loop.
It's possible to use it on a wait() or m.b. just before some statements if we are inside a function.

## Functions / Threads

Functions can be simulated easily because they have waits only at the end. 
May be they can have extra delays to simulate complex computations.

Threads can have waits in arbitrary places so they require complex thread-based scheduler to be simulated.


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

TODO

* Features
** function builder (mikhail)
** memory module (veronika)
** SysBigInteger (valentin)
** SysFloat (?) (valentin)
** delayed happens (?) (sergey)
** ports with default value (?)
* Tests
** FIFO / buses (sergey)
** memory module (veronika)
** register + integer (veronika or valentin)
** port bindings check (?) (mikhail)
** Some large sample (?) (mikhail or sergey)
* Refactorings (mikhail)
** wireInput/ input etc. (make more or less the same)
** remove triggeredFunction (?)
** function / stagedFunction together (?)

# Design Notes

## Arithmetics

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


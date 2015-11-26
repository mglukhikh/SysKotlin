package sysk

// Very simple usage test
fun main(arg: Array<String>) {
    // integers
    val x: @Width(4) SysInteger = SysInteger.valueOf(5)
    val y: @Width(6) SysInteger = SysInteger.valueOf(9).extend(6)
    val z: @Width(6) SysInteger = x + y
    println(x)
    println(y)
    println(z)
    println(z[2])
    println(z[4,1])
    val v: @Width(12) SysInteger = y * z
    println(v)

    val scheduler = SysScheduler()
    // signals
    val first: SysSignal<Boolean> = SysSignal("first", false, scheduler)
    println("${first.name} = $first")
    first.value = true
    println("${first.name} = $first")
    first.update()
    println("${first.name} = $first")
    val second: SysSignal<Boolean> = SysSignal("second", true, scheduler)
    println(first == second)
    second.value = false
    println("$first, $second, ${first == second}")
    second.update()
    println("$first, $second, ${first == second}")

    // ports
    val connector: SysSignal<Boolean> = SysSignal("connector", false, scheduler)
    val input: SysInput<Boolean> = SysInput("input", null, connector)
    val output: SysOutput<Boolean> = SysOutput("output", null, connector)
    println("${input.name} = ${input.value}")
    output.value = true
    connector.update()
    println("${input.name} = ${input.value}")

    // clock
    val clock = SysClockedSignal("clock", time(20, TimeUnit.NS), scheduler)
    println("${scheduler.currentTime}: ${clock.name} = $clock")
    scheduler.start(time(55, TimeUnit.NS))
    println("${scheduler.currentTime}: ${clock.name} = $clock")
    scheduler.start(time(115, TimeUnit.NS))
    println("${scheduler.currentTime}: ${clock.name} = $clock")
    scheduler.start(time(145, TimeUnit.NS))
    println("${scheduler.currentTime}: ${clock.name} = $clock")
}
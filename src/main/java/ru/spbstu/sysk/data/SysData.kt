package ru.spbstu.sysk.data

import kotlin.reflect.KClass
import kotlin.reflect.companionObjectInstance

interface SysData

interface SysDataCompanion<out T : SysData> {
    val undefined: T
}

object UndefinedCollection {

    private val map = hashMapOf<KClass<out SysData>, SysData>()

    fun <T : SysData> register(type: KClass<T>, instance: T) {
        map[type] = instance
    }

    fun <T : SysData> undefined(type: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return map[type] as? T
    }
}

inline fun <reified T : SysData> undefined(): T {
    val kClass = T::class
    @Suppress("UNCHECKED_CAST")
    val undefined = UndefinedCollection.undefined(kClass)
            ?: (kClass.companionObjectInstance as? SysDataCompanion<T>)?.undefined
            ?: kClass.constructors.firstOrNull { it.parameters.isEmpty() }?.call()
            ?: throw AssertionError("$kClass has no registered undefined or default constructor")
    UndefinedCollection.register(kClass, undefined)
    return undefined
}
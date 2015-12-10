package sysk

import java.util.*
import kotlin.reflect.KClass

interface SysData

object UndefinedCollection {

    val map = HashMap<KClass<*>, SysData>()

    fun <T : SysData> register(type: KClass<T>, instance: T) {
        map[type] = instance
    }

    fun <T : SysData> undefined(type: KClass<T>): T? {
        return map[type] as? T
    }
}

inline fun <reified T : SysData> undefined(): T =
        UndefinedCollection.undefined(T::class) ?: throw AssertionError("${T::class} has no undefined status")

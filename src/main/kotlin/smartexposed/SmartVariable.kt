@file:Suppress("unused")
package smartexposed

import org.jetbrains.exposed.sql.Column
import kotlin.reflect.KProperty

fun <T> smartVariable(column: Column<T>): SmartVariable<T, T> {
    return SmartVariable(column = column) { it }
}

fun <T, V> smartVariable(column: Column<T>, initBlock: (T) -> V): SmartVariable<T, V> {
    return SmartVariable(column = column, initBlock = initBlock)
}

fun <T> smartVariableWithDefault(column: Column<T>, defaultValue: T): SmartVariableWithDefault<T, T> {
    return SmartVariableWithDefault(column = column, defaultValue = defaultValue) { it }
}

fun <T, V> smartVariableWithDefault(column: Column<T>, defaultValue: V, initBlock: (T) -> V): SmartVariableWithDefault<T, V> {
    return SmartVariableWithDefault(column = column, defaultValue = defaultValue, initBlock = initBlock)
}

open class SmartVariableWithDefault<T, V>(column: Column<T>, defaultValue: V, initBlock: (T) -> V): SmartVariable<T, V> (column, initBlock = initBlock) {
    init {
        this.value = defaultValue
    } }

open class SmartVariable<T, V>(
    open val column: Column<T>,
    val initBlock: (T)->V
) {
    var value: V? = null

    open fun init(value: T) {
        this.value = initBlock(value)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        @Suppress("UNCHECKED_CAST")
        return this.value as V
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.value = value
    }
}
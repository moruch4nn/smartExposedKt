@file:Suppress("unused")
package smartexposed

import org.jetbrains.exposed.sql.Column
import kotlin.reflect.KProperty

fun <T> smartVariable(column: Column<T>): SmartVariable<T, T> {
    return SmartVariable(column = column, initBlock = { it }, insertBlock = { it })
}

fun <T, V> smartVariable(column: Column<T>, initBlock: (T) -> V, insertBlock: (V) -> T): SmartVariable<T, V> {
    return SmartVariable(column = column, initBlock = initBlock, insertBlock = insertBlock)
}

fun <T> smartVariableWithDefault(column: Column<T>, defaultValue: T): SmartVariableWithDefault<T, T> {
    return SmartVariableWithDefault(column = column, defaultValue = defaultValue, initBlock = { it }, insertBlock = { it })
}

fun <T, V> smartVariableWithDefault(column: Column<T>, defaultValue: V, initBlock: (T) -> V, insertBlock: (V)->T): SmartVariableWithDefault<T, V> {
    return SmartVariableWithDefault(column = column, defaultValue = defaultValue, initBlock = initBlock, insertBlock = insertBlock)
}

open class SmartVariableWithDefault<T, V>(column: Column<T>, defaultValue: V, initBlock: (T) -> V, insertBlock: (V)->T): SmartVariable<T, V> (column, initBlock = initBlock, insertBlock = insertBlock) {
    init {
        this.value = defaultValue
    } }

open class SmartVariable<T, V>(
    open val column: Column<T>,
    val initBlock: (T)->V,
    val insertBlock: (V)->T
) {
    protected var value: V? = null
    var initialized = false

    open fun valueForInsert(): T {
        if(!initialized) { throw IllegalStateException("Variable is not initialized") }
        @Suppress("UNCHECKED_CAST")
        return insertBlock(this.value as V)
    }

    open fun init(value: T) {
        this.value = initBlock(value)
        this.initialized = true
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        if(!initialized) { throw IllegalStateException("Variable ${property.name} is not initialized") }
        @Suppress("UNCHECKED_CAST")
        return this.value as V
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.value = value
        this.initialized = true
    }
}
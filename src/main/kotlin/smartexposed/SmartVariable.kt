@file:Suppress("unused")
package smartexposed

import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import kotlin.reflect.KProperty

fun <T> smartVariable(column: Column<T>): SmartVariable<T, T> {
    return SmartVariable(column = column, initBlock = null, insertBlock = null)
}

fun <T, V> smartVariable(column: Column<T>, initBlock: (T) -> V, insertBlock: (V) -> T): SmartVariable<T, V> {
    return SmartVariable(column = column, initBlock = initBlock, insertBlock = insertBlock)
}

fun <T> smartVariableWithAlias(column: Column<T>, alias: Alias<Table>): SmartVariableWithAlias<T, T> {
    return SmartVariableWithAlias(column = column, alias = alias, initBlock = null, insertBlock = null)
}

fun <T, V> smartVariableWithAlias(column: Column<T>, alias: Alias<Table>, initBlock: (T) -> V, insertBlock: (V) -> T): SmartVariableWithAlias<T, V> {
    return SmartVariableWithAlias(column = column, alias = alias, initBlock = initBlock, insertBlock = insertBlock)
}

fun <T> smartVariableWithDefault(column: Column<T>, defaultValue: ()->T): SmartVariableWithDefault<T, T> {
    return SmartVariableWithDefault(column = column, defaultValue = defaultValue, initBlock = null, insertBlock = null)
}

fun <T, V> smartVariableWithDefault(column: Column<T>, defaultValue: ()->V, initBlock: (T) -> V, insertBlock: (V)->T): SmartVariableWithDefault<T, V> {
    return SmartVariableWithDefault(column = column, defaultValue = defaultValue, initBlock = initBlock, insertBlock = insertBlock)
}

fun <T> smartVariableWithAliasAndDefault(column: Column<T>, alias: Alias<Table>, defaultValue: ()->T): SmartVariableWithAliasAndDefault<T, T> {
    return SmartVariableWithAliasAndDefault(column = column, alias = alias, defaultValue = defaultValue, initBlock = null, insertBlock = null)
}

fun <T, V> smartVariableWithAliasAndDefault(column: Column<T>, alias: Alias<Table>, defaultValue: ()->V, initBlock: (T) -> V, insertBlock: (V)->T): SmartVariableWithAliasAndDefault<T, V> {
    return SmartVariableWithAliasAndDefault(column = column, alias = alias, defaultValue = defaultValue, initBlock = initBlock, insertBlock = insertBlock)
}

class SmartVariableWithAliasAndDefault<T, V>(column: Column<T> , alias: Alias<Table>, val defaultValue: ()->V, initBlock: ((T)->V)?, insertBlock: ((V)->T)?): SmartVariableWithAlias<T, V> (column, alias = alias, initBlock = initBlock, insertBlock = insertBlock) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V { return if(!this.initialized) { defaultValue() } else { super.getValue(thisRef, property) } }

    override fun valueForInsert(): Any? { return if(!initialized) { defaultValue() } else { super.valueForInsert() } }
}

open class SmartVariableWithAlias<T, V>(column: Column<T>, val alias: Alias<Table>, initBlock: ((T)->V)?, insertBlock: ((V)->T)?): SmartVariable<T, V> (column, initBlock = initBlock, insertBlock = insertBlock) {
    override fun exists(raw: ResultRow): Boolean {
        return raw.hasValue(alias[column])
    }

    override fun init(raw: ResultRow) {
        this._init(raw[alias[column]])
    }
}

class SmartVariableWithDefault<T, V>(column: Column<T>, val defaultValue: ()->V, initBlock: ((T)->V)?, insertBlock: ((V)->T)?): SmartVariable<T, V> (column, initBlock = initBlock, insertBlock = insertBlock) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V { return if(!this.initialized) { defaultValue() } else { super.getValue(thisRef, property) } }

    override fun valueForInsert(): Any? { return if(!initialized) { defaultValue() } else { super.valueForInsert() } }
}

open class SmartVariable<T: Any?, V: Any?>(
    open val column: Column<T>,
    val initBlock: ((T)->V)?,
    val insertBlock: ((V)->T)?
) {
    protected var value: V? = null
    var initialized = false

    open fun valueForInsert(): Any? {
        if(!initialized) { throw IllegalStateException("Variable is not initialized") }
        @Suppress("UNCHECKED_CAST")
        return (insertBlock?.let { it(this.value as V) }?: this.value) as Any
    }

    open fun exists(raw: ResultRow): Boolean { return raw.hasValue(column) }

    open fun init(raw: ResultRow) {
        this._init(raw[column])
    }

    @Suppress("FunctionName")
    protected fun _init(value: T) {
        @Suppress("UNCHECKED_CAST")
        this.value = (initBlock?.let { it(value) } ?:value) as V
        this.initialized = true
    }

    open operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        if(!initialized) { throw IllegalStateException("Variable ${property.name} is not initialized") }
        @Suppress("UNCHECKED_CAST")
        return this.value as V
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.value = value
        this.initialized = true
    }
}
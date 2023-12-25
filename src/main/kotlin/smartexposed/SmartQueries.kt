@file:Suppress("unused")
package smartexposed

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


inline fun <reified T: Any> Query.executeAsSmart(): List<T> = this.map(T::class.initializeWithEmptyConstructor()::onInjectFrom)

inline fun <reified T: Any> T.smartVariables(): List<SmartVariable<*, *>> {
    return this::class.declaredMemberProperties.mapNotNull { field ->
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val delegate = (field as KProperty1<Any?, Any?>).getDelegate(this) ?: return@mapNotNull null
        if(delegate !is SmartVariable<*, *>) { return@mapNotNull null }
        return@mapNotNull delegate
    }
}

inline fun <reified T: Any> ResultRow.injectTo(instance: T) {
    instance.smartVariables().forEach { delegate ->
        @Suppress("UNCHECKED_CAST")
        (delegate as SmartVariable<Any?, Any?>).init(this[delegate.column])
    }
}

inline fun <reified I: Any, reified T: Table> T.smartInsert(instance: I): InsertStatement<Number> {
    return this.insert {  insertStatement ->
        instance.smartVariables().forEach { delegate ->
            @Suppress("UNCHECKED_CAST")
            insertStatement[delegate.column as Column<Any?>] = (delegate as SmartVariable<Any?, Any?>).value
        } }
}

inline fun <reified I: Any, reified T: Table> T.smartUpsert(instance: I): InsertStatement<Long> {
    return this.upsert {  insertStatement ->
        instance.smartVariables().forEach { delegate ->
            @Suppress("UNCHECKED_CAST")
            insertStatement[delegate.column as Column<Any?>] = (delegate as SmartVariable<Any?, Any?>).value
        } }
}

inline fun <reified I: Any, reified T: Table> T.smartInsertIgnore(instance: I): InsertStatement<Long> {
    return this.insertIgnore {  insertStatement ->
        instance.smartVariables().forEach { delegate ->
            @Suppress("UNCHECKED_CAST")
            insertStatement[delegate.column as Column<Any?>] = (delegate as SmartVariable<Any?, Any?>).value
        } }
}

inline fun <reified T: Any> ResultRow.onInjectTo(instance: T): T {
    this.injectTo(instance)
    return instance
}

inline fun <reified T: Any> T.injectFrom(resultRow: ResultRow) = resultRow.onInjectTo(this)

inline fun <reified T: Any> T.onInjectFrom(resultRow: ResultRow): T {
    this.injectFrom(resultRow)
    return this
}

inline fun <reified T: Any> Class<T>.initializeWithEmptyConstructor(): T { return (this.constructors.firstOrNull { it.parameters.isEmpty() }?: error("No empty constructor")).newInstance() as T }

inline fun <reified T: Any> KClass<T>.initializeWithEmptyConstructor(): T = this.java.initializeWithEmptyConstructor()

inline fun <reified T: Any> Collection<ResultRow>.deserializeAsSmart(): List<T> {
    val resultList = this.map(T::class.initializeWithEmptyConstructor()::onInjectFrom)
    return resultList
}
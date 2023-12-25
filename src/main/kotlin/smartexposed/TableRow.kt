@file:Suppress("unused")
package smartexposed

import org.jetbrains.exposed.sql.*

/**
 * Inheriting this is optional.
 * Inheritance makes operations easier.
 */
abstract class SmartTable(val table: Table) {
    fun insert() = table.smartInsert(this)
    fun insertIgnore() = table.smartInsertIgnore(this)
    fun update(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null, limit: Int? = null) = table.smartUpdate(this, where, limit)

    fun upsert(vararg keys: Column<*>, onUpdate: List<Pair<Column<*>, Expression<*>>>? = null, where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) = table.smartUpsert(this, *keys, onUpdate = onUpdate, where = where)

    fun pull(where: (SqlExpressionBuilder.() -> Op<Boolean>)) = this.onInjectFrom(table.select(where = where).firstOrNull()?:throw Exception("No rows found"))
}
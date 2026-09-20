// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaError
import io.github.taoguan.luaj.LuaTable
import io.github.taoguan.luaj.LuaValue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command

/**
 * Conversion helpers between Kotlin/JVM values and Lua values as used by
 * the Lua rule engine.
 */
internal object LuaConversion {
    /**
     * Convert a Kotlin value to the closest Lua representation.
     *
     * Lists are turned into (1-based) Lua sequences, maps into tables with
     * string keys, paths into their string representation and everything
     * else which is not a primitive is wrapped into a userdatum.
     */
    // one branch per supported type: the complexity mirrors the type mapping
    @Suppress("CyclomaticComplexMethod")
    fun toLua(value: Any?): LuaValue = when (value) {
        null -> LuaValue.NIL
        is LuaValue -> value
        is String -> LuaValue.valueOf(value)
        is Boolean -> LuaValue.valueOf(value)
        is Int -> LuaValue.valueOf(value)
        is Long -> LuaValue.valueOf(value.toDouble())
        is Float -> LuaValue.valueOf(value.toDouble())
        is Double -> LuaValue.valueOf(value)
        is MPPPath -> LuaValue.valueOf(value.toString())
        is List<*> -> LuaValue.tableOf().also { table ->
            value.forEachIndexed { i, element ->
                table.set(i + 1, toLua(element))
            }
        }
        is Map<*, *> -> LuaValue.tableOf().also { table ->
            value.forEach { (k, v) ->
                table.set(k.toString(), toLua(v))
            }
        }
        else -> LuaValue.userdataOf(value)
    }

    /**
     * Convert a Lua value back to a Kotlin value. Sequences become lists,
     * other tables become maps with string keys and userdata are unwrapped.
     */
    fun fromLua(value: LuaValue): Any? = when {
        value.isnil() -> null
        value.type() == LuaValue.TBOOLEAN -> value.toboolean()
        value.type() == LuaValue.TNUMBER -> {
            val double = value.todouble()
            when {
                // integral in-range numbers become integers; out-of-range
                // integrals stay exact as longs because toint() would
                // silently wrap them into unrelated values
                double % 1.0 == 0.0 &&
                    double in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble() ->
                    double.toInt()
                double % 1.0 == 0.0 -> double.toLong()
                else -> double
            }
        }
        value.type() == LuaValue.TSTRING -> value.tojstring()
        value is LuaTable -> tableToKotlin(value)
        value.isuserdata() -> value.touserdata()
        else -> value.tojstring()
    }

    private fun tableToKotlin(table: LuaTable): Any {
        val keys = table.keys()
        val isSequence = keys.withIndex().all { (i, key) ->
            key.isnumber() && key.toint() == i + 1
        }
        return if (isSequence) {
            keys.map { key -> fromLua(table.get(key)) }
        } else {
            keys.associate { key -> key.tojstring() to fromLua(table.get(key)) }
        }
    }

    /**
     * Convert the result of a Lua flag evaluation into a list of strings
     * (mirroring the MVEL semantics where a flag may return a string or a
     * list of strings).
     */
    fun flagResultToList(value: LuaValue): List<String> = when {
        value.isnil() -> emptyList()
        value is LuaTable -> flatten(value).map { it.toString() }
        else -> listOf(value.tojstring())
    }

    /**
     * Convert the result of a Lua command evaluation into a flat list of
     * objects (commands and booleans in well-behaved rules).
     */
    fun commandResultToList(value: LuaValue): List<Any> = when {
        value.isnil() -> emptyList()
        value is LuaTable -> flatten(value)
        else -> listOfNotNull(fromLua(value))
    }

    private fun flatten(table: LuaTable): List<Any> =
        table.keys().flatMap { key ->
            val element = table.get(key)
            if (element is LuaTable) {
                flatten(element)
            } else {
                listOfNotNull(fromLua(element))
            }
        }

    /**
     * Build the Lua representation of a file reference (the `reference`
     * value available to commands and flags).
     */
    fun referenceToLua(path: MPPPath): LuaTable =
        LuaValue.tableOf().also { table ->
            table.set("fileName", LuaValue.valueOf(path.fileName))
            table.set("path", LuaValue.valueOf(path.toString()))
            table.set("parent", LuaValue.valueOf(path.parent.toString()))
        }

    /**
     * Run [block] converting Lua errors raised during the evaluation into
     * arara exceptions; nested arara exceptions (e.g. raised by `throwError`)
     * are unwrapped and rethrown as-is.
     */
    @Throws(AraraException::class)
    fun <T> withLuaErrorHandling(block: () -> T): T = try {
        block()
    } catch (exception: LuaError) {
        val araraCause = generateSequence<Throwable>(exception) { it.cause }
            .firstOrNull { it is AraraException } as? AraraException
        throw araraCause ?: AraraException(
            exception.message ?: "The Lua evaluation failed.",
            exception
        )
    }

    /**
     * Unwrap a userdatum into a [Command] if possible.
     */
    fun toCommand(value: LuaValue): Command? =
        value.takeIf { it.isuserdata() }?.touserdata() as? Command
}

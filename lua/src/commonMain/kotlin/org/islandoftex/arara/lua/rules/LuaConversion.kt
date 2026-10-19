// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.localization.LanguageController
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaNumber
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction

/** The typed boundary between runtime values and the Luak interpreter. */
@Suppress("TooManyFunctions")
internal object LuaConversion {
    // Lua numbers above this exclusive bound cannot be converted to a Kotlin Long.
    private const val LONG_UPPER_BOUND = 9223372036854775808.0

    fun toLua(value: Any?): LuaValue = toLua(value, mutableListOf())

    @Suppress("CyclomaticComplexMethod")
    private fun toLua(value: Any?, visiting: MutableList<Any>): LuaValue = when (value) {
        null -> LuaValue.NIL
        is LuaValue -> value
        is String -> LuaValue.valueOf(value)
        is Boolean -> LuaValue.valueOf(value)
        is Int -> LuaValue.valueOf(value)
        is Long -> longToLua(value)
        is Float -> LuaValue.valueOf(value.toDouble())
        is Double -> LuaValue.valueOf(value)
        is List<*> -> within(value, visiting) {
            LuaValue.tableOf().also { table ->
                value.forEachIndexed { index, element -> table.set(index + 1, toLua(element, visiting)) }
            }
        }
        is Map<*, *> -> within(value, visiting) {
            val keys = value.keys.map { it.toString() }
            if (keys.toSet().size != keys.size) throw LuaHelperException("Map keys collide as Lua strings.")
            LuaValue.tableOf().also { table ->
                value.forEach { (key, element) -> table.set(key.toString(), toLua(element, visiting)) }
            }
        }
        else -> LuaValue.userdataOf(value)
    }

    /** Luak uses Int or Double; refuse a Long that would silently lose precision. */
    private fun longToLua(value: Long): LuaValue {
        val number = value.toDouble()
        if (number >= LONG_UPPER_BOUND || number.toLong() != value) {
            throw LuaHelperException("Integer $value cannot be represented exactly as a Lua number.")
        }
        return LuaValue.valueOf(number)
    }

    fun fromLua(value: LuaValue): Any? = fromLua(value, mutableListOf())

    @Suppress("CyclomaticComplexMethod")
    private fun fromLua(value: LuaValue, visiting: MutableList<Any>): Any? = when {
        value.isnil() -> null
        value is LuaBoolean -> value.toboolean()
        value is LuaNumber -> numberToKotlin(value.todouble())
        value is LuaString -> value.tojstring()
        value is LuaTable -> within(value, visiting) {
            val sequence = sequence(value)
            if (sequence != null) {
                sequence.map { fromLua(it, visiting) }
            } else {
                val keys = value.keys()
                val names = keys.map { it.tojstring() }
                if (names.toSet().size != names.size) throw LuaHelperException("Lua table keys collide as strings.")
                keys.associate { it.tojstring() to fromLua(value[it], visiting) }
            }
        }
        value.isuserdata() -> value.touserdata()
        else -> value.tojstring()
    }

    private fun numberToKotlin(number: Double): Any = when {
        number % 1.0 != 0.0 -> number
        number in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble() -> number.toInt()
        number >= Long.MIN_VALUE.toDouble() && number < LONG_UPPER_BOUND -> number.toLong()
        else -> number
    }

    /** Sequence order comes from indices, not the table's key enumeration. */
    private fun sequence(table: LuaTable): List<LuaValue>? {
        val keys = table.keys()
        return if (keys.all { it is LuaNumber && it.isint() && it.toint() in 1..keys.size }) {
            (1..keys.size).map { table[it] }
        } else null
    }

    private inline fun <T> within(value: Any, visiting: MutableList<Any>, block: () -> T): T {
        if (visiting.any { it === value }) {
            throw LuaHelperException("Cyclic tables and collections cannot be converted.")
        }
        visiting.add(value)
        return try {
            block()
        } finally {
            visiting.removeAt(visiting.lastIndex)
        }
    }

    fun badArgument(helper: String, index: Int, expected: String, actual: LuaValue): LuaHelperException =
        LuaHelperException("bad argument #$index to '$helper' ($expected expected, got ${actual.typename()})")

    fun LuaValue.stringArgument(helper: String, index: Int): String =
        (this as? LuaString)?.tojstring() ?: throw badArgument(helper, index, "string", this)

    fun LuaValue.numberArgument(helper: String, index: Int): Int =
        (this as? LuaNumber)?.takeIf { it.isint() }?.toint()
            ?: throw badArgument(helper, index, "32-bit integer", this)

    fun LuaValue.booleanArgument(helper: String, index: Int): Boolean =
        (this as? LuaBoolean)?.toboolean() ?: throw badArgument(helper, index, "boolean", this)

    fun LuaValue.pathArgument(helper: String, index: Int): MPPPath = when {
        isuserdata() -> touserdata() as? MPPPath ?: throw badArgument(helper, index, "path", this)
        this is LuaString -> MPPPath(tojstring())
        else -> throw badArgument(helper, index, "path", this)
    }

    fun LuaValue.stringListArgument(helper: String, index: Int): List<String> {
        val values = (this as? LuaTable)?.let { sequence(it) }
            ?: throw badArgument(helper, index, "list of strings", this)
        return values.map {
            (it as? LuaString)?.tojstring() ?: throw badArgument(helper, index, "list of strings", this)
        }
    }

    fun booleanResult(value: LuaValue, source: String): Boolean =
        (value as? LuaBoolean)?.toboolean() ?: throw AraraException(
            LanguageController.messages.ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN,
            LuaHelperException("bad result from '$source' (boolean expected, got ${value.typename()})")
        )

    fun flagResultToList(value: LuaValue): List<String> = when {
        value.isnil() -> emptyList()
        value is LuaTable -> flatten(value, mutableListOf()).map { it.toString() }
        else -> listOf(value.tojstring())
    }

    fun commandResultToList(value: LuaValue): List<Any> = when {
        value.isnil() -> emptyList()
        value is LuaTable -> flatten(value, mutableListOf())
        else -> listOfNotNull(fromLua(value))
    }

    private fun flatten(table: LuaTable, visiting: MutableList<Any>): List<Any> = within(table, visiting) {
        val values = sequence(table) ?: table.keys().map { table[it] }
        values.flatMap { value ->
            if (value is LuaTable) flatten(value, visiting) else listOfNotNull(fromLua(value))
        }
    }

    fun referenceToLua(path: MPPPath): LuaTable = LuaValue.tableOf().also { table ->
        table.set("file_name", LuaValue.valueOf(path.fileName))
        table.set("path", LuaValue.valueOf(path.toString()))
        table.set("parent", LuaValue.valueOf(path.parent.toString()))
    }

    fun luaFunction(body: (Varargs) -> LuaValue): LuaValue = object : VarArgFunction() {
        override fun invoke(args: Varargs): Varargs = body(args)
    }

    fun toCommand(value: LuaValue): Command? = value.takeIf { it.isuserdata() }?.touserdata() as? Command

    fun diagnostic(error: LuaError): String {
        val text = error.getLuaMessage() ?: error.message ?: error.toString()
        val location = error.fileline
        return if (location.isNullOrBlank() || text.contains(location)) text else "$location: $text"
    }

    /** Luak keeps wrapped callback failures in luaCause rather than Throwable.cause. */
    fun <T> withLuaErrorHandling(message: String, block: () -> T): T = try {
        block()
    } catch (exception: LuaHelperException) {
        throw AraraException(message, exception)
    } catch (exception: LuaError) {
        val araraCause = generateSequence<Throwable>(exception) {
            if (it is LuaError) it.luaCause ?: it.cause else it.cause
        }.filterIsInstance<AraraException>().firstOrNull()
        throw when (araraCause) {
            is LuaHelperException -> AraraException(message, araraCause)
            null -> AraraException(message, Exception(diagnostic(exception), exception))
            else -> araraCause
        }
    }
}

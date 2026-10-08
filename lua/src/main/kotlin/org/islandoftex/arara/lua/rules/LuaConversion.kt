// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.Globals
import io.github.taoguan.luaj.LuaBoolean
import io.github.taoguan.luaj.LuaError
import io.github.taoguan.luaj.LuaInteger
import io.github.taoguan.luaj.LuaNumber
import io.github.taoguan.luaj.LuaString
import io.github.taoguan.luaj.LuaTable
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.Varargs
import io.github.taoguan.luaj.lib.VarArgFunction
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.RuleMethods

/**
 * Conversion helpers between Kotlin/JVM values and Lua values as used by
 * the Lua rule engine.
 */
// the object is the engine's single typed boundary with Lua: its members
// are small overloads of one conversion concern, and the count mirrors
// the Lua type surface rather than accumulated responsibilities —
// splitting the members would scatter the boundary or leave a fragment
// at the threshold again
@Suppress("TooManyFunctions")
internal object LuaConversion {
    /**
     * Convert a Kotlin value to the closest Lua representation.
     *
     * Lists are turned into (1-based) Lua sequences, maps into tables with
     * string keys, paths are wrapped into file userdata (so round-trips,
     * e.g. through the session, preserve the object and the helpers
     * dispatch to the file overloads) and everything else which is not
     * a primitive is wrapped into a userdatum. Longs become exact
     * integers while they fit; beyond the exact double range the Lua
     * number loses precision.
     */
    // one branch per supported type: the complexity mirrors the type mapping
    @Suppress("CyclomaticComplexMethod")
    fun toLua(value: Any?): LuaValue = when (value) {
        null -> LuaValue.NIL
        is LuaValue -> value
        is String -> LuaValue.valueOf(value)
        is Boolean -> LuaValue.valueOf(value)
        is Int -> LuaValue.valueOf(value)
        is Long -> LuaInteger.valueOf(value)
        is Float -> LuaValue.valueOf(value.toDouble())
        is Double -> LuaValue.valueOf(value)
        is MPPPath -> LuaValue.userdataOf(value)
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

    /**
     * Converts a Lua table to its Kotlin shape: a consecutive
     * one-based sequence becomes a list, anything else a map.
     */
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
     * Build the error for a helper argument of the wrong type, mirroring
     * the argument errors of Lua's own library functions: Lua's operators
     * coerce, its library functions type-check, and the arara helpers
     * are library functions. The error is a [LuaHelperException], so
     * the rule evaluation can wrap it behind the localized runtime
     * errors of the engine.
     */
    fun badArgument(
        helper: String,
        index: Int,
        expected: String,
        actual: LuaValue
    ): AraraException = LuaHelperException(
        "bad argument #$index to '$helper' ($expected expected, " +
            "got ${actual.typename()})"
    )

    /**
     * Extract the value as a Lua string, rejecting other types: Lua's
     * implicit coercions do not apply at the helper boundary, so numbers
     * do not silently become strings (convert explicitly with `tostring`
     * instead). The instance check is deliberate — in this LuaJ fork
     * `isstring()` and `isnumber()` are true for both numbers and
     * numeric strings.
     */
    fun LuaValue.stringArgument(helper: String, index: Int): String =
        (this as? LuaString)?.tojstring()
            ?: throw badArgument(helper, index, "string", this)

    /**
     * Extract the value as a Lua number, rejecting other types.
     */
    fun LuaValue.numberArgument(helper: String, index: Int): Int =
        (this as? LuaNumber)?.toint()
            ?: throw badArgument(helper, index, "number", this)

    /**
     * Extract the value as a Lua boolean, rejecting other types.
     */
    fun LuaValue.booleanArgument(helper: String, index: Int): Boolean =
        (this as? LuaBoolean)?.toboolean()
            ?: throw badArgument(helper, index, "boolean", this)

    /**
     * Extract the value as a path: a handle from the path helpers
     * (`to_file`, `current_file`) or a real Lua string, read as a
     * path. The runtime dispatch is the doctrine's overloading —
     * the same name answers both argument shapes, the way Lua's own
     * library functions do. The conditional family's
     * file-and-extension dispatch stays its own: there a string
     * means an extension, here a string means a path.
     */
    fun LuaValue.pathArgument(helper: String, index: Int): MPPPath =
        when {
            isuserdata() -> touserdata() as? MPPPath
                ?: throw badArgument(helper, index, "path", this)
            this is LuaString -> MPPPath(tojstring())
            else -> throw badArgument(helper, index, "path", this)
        }

    /**
     * Extract the value as a list of strings, rejecting other shapes
     * and non-string elements alike.
     */
    fun LuaValue.stringListArgument(
        helper: String,
        index: Int
    ): List<String> {
        val list = fromLua(this) as? List<*>
            ?: throw badArgument(helper, index, "list of strings", this)
        return list.map { element ->
            element as? String
                ?: throw badArgument(helper, index, "list of strings", this)
        }
    }

    /**
     * Convert the result of a rule's `exit` closure: it has to be a real
     * boolean — numbers and strings are not silently truth-tested, which
     * (given that `toboolean()` accepts nearly everything in this fork)
     * would make every exit code pass. The non boolean case reports the
     * same localized error the MVEL dialect produces, with the type
     * detail available as the cause.
     */
    fun booleanResult(value: LuaValue, source: String): Boolean =
        (value as? LuaBoolean)?.toboolean()
            ?: throw AraraException(
                LanguageController.messages
                    .errorInterpreterWrongExitClosureReturn,
                LuaHelperException(
                    "bad result from '$source' (boolean expected, " +
                        "got ${value.typename()})"
                )
            )

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

    /**
     * Flattens a Lua table into a list, descending into nested
     * tables and dropping nil entries.
     */
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
            table.set("file_name", LuaValue.valueOf(path.fileName))
            table.set("path", LuaValue.valueOf(path.toString()))
            table.set("parent", LuaValue.valueOf(path.parent.toString()))
        }

    /**
     * Wrap a Kotlin function as a Lua function value; both helper
     * environments build their bindings with it.
     */
    fun luaFunction(body: (Varargs) -> LuaValue): LuaValue =
        object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs = body(args)
        }

    /**
     * Derive the snake_case spelling of a helper name: the Lua-idiomatic
     * alias every registered helper is additionally bound under, so
     * rules read like Lua while the historic camelCase spelling of the
     * shared helper vocabulary stays available throughout the lifetime
     * of the deprecated engine.
     */
    fun String.snakeCase(): String =
        fold(StringBuilder()) { builder, char ->
            if (char.isUpperCase()) {
                builder.append('_').append(char.lowercaseChar())
            } else {
                builder.append(char)
            }
        }.toString()

    /**
     * Bind a helper under both spellings: the historic camelCase name
     * and its derived snake_case alias. Each spelling receives its own
     * wrapper, so argument errors name the spelling the rule actually
     * called; a name that is already snake_case has no distinct alias
     * and binds once.
     */
    fun bindSpelled(
        globals: Globals,
        name: String,
        body: (name: String, args: Varargs) -> LuaValue,
    ) {
        globals.set(name, luaFunction { args -> body(name, args) })
        val alias = name.snakeCase()
        if (alias != name) {
            globals.set(alias, luaFunction { args -> body(alias, args) })
        }
    }

    /**
     * Build the session as a table of operations, as Lua cannot call
     * methods on the underlying object directly; both helper
     * environments expose the very same shape.
     */
    fun sessionTable(): LuaValue {
        val session = RuleMethods.getSession()
        return LuaValue.tableOf().also { table ->
            table.set(
                "get",
                luaFunction { args ->
                    val key = args.arg(1)
                        .stringArgument("session.get", 1)
                    toLua(session.get(key))
                }
            )
            table.set(
                "put",
                luaFunction { args ->
                    val key = args.arg(1)
                        .stringArgument("session.put", 1)
                    val value = fromLua(args.arg(2))
                        ?: throw badArgument(
                            "session.put", 2, "value", args.arg(2)
                        )
                    session.put(key, value)
                    LuaValue.NIL
                }
            )
            table.set(
                "contains",
                luaFunction { args ->
                    val key = args.arg(1)
                        .stringArgument("session.contains", 1)
                    LuaValue.valueOf(session.contains(key))
                }
            )
            table.set(
                "remove",
                luaFunction { args ->
                    val key = args.arg(1)
                        .stringArgument("session.remove", 1)
                    session.remove(key)
                    LuaValue.NIL
                }
            )
            table.set(
                "clear",
                luaFunction { _ ->
                    session.clear()
                    LuaValue.NIL
                }
            )
        }
    }

    /**
     * Run [block] converting Lua errors raised during the evaluation
     * into arara exceptions: plain Lua errors and typed helper
     * failures surface behind the provided localized [message],
     * while other arara exceptions keep flowing as themselves -
     * authored `throwError` errors among them, which stay primary
     * and carry the rule header the interpreter prefixes.
     */
    @Throws(AraraException::class)
    fun <T> withLuaErrorHandling(
        message: String,
        block: () -> T
    ): T = try {
        block()
    } catch (exception: LuaError) {
        val araraCause = generateSequence<Throwable>(exception) { it.cause }
            .firstOrNull { it is AraraException } as? AraraException
        throw when (araraCause) {
            is LuaHelperException -> AraraException(message, araraCause)
            null -> AraraException(message, exception)
            else -> araraCause
        }
    }

    /**
     * Unwrap a userdatum into a [Command] if possible.
     */
    fun toCommand(value: LuaValue): Command? =
        value.takeIf { it.isuserdata() }?.touserdata() as? Command
}

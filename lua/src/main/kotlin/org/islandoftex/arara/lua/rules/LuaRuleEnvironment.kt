// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.Globals
import io.github.taoguan.luaj.LuaError
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.Varargs
import io.github.taoguan.luaj.lib.VarArgFunction
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.rules.RuleMethods

import org.islandoftex.arara.lua.rules.LuaConversion.badArgument
import org.islandoftex.arara.lua.rules.LuaConversion.booleanArgument
import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.islandoftex.arara.lua.rules.LuaConversion.numberArgument
import org.islandoftex.arara.lua.rules.LuaConversion.stringArgument
import org.islandoftex.arara.lua.rules.LuaConversion.stringListArgument
import org.islandoftex.arara.lua.rules.LuaConversion.toLua

/**
 * Set up the global environment rule scripts are evaluated in.
 *
 * The environment exposes the same helper methods MVEL rules enjoy (both
 * dialects bind the very helpers from the core module) plus a few Lua
 * conveniences (`contains`, `keys`, `join`).
 */
internal object LuaRuleEnvironment {
    /**
     * The Lua prelude with pure-Lua helpers available to all rules.
     */
    private const val PRELUDE = """
        function contains(list, value)
            for _, v in ipairs(list) do
                if v == value then return true end
            end
            return false
        end
        function keys(map)
            local result = {}
            for k in pairs(map) do result[#result + 1] = tostring(k) end
            table.sort(result)
            return result
        end
        function join(list, separator)
            local strings = {}
            for i, v in ipairs(list) do strings[i] = tostring(v) end
            return table.concat(strings, separator or " ")
        end
        function isList(value) return type(value) == "table" end
        function isMap(value) return type(value) == "table" end
        function isString(value) return type(value) == "string" end
        function isBoolean(value) return type(value) == "boolean" end
    """

    /**
     * Create a fresh global environment for one rule file.
     */
    fun newGlobals(): Globals {
        val globals = JsePlatform.standardGlobals()
        bindHelpers(globals)
        globals.load(PRELUDE, "arara-prelude").call()
        return globals
    }

    private fun luaFunction(body: (Varargs) -> LuaValue): LuaValue =
        object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs = body(args)
        }

    private fun Varargs.kotlinArgs(from: Int = 1): List<Any?> =
        (from..narg()).map { fromLua(arg(it)) }

    private fun truthiness(value: LuaValue): Boolean = when {
        value.isnil() -> false
        value.type() == LuaValue.TBOOLEAN -> value.toboolean()
        else -> RuleMethods.isTrue(value.tojstring())
    }

    private fun isEmptyValue(value: LuaValue): Boolean = when {
        value.isnil() -> true
        value.istable() -> value.length() == 0
        else -> value.tojstring().isEmpty()
    }

    // the bindings forward to Java vararg methods (hence the spread
    // operators), access Lua arguments positionally (hence the argument
    // index numbers) and report binding errors where they occur
    @Suppress(
        "LongMethod", "ComplexMethod",
        "SpreadOperator", "MagicNumber", "ThrowsCount"
    )
    private fun bindHelpers(globals: Globals) {
        globals.set(
            "throwError",
            luaFunction { args ->
                throw AraraException(
                    args.arg(1).stringArgument("throwError", 1)
                )
            }
        )
        globals.set(
            "getCommand",
            luaFunction { args ->
                LuaValue.userdataOf(
                    RuleMethods.getCommand(*args.kotlinArgs().toTypedArray())
                )
            }
        )
        globals.set(
            "getCommandWithWorkingDirectory",
            luaFunction { args ->
                LuaValue.userdataOf(
                    RuleMethods.getCommandWithWorkingDirectory(
                        args.arg(1).tojstring(),
                        *args.kotlinArgs(from = 2).toTypedArray()
                    )
                )
            }
        )
        globals.set(
            "isTrue",
            luaFunction { args ->
                conditionalValue(args, positive = true)
            }
        )
        globals.set(
            "isFalse",
            luaFunction { args ->
                conditionalValue(args, positive = false)
            }
        )
        globals.set(
            "isEmpty",
            luaFunction { args ->
                when (args.narg()) {
                    1 -> LuaValue.valueOf(isEmptyValue(args.arg(1)))
                    else -> if (isEmptyValue(args.arg(1)))
                        args.arg(2)
                    else
                        args.arg(3).takeUnless { it.isnil() }
                            ?: LuaValue.valueOf("")
                }
            }
        )
        globals.set(
            "isNotEmpty",
            luaFunction { args ->
                when (args.narg()) {
                    1 -> LuaValue.valueOf(!isEmptyValue(args.arg(1)))
                    else -> if (!isEmptyValue(args.arg(1)))
                        args.arg(2)
                    else
                        args.arg(3).takeUnless { it.isnil() }
                            ?: LuaValue.valueOf("")
                }
            }
        )
        globals.set(
            "getBasename",
            luaFunction { args ->
                LuaValue.valueOf(
                    RuleMethods.getBasename(
                        args.arg(1).stringArgument("getBasename", 1)
                    )
                )
            }
        )
        globals.set(
            "getFiletype",
            luaFunction { args ->
                LuaValue.valueOf(
                    RuleMethods.getFiletype(
                        args.arg(1).stringArgument("getFiletype", 1)
                    )
                )
            }
        )
        globals.set(
            "getOriginalFile",
            luaFunction {
                LuaValue.valueOf(RuleMethods.originalFile)
            }
        )
        globals.set(
            "getOriginalReference",
            luaFunction {
                LuaValue.valueOf(RuleMethods.originalReference.toString())
            }
        )
        globals.set(
            "halt",
            luaFunction { args ->
                RuleMethods.halt(
                    args.arg(1).takeUnless { it.isnil() }
                        ?.numberArgument("halt", 1) ?: 0
                )
                LuaValue.NIL
            }
        )
        globals.set(
            "buildString",
            luaFunction { args ->
                LuaValue.valueOf(
                    RuleMethods.buildString(*args.kotlinArgs().toTypedArray())
                )
            }
        )
        globals.set(
            "trimSpaces",
            luaFunction { args ->
                LuaValue.valueOf(
                    RuleMethods.trimSpaces(
                        args.arg(1).stringArgument("trimSpaces", 1)
                    )
                )
            }
        )
        globals.set(
            "replicatePattern",
            luaFunction { args ->
                toLua(
                    RuleMethods.replicatePattern(
                        args.arg(1).stringArgument("replicatePattern", 1),
                        args.arg(2).stringListArgument("replicatePattern", 2)
                    )
                )
            }
        )
        globals.set(
            "isVerboseMode",
            luaFunction {
                LuaValue.valueOf(RuleMethods.isVerboseMode())
            }
        )
        globals.set(
            "isOnPath",
            luaFunction { args ->
                LuaValue.valueOf(
                    RuleMethods.isOnPath(
                        args.arg(1).stringArgument("isOnPath", 1)
                    )
                )
            }
        )
        listOf(
            "isWindows" to { RuleMethods.isWindows() },
            "isLinux" to { RuleMethods.isLinux() },
            "isMac" to { RuleMethods.isMac() },
            "isUnix" to { RuleMethods.isUnix() },
            "isCygwin" to { RuleMethods.isCygwin() }
        ).forEach { (name, check) ->
            globals.set(
                name,
                luaFunction { args ->
                    when (args.narg()) {
                        0 -> LuaValue.valueOf(check())
                        else -> if (check()) args.arg(1) else args.arg(2)
                    }
                }
            )
        }
        globals.set(
            "unsafelyExecuteSystemCommand",
            luaFunction { args ->
                val command = LuaConversion.toCommand(args.arg(1))
                    ?: throw badArgument(
                        "unsafelyExecuteSystemCommand", 1,
                        "command", args.arg(1)
                    )
                val (code, output) = RuleMethods
                    .unsafelyExecuteSystemCommand(command)
                LuaValue.tableOf().also {
                    it.set("first", LuaValue.valueOf(code))
                    it.set("second", LuaValue.valueOf(output))
                }
            }
        )
        globals.set(
            "listFilesByExtensions",
            luaFunction { args ->
                toLua(
                    RuleMethods.listFilesByExtensions(
                        args.arg(1)
                            .stringArgument("listFilesByExtensions", 1),
                        args.arg(2).stringListArgument(
                            "listFilesByExtensions", 2
                        ),
                        args.arg(3)
                            .booleanArgument("listFilesByExtensions", 3)
                    ).map { it.toString() }
                )
            }
        )
        globals.set(
            "listFilesByPatterns",
            luaFunction { args ->
                toLua(
                    RuleMethods.listFilesByPatterns(
                        args.arg(1)
                            .stringArgument("listFilesByPatterns", 1),
                        args.arg(2).stringListArgument(
                            "listFilesByPatterns", 2
                        ),
                        args.arg(3)
                            .booleanArgument("listFilesByPatterns", 3)
                    ).map { it.toString() }
                )
            }
        )
        globals.set(
            "writeToFile",
            luaFunction { args ->
                val path = args.arg(1).stringArgument("writeToFile", 1)
                val content = fromLua(args.arg(2))
                val append = args.arg(3).booleanArgument("writeToFile", 3)
                val result = if (content is List<*>) {
                    RuleMethods.writeToFile(
                        path,
                        content.map { it.toString() },
                        append
                    )
                } else {
                    RuleMethods.writeToFile(
                        path,
                        content.toString(),
                        append
                    )
                }
                LuaValue.valueOf(result)
            }
        )
        globals.set(
            "readFromFile",
            luaFunction { args ->
                toLua(
                    RuleMethods.readFromFile(
                        args.arg(1).stringArgument("readFromFile", 1)
                    )
                )
            }
        )
        globals.set(
            "isSubdirectory",
            luaFunction { args ->
                LuaValue.valueOf(
                    RuleMethods.isSubdirectory(
                        MPPPath(
                            args.arg(1).stringArgument("isSubdirectory", 1)
                        )
                    )
                )
            }
        )
        globals.set(
            "showMessage",
            luaFunction { args ->
                RuleMethods.showMessage(
                    type = args.arg(1).numberArgument("showMessage", 1),
                    title = args.arg(2).stringArgument("showMessage", 2),
                    text = args.arg(3).stringArgument("showMessage", 3)
                )
                LuaValue.NIL
            }
        )
        globals.set(
            "normalizePath",
            luaFunction { args ->
                LuaValue.valueOf(
                    MPPPath(args.arg(1).stringArgument("normalizePath", 1))
                        .normalize()
                        .toString()
                )
            }
        )
        globals.set(
            "createDirectories",
            luaFunction { args ->
                MPPPath(args.arg(1).stringArgument("createDirectories", 1))
                    .createDirectories()
                LuaValue.TRUE
            }
        )
        globals.set(
            "removeDirectory",
            luaFunction { args ->
                MPPPath(args.arg(1).stringArgument("removeDirectory", 1))
                    .removeDirectory()
                LuaValue.TRUE
            }
        )
    }

    /**
     * Implement the `isTrue`/`isFalse` family:
     * one argument yields a boolean, two to four arguments select between
     * the yes/no/fallback values (fallback applies to empty input).
     */
    // the argument positions mirror the documented helper signatures
    @Suppress("MagicNumber")
    private fun conditionalValue(args: Varargs, positive: Boolean): LuaValue {
        val value = args.arg(1)
        return when (args.narg()) {
            0 -> throw LuaError("isTrue/isFalse require at least one argument")
            1 -> LuaValue.valueOf(
                !isEmptyValue(value) && truthiness(value) == positive
            )
            else -> {
                val yes = args.arg(2)
                val no = args.arg(3).takeUnless { it.isnil() }
                    ?: LuaValue.valueOf("")
                val fallback = args.arg(4).takeUnless { it.isnil() }
                when {
                    isEmptyValue(value) -> fallback ?: no
                    truthiness(value) == positive -> yes
                    else -> no
                }
            }
        }
    }
}

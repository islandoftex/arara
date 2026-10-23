// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.islandoftex.arara.lua.rules.LuaBindings.sessionTable
import org.islandoftex.arara.lua.rules.LuaConversion.badArgument
import org.islandoftex.arara.lua.rules.LuaConversion.booleanArgument
import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.islandoftex.arara.lua.rules.LuaConversion.numberArgument
import org.islandoftex.arara.lua.rules.LuaConversion.pathArgument
import org.islandoftex.arara.lua.rules.LuaConversion.stringArgument
import org.islandoftex.arara.lua.rules.LuaConversion.stringListArgument
import org.islandoftex.arara.lua.rules.LuaConversion.toLua
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.common.CommonPlatform

/**
 * Binds shared helpers under camelCase and snake_case names.
 * Paths and commands cross the Lua boundary as opaque handles.
 */
internal object LuaRuleEnvironment {
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
        function is_list(value) return isList(value) end
        function is_map(value) return isMap(value) end
        function is_string(value) return isString(value) end
        function is_boolean(value) return isBoolean(value) end
    """

    fun newGlobals(): Globals {
        val globals = CommonPlatform.standardGlobals()
        LuaConditionalEnvironment.bindConditionalHelpers(globals)
        bindHelpers(globals)
        globals.load(PRELUDE, "arara-prelude").call()
        return globals
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
        "LongMethod", "CyclomaticComplexMethod",
        "SpreadOperator", "MagicNumber", "ThrowsCount"
    )
    private fun bindHelpers(globals: Globals) {
        bindSpelled(
            globals, "throwError"
        ) { name, args ->
            // the headered twin of the core helper: the rule header
            // the interpreter prefixes author errors with rides on
            // the type, exactly as in the MVEL dialect
            throw AraraExceptionWithHeader(
                args.arg(1).stringArgument(name, 1)
            )
        }
        bindSpelled(
            globals, "getCommand"
        ) { _, args ->
            LuaValue.userdataOf(
                RuleMethods.getCommand(*args.kotlinArgs().toTypedArray())
            )
        }
        bindSpelled(
            globals, "getCommandWithWorkingDirectory"
        ) { name, args ->
            LuaValue.userdataOf(
                RuleMethods.getCommandWithWorkingDirectory(
                    args.arg(1).pathArgument(name, 1),
                    *args.kotlinArgs(from = 2).toTypedArray()
                )
            )
        }
        bindSpelled(
            globals, "isTrue"
        ) { name, args ->
            conditionalValue(args, positive = true, name = name)
        }
        bindSpelled(
            globals, "isFalse"
        ) { name, args ->
            conditionalValue(args, positive = false, name = name)
        }
        bindSpelled(
            globals, "isEmpty"
        ) { _, args ->
            when (args.narg()) {
                1 -> LuaValue.valueOf(isEmptyValue(args.arg(1)))
                else -> if (isEmptyValue(args.arg(1)))
                    args.arg(2)
                else
                    args.arg(3).takeUnless { it.isnil() }
                        ?: LuaValue.valueOf("")
            }
        }
        bindSpelled(
            globals, "isNotEmpty"
        ) { _, args ->
            when (args.narg()) {
                1 -> LuaValue.valueOf(!isEmptyValue(args.arg(1)))
                else -> if (!isEmptyValue(args.arg(1)))
                    args.arg(2)
                else
                    args.arg(3).takeUnless { it.isnil() }
                        ?: LuaValue.valueOf("")
            }
        }
        bindSpelled(
            globals, "getBasename"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.getBasename(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
        bindSpelled(
            globals, "getFiletype"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.getFiletype(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
        bindSpelled(
            globals, "getOriginalFile"
        ) { _, _ ->
            LuaValue.valueOf(RuleMethods.originalFile)
        }
        bindSpelled(
            globals, "getOriginalReference"
        ) { _, _ ->
            LuaValue.valueOf(RuleMethods.originalReference.toString())
        }
        bindSpelled(
            globals, "halt"
        ) { name, args ->
            RuleMethods.halt(
                args.arg(1).takeUnless { it.isnil() }
                    ?.numberArgument(name, 1) ?: 0
            )
            LuaValue.NIL
        }
        // the session is exposed as a table of operations (the very same
        // shape as the directive conditional environment), as Lua cannot
        // call methods on the underlying object directly
        bindSpelled(
            globals, "getSession"
        ) { _, _ -> sessionTable() }
        bindSpelled(
            globals, "buildString"
        ) { _, args ->
            LuaValue.valueOf(
                RuleMethods.buildString(*args.kotlinArgs().toTypedArray())
            )
        }
        bindSpelled(
            globals, "trimSpaces"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.trimSpaces(
                    args.arg(1).stringArgument(name, 1)
                )
            )
        }
        bindSpelled(
            globals, "replicatePattern"
        ) { name, args ->
            toLua(
                RuleMethods.replicatePattern(
                    args.arg(1).stringArgument(name, 1),
                    args.arg(2).stringListArgument(name, 2)
                )
            )
        }
        bindSpelled(
            globals, "isVerboseMode"
        ) { _, _ ->
            LuaValue.valueOf(RuleMethods.isVerboseMode())
        }
        bindSpelled(
            globals, "isOnPath"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.isOnPath(
                    args.arg(1).stringArgument(name, 1)
                )
            )
        }
        listOf(
            "isWindows" to { RuleMethods.isWindows() },
            "isLinux" to { RuleMethods.isLinux() },
            "isMac" to { RuleMethods.isMac() },
            "isUnix" to { RuleMethods.isUnix() },
            "isCygwin" to { RuleMethods.isCygwin() }
        ).forEach { (name, check) ->
            bindSpelled(globals, name) { _, args ->
                when (args.narg()) {
                    0 -> LuaValue.valueOf(check())
                    else -> if (check()) args.arg(1) else args.arg(2)
                }
            }
        }
        bindSpelled(
            globals, "unsafelyExecuteSystemCommand"
        ) { name, args ->
            val command = LuaConversion.toCommand(args.arg(1))
                ?: throw badArgument(
                    name, 1,
                    "command", args.arg(1)
                )
            val (code, output) = RuleMethods
                .unsafelyExecuteSystemCommand(command)
            LuaValue.tableOf().also {
                it.set("first", LuaValue.valueOf(code))
                it.set("second", LuaValue.valueOf(output))
            }
        }
        bindSpelled(
            globals, "listFilesByExtensions"
        ) { name, args ->
            toLua(
                RuleMethods.listFilesByExtensions(
                    args.arg(1).pathArgument(name, 1),
                    args.arg(2).stringListArgument(name, 2),
                    args.arg(3).booleanArgument(name, 3)
                ).map { it.toString() }
            )
        }
        bindSpelled(
            globals, "listFilesByPatterns"
        ) { name, args ->
            toLua(
                RuleMethods.listFilesByPatterns(
                    args.arg(1).pathArgument(name, 1),
                    args.arg(2).stringListArgument(name, 2),
                    args.arg(3).booleanArgument(name, 3)
                ).map { it.toString() }
            )
        }
        bindSpelled(
            globals, "writeToFile"
        ) { name, args ->
            val path = args.arg(1).pathArgument(name, 1)
            // nil content is not a value a rule can write, and neither
            // is a map-shaped table: the typed boundary rejects both
            // instead of inventing the text a plain coercion would
            // produce
            val content = fromLua(args.arg(2))
                ?: throw badArgument(name, 2, "string or list", args.arg(2))
            if (content is Map<*, *>) {
                throw badArgument(name, 2, "string or list", args.arg(2))
            }
            val append = args.arg(3).booleanArgument(name, 3)
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
        bindSpelled(
            globals, "readFromFile"
        ) { name, args ->
            toLua(
                RuleMethods.readFromFile(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
        bindSpelled(
            globals, "isSubdirectory"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.isSubdirectory(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
        bindSpelled(
            globals, "showMessage"
        ) { name, args ->
            RuleMethods.showMessage(
                type = args.arg(1).numberArgument(name, 1),
                title = args.arg(2).stringArgument(name, 2),
                text = args.arg(3).stringArgument(name, 3)
            )
            LuaValue.NIL
        }
        bindSpelled(
            globals, "normalizePath"
        ) { name, args ->
            LuaValue.valueOf(
                args.arg(1).pathArgument(name, 1)
                    .normalize()
                    .toString()
            )
        }
        bindSpelled(
            globals, "createDirectories"
        ) { name, args ->
            args.arg(1).pathArgument(name, 1)
                .createDirectories()
            LuaValue.valueOf(true)
        }
        bindSpelled(
            globals, "removeDirectory"
        ) { name, args ->
            args.arg(1).pathArgument(name, 1)
                .removeDirectory()
            LuaValue.valueOf(true)
        }
    }

    // the argument positions mirror the documented helper signatures
    @Suppress("MagicNumber")
    private fun conditionalValue(
        args: Varargs,
        positive: Boolean,
        name: String
    ): LuaValue {
        val value = args.arg(1)
        return when (args.narg()) {
            0 -> throw LuaError(LanguageController.messages.errorLuaHelperNoArgs(name))
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

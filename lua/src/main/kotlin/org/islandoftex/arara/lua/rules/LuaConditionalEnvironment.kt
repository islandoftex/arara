// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.Globals
import io.github.taoguan.luaj.LuaNumber
import io.github.taoguan.luaj.LuaString
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.Varargs
import io.github.taoguan.luaj.lib.VarArgFunction
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.GUIDialogs
import org.islandoftex.arara.lua.rules.LuaConversion.badArgument
import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.islandoftex.arara.lua.rules.LuaConversion.numberArgument
import org.islandoftex.arara.lua.rules.LuaConversion.stringArgument
import org.islandoftex.arara.lua.rules.LuaConversion.toLua

/**
 * Set up the global environment Lua directive conditionals are evaluated
 * in (see [LuaConditionalEvaluator]).
 *
 * The environment exposes the very helpers the MVEL dialect offers for
 * conditionals (both dialects bind the very helpers from the core
 * module), so both dialects behave identically. Only helpers registered
 * in the MVEL conditional registry are bound: the directive conditional
 * world deliberately offers fewer tools than the rule world.
 *
 * Where the MVEL helper set offers overloads for plain extensions and
 * file references, the Lua bindings accept either a string (interpreted
 * as the extension, mirroring the MVEL string overload) or a file
 * reference value obtained from `toFile` or `currentFile`.
 *
 * The helper boundary is typed: arguments must match the declared type
 * and mismatches raise Lua-style bad argument errors. Lua's implicit
 * coercions apply to operators, not to arara's library functions —
 * convert explicitly with `tostring` or `tonumber` when needed.
 */
internal object LuaConditionalEnvironment {
    /**
     * Create a fresh global environment for one conditional evaluation.
     */
    fun newGlobals(): Globals {
        val globals = JsePlatform.standardGlobals()
        bindHelpers(globals)
        return globals
    }

    /**
     * The default width the MVEL dialog helpers fall back to, taken from
     * the same source of truth.
     */
    private val defaultDialogWidth: Int by lazy {
        GUIDialogs(Session.userInterfaceOptions).defaultWidth
    }

    private fun luaFunction(body: (Varargs) -> LuaValue): LuaValue =
        object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs = body(args)
        }

    private fun Varargs.values(from: Int): List<Any?> =
        (from..narg()).map { fromLua(arg(it)) }

    /**
     * Extract the argument at [index] as a file reference: a file object
     * (from `toFile` or `currentFile`) or a real Lua string (the plain
     * extension overload). Lua numbers do not silently become strings
     * here — the helpers type-check their arguments as Lua's own
     * library functions do; convert explicitly with `tostring` when
     * needed.
     */
    private fun Varargs.fileArgument(index: Int, helper: String): Any {
        val value = arg(index)
        return when {
            value.isuserdata() -> value.touserdata() as? MPPPath
                ?: throw badArgument(helper, index, "file or string", value)
            value is LuaString -> value.tojstring()
            else -> throw badArgument(helper, index, "file or string", value)
        }
    }

    // the bindings forward to Java vararg helpers (hence the spread
    // operators), inspect argument positions (hence the offsets) and
    // report binding errors where they occur
    @Suppress(
        "LongMethod", "ComplexMethod", "ThrowsCount",
        "SpreadOperator", "MagicNumber"
    )
    private fun bindHelpers(globals: Globals) {
        // file existence and content checks; both the extension-based
        // and the file-based MVEL overloads are available
        listOf("exists", "missing", "changed", "unchanged").forEach { name ->
            globals.set(
                name,
                luaFunction { args ->
                    val reference = args.fileArgument(1, name)
                    val result = when (reference) {
                        is String -> when (name) {
                            "exists" -> ConditionalMethods.exists(reference)
                            "missing" -> ConditionalMethods.missing(reference)
                            "changed" -> ConditionalMethods.changed(reference)
                            else -> ConditionalMethods.unchanged(reference)
                        }
                        is MPPPath -> when (name) {
                            "exists" -> ConditionalMethods.exists(reference)
                            "missing" -> ConditionalMethods.missing(reference)
                            "changed" -> ConditionalMethods.changed(reference)
                            else -> ConditionalMethods.unchanged(reference)
                        }
                        else -> throw badArgument(
                            name, 1, "file or string", args.arg(1)
                        )
                    }
                    LuaValue.valueOf(result)
                }
            )
        }

        globals.set(
            "found",
            luaFunction { args ->
                val reference = args.fileArgument(1, "found")
                val needle = args.arg(2).stringArgument("found", 2)
                val result = when (reference) {
                    is String -> ConditionalMethods.found(reference, needle)
                    is MPPPath -> ConditionalMethods.found(reference, needle)
                    else -> throw badArgument(
                        "found", 1, "file or string", args.arg(1)
                    )
                }
                LuaValue.valueOf(result)
            }
        )

        globals.set(
            "toFile",
            luaFunction { args ->
                LuaValue.userdataOf(
                    ConditionalMethods.toFile(
                        args.arg(1).stringArgument("toFile", 1)
                    )
                )
            }
        )
        globals.set(
            "currentFile",
            luaFunction { _ ->
                LuaValue.userdataOf(ConditionalMethods.currentFile())
            }
        )
        globals.set(
            "getOriginalReference",
            luaFunction { _ ->
                LuaValue.userdataOf(RuleMethods.originalReference)
            }
        )
        globals.set(
            "getBasename",
            luaFunction { args ->
                val reference = args.fileArgument(1, "getBasename")
                when (reference) {
                    is String -> RuleMethods.getBasename(reference)
                    is MPPPath -> RuleMethods.getBasename(reference)
                    else -> throw badArgument(
                        "getBasename", 1, "file or string", args.arg(1)
                    )
                }.let { LuaValue.valueOf(it) }
            }
        )

        // the session is exposed as a table of operations, as Lua cannot
        // call methods on the underlying object directly
        globals.set(
            "getSession",
            luaFunction { _ ->
                val session = RuleMethods.getSession()
                LuaValue.tableOf().also { table ->
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
        )

        // interactive dialogs; the width argument is optional, mirroring
        // the MVEL defaults (a leading number followed by another number
        // denotes an explicit width)
        bindDialog(
            globals, "showOptions"
        ) { width, type, title, text, values ->
            LuaValue.valueOf(
                ConditionalMethods.showOptions(
                    width, type, title, text, *values
                )
            )
        }
        bindDialog(
            globals, "showInput"
        ) { width, type, title, text, _ ->
            LuaValue.valueOf(
                ConditionalMethods.showInput(width, type, title, text)
            )
        }
        bindDialog(
            globals, "showDropdown"
        ) { width, type, title, text, values ->
            LuaValue.valueOf(
                ConditionalMethods.showDropdown(
                    width, type, title, text, *values
                )
            )
        }

        // classloading helpers return a table with the status name and
        // the loaded class or object
        bindClassLoading(
            globals, "loadClass",
            fileBody = { reference, className ->
                ConditionalMethods.loadClass(reference, className)
            },
            stringBody = { reference, className ->
                ConditionalMethods.loadClass(reference, className)
            }
        )
        bindClassLoading(
            globals, "loadObject",
            fileBody = { reference, className ->
                ConditionalMethods.loadObject(reference, className)
            },
            stringBody = { reference, className ->
                ConditionalMethods.loadObject(reference, className)
            }
        )
    }

    // the dialog arguments follow a positional protocol (an optional
    // width, the message type, title, text and the extra values)
    @Suppress("MagicNumber")
    private fun bindDialog(
        globals: Globals,
        name: String,
        body: (
            width: Int, type: Int, title: String,
            text: String, values: Array<Any>
        ) -> LuaValue
    ) {
        globals.set(
            name,
            luaFunction { args ->
                // an explicit width is present when the first argument
                // is a real number and the second one is a number as
                // well (the mandatory message type); otherwise the first
                // argument is the type and the default width applies.
                // Instance checks, not isnumber() — in this LuaJ fork
                // the latter also accepts numeric strings.
                val hasWidth = args.arg(1) is LuaNumber &&
                    args.arg(2) is LuaNumber
                val offset = if (hasWidth) 2 else 1
                val width = if (hasWidth)
                    args.arg(1).numberArgument(name, 1)
                else
                    defaultDialogWidth
                val type = args.arg(offset).numberArgument(name, offset)
                val title = args.arg(offset + 1)
                    .stringArgument(name, offset + 1)
                val text = args.arg(offset + 2)
                    .stringArgument(name, offset + 2)
                val values = args.values(offset + 3)
                    .filterNotNull()
                    .toTypedArray()
                body(width, type, title, text, values)
            }
        )
    }

    private fun bindClassLoading(
        globals: Globals,
        name: String,
        fileBody: (reference: MPPPath, className: String) ->
            Pair<ClassLoadingStatus, Any>,
        stringBody: (reference: String, className: String) ->
            Pair<ClassLoadingStatus, Any>
    ) {
        globals.set(
            name,
            luaFunction { args ->
                val className = args.arg(2).stringArgument(name, 2)
                val reference = args.fileArgument(1, name)
                val (status, value) = when (reference) {
                    is MPPPath -> fileBody(reference, className)
                    is String -> stringBody(reference, className)
                    else -> throw badArgument(
                        name, 1, "file or string", args.arg(1)
                    )
                }
                LuaValue.tableOf().also { table ->
                    table.set("status", LuaValue.valueOf(status.name))
                    table.set("value", LuaValue.userdataOf(value))
                }
            }
        )
    }
}

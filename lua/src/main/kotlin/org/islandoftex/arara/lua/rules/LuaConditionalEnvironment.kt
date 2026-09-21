// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.Globals
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.Varargs
import io.github.taoguan.luaj.lib.VarArgFunction
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.GUIDialogs
import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.islandoftex.arara.lua.rules.LuaConversion.toLua
import org.islandoftex.arara.mvel.utils.ConditionalMethods
import org.islandoftex.arara.mvel.utils.RuleMethods

/**
 * Set up the global environment Lua directive conditionals are evaluated
 * in (see [LuaConditionalEvaluator]).
 *
 * The environment exposes the very helpers the MVEL dialect offers for
 * conditionals (they live in the MVEL module and will move to a neutral
 * home once the MVEL format is removed), so both dialects behave
 * identically. Only helpers registered in the MVEL conditional registry
 * are bound: the directive conditional world deliberately offers fewer
 * tools than the rule world.
 *
 * Where the MVEL helper set offers overloads for plain extensions and
 * file references, the Lua bindings accept either a string (interpreted
 * as the extension, mirroring the MVEL string overload) or a file
 * reference value obtained from `toFile` or `currentFile`.
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
     * Extract the argument at [index] as a file reference; strings are
     * treated as plain paths.
     */
    private fun Varargs.fileArgument(index: Int): Any =
        arg(index).takeIf { it.isuserdata() }?.touserdata()
            ?: arg(index).takeIf { it.isstring() }?.tojstring()
            ?: throw AraraException(
                "The provided value is neither a file nor a path."
            )

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
                    val result = when (val reference = args.fileArgument(1)) {
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
                        else -> throw AraraException(
                            "The '$name' helper requires a file or an " +
                                "extension."
                        )
                    }
                    LuaValue.valueOf(result)
                }
            )
        }

        globals.set(
            "found",
            luaFunction { args ->
                val result = when (val reference = args.fileArgument(1)) {
                    is String -> ConditionalMethods.found(
                        reference, args.arg(2).tojstring()
                    )
                    is MPPPath -> ConditionalMethods.found(
                        reference, args.arg(2).tojstring()
                    )
                    else -> throw AraraException(
                        "The 'found' helper requires a file or an " +
                            "extension."
                    )
                }
                LuaValue.valueOf(result)
            }
        )

        globals.set(
            "toFile",
            luaFunction { args ->
                LuaValue.userdataOf(
                    ConditionalMethods.toFile(args.arg(1).tojstring())
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
                when (val reference = args.fileArgument(1)) {
                    is String -> RuleMethods.getBasename(reference)
                    is MPPPath -> RuleMethods.getBasename(reference)
                    else -> throw AraraException(
                        "The 'getBasename' helper requires a file."
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
                            toLua(session.get(args.arg(1).tojstring()))
                        }
                    )
                    table.set(
                        "put",
                        luaFunction { args ->
                            session.put(
                                args.arg(1).tojstring(),
                                fromLua(args.arg(2))
                                    ?: throw AraraException(
                                        "The session value cannot be null."
                                    )
                            )
                            LuaValue.NIL
                        }
                    )
                    table.set(
                        "contains",
                        luaFunction { args ->
                            LuaValue.valueOf(
                                session.contains(args.arg(1).tojstring())
                            )
                        }
                    )
                    table.set(
                        "remove",
                        luaFunction { args ->
                            session.remove(args.arg(1).tojstring())
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
            globals, "loadClass"
        ) { reference, className ->
            when (reference) {
                is MPPPath -> ConditionalMethods.loadClass(reference, className)
                is String -> ConditionalMethods.loadClass(reference, className)
                else -> throw AraraException(
                    "The 'loadClass' helper requires a file."
                )
            }
        }
        bindClassLoading(
            globals, "loadObject"
        ) { reference, className ->
            when (reference) {
                is MPPPath -> ConditionalMethods.loadObject(reference, className)
                is String -> ConditionalMethods.loadObject(reference, className)
                else -> throw AraraException(
                    "The 'loadObject' helper requires a file."
                )
            }
        }
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
                // an explicit width is present when the first argument is
                // a number and the second one is a number as well (the
                // mandatory message type); otherwise the first argument
                // is the type and the default width applies
                val hasWidth = args.arg(1).isnumber() && args.arg(2).isnumber()
                val offset = if (hasWidth) 2 else 1
                val width = if (hasWidth) args.arg(1).toint() else defaultDialogWidth
                val type = args.arg(offset).toint()
                val title = args.arg(offset + 1).tojstring()
                val text = args.arg(offset + 2).tojstring()
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
        body: (reference: Any, className: String) ->
            Pair<ClassLoadingStatus, Any>
    ) {
        globals.set(
            name,
            luaFunction { args ->
                val (status, value) = body(
                    args.fileArgument(1), args.arg(2).tojstring()
                )
                LuaValue.tableOf().also { table ->
                    table.set("status", LuaValue.valueOf(status.name))
                    table.set("value", LuaValue.userdataOf(value))
                }
            }
        )
    }
}

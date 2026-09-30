// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.Globals
import io.github.taoguan.luaj.LuaNumber
import io.github.taoguan.luaj.LuaString
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.Varargs
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.lua.rules.LuaConversion.badArgument
import org.islandoftex.arara.lua.rules.LuaConversion.bindSpelled
import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.islandoftex.arara.lua.rules.LuaConversion.numberArgument
import org.islandoftex.arara.lua.rules.LuaConversion.sessionTable
import org.islandoftex.arara.lua.rules.LuaConversion.stringArgument

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
 *
 * Every camelCase helper is additionally bound under its snake_case
 * alias, the Lua-idiomatic spelling; each spelling reports argument
 * errors under the name the conditional actually called, and the
 * historic spelling stays available throughout the lifetime of the
 * deprecated engine.
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
            bindSpelled(globals, name) { bound, args ->
                val reference = args.fileArgument(1, bound)
                val result = when (reference) {
                    is String -> when (bound) {
                        "exists" -> ConditionalMethods.exists(reference)
                        "missing" -> ConditionalMethods.missing(reference)
                        "changed" -> ConditionalMethods.changed(reference)
                        else -> ConditionalMethods.unchanged(reference)
                    }
                    is MPPPath -> when (bound) {
                        "exists" -> ConditionalMethods.exists(reference)
                        "missing" -> ConditionalMethods.missing(reference)
                        "changed" -> ConditionalMethods.changed(reference)
                        else -> ConditionalMethods.unchanged(reference)
                    }
                    else -> throw badArgument(
                        bound, 1, "file or string", args.arg(1)
                    )
                }
                LuaValue.valueOf(result)
            }
        }

        bindSpelled(
            globals, "found"
        ) { name, args ->
            val reference = args.fileArgument(1, name)
            val needle = args.arg(2).stringArgument(name, 2)
            val result = when (reference) {
                is String -> ConditionalMethods.found(reference, needle)
                is MPPPath -> ConditionalMethods.found(reference, needle)
                else -> throw badArgument(
                    name, 1, "file or string", args.arg(1)
                )
            }
            LuaValue.valueOf(result)
        }

        bindSpelled(
            globals, "toFile"
        ) { name, args ->
            LuaValue.userdataOf(
                ConditionalMethods.toFile(
                    args.arg(1).stringArgument(name, 1)
                )
            )
        }
        bindSpelled(
            globals, "currentFile"
        ) { _, _ ->
            LuaValue.userdataOf(ConditionalMethods.currentFile())
        }
        bindSpelled(
            globals, "getOriginalReference"
        ) { _, _ ->
            LuaValue.userdataOf(RuleMethods.originalReference)
        }
        bindSpelled(
            globals, "getBasename"
        ) { name, args ->
            val reference = args.fileArgument(1, name)
            when (reference) {
                is String -> RuleMethods.getBasename(reference)
                is MPPPath -> RuleMethods.getBasename(reference)
                else -> throw badArgument(
                    name, 1, "file or string", args.arg(1)
                )
            }.let { LuaValue.valueOf(it) }
        }

        // the session is exposed as a table of operations, as Lua cannot
        // call methods on the underlying object directly
        bindSpelled(
            globals, "getSession"
        ) { _, _ -> sessionTable() }

        // interactive dialogs
        bindDialog(
            globals, "showOptions"
        ) { type, title, text, values ->
            LuaValue.valueOf(
                ConditionalMethods.showOptions(
                    type, title, text, *values
                )
            )
        }
        bindDialog(
            globals, "showInput"
        ) { type, title, text, _ ->
            LuaValue.valueOf(
                ConditionalMethods.showInput(type, title, text)
            )
        }
        bindDialog(
            globals, "showDropdown"
        ) { type, title, text, values ->
            LuaValue.valueOf(
                ConditionalMethods.showDropdown(
                    type, title, text, *values
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

    // the dialog arguments follow a positional protocol (the message
    // type, title, text and the extra values); the width parameter of
    // the Swing era is gone, and arara owns the terminal geometry
    @Suppress("MagicNumber")
    private fun bindDialog(
        globals: Globals,
        name: String,
        body: (
            type: Int, title: String,
            text: String, values: Array<Any>
        ) -> LuaValue
    ) {
        bindSpelled(globals, name) { bound, args ->
            val type = args.arg(1).numberArgument(bound, 1)
            val title = args.arg(2).stringArgument(bound, 2)
            val text = args.arg(3).stringArgument(bound, 3)
            val values = args.values(4)
                .filterNotNull()
                .toTypedArray()
            body(type, title, text, values)
        }
    }

    private fun bindClassLoading(
        globals: Globals,
        name: String,
        fileBody: (reference: MPPPath, className: String) ->
            Pair<ClassLoadingStatus, Any>,
        stringBody: (reference: String, className: String) ->
            Pair<ClassLoadingStatus, Any>
    ) {
        bindSpelled(globals, name) { bound, args ->
            val className = args.arg(2).stringArgument(bound, 2)
            val reference = args.fileArgument(1, bound)
            val (status, value) = when (reference) {
                is MPPPath -> fileBody(reference, className)
                is String -> stringBody(reference, className)
                else -> throw badArgument(
                    bound, 1, "file or string", args.arg(1)
                )
            }
            LuaValue.tableOf().also { table ->
                table.set("status", LuaValue.valueOf(status.name))
                table.set("value", LuaValue.userdataOf(value))
            }
        }
    }
}

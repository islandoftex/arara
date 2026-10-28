// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

internal object LuaConditionalLoading {
    fun bind(globals: Globals) {
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
        )    }

    private fun bindClassLoading(
        globals: Globals,
        name: String,
        fileBody: (reference: MPPPath, className: String) ->
        Pair<ClassLoadingStatus, Any>,
        stringBody: (reference: String, className: String) ->
        Pair<ClassLoadingStatus, Any>
    ) {
        bindSpelled(globals, name) { bound, args ->
            val className = args.arg(LuaArgumentIndex.SECOND).stringArgument(bound, LuaArgumentIndex.SECOND)
            val reference = args.fileArgument(1, bound)
            val (status, value) = reference.fold(
                { fileBody(it, className) }, { stringBody(it, className) }
            )
            LuaValue.tableOf().also { table ->
                table.set("status", LuaValue.valueOf(status.name))
                table.set("value", LuaValue.userdataOf(value))
            }
        }
    }}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaArguments.badArgument
import org.islandoftex.arara.lua.rules.LuaArguments.booleanArgument
import org.islandoftex.arara.lua.rules.LuaArguments.pathArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringListArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.islandoftex.arara.lua.rules.LuaConversion.fromLua
import org.islandoftex.arara.lua.rules.LuaConversion.toLua
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/** Register file helpers without changing their script names. */
internal object LuaFileBindings {
    fun bind(globals: Globals) {
        bindListFilesByExtensions(globals)
        bindListFilesByPatterns(globals)
        bindWriteToFile(globals)
        bindReadFromFile(globals)
        bindIsSubdirectory(globals)
        bindNormalizePath(globals)
        bindCreateDirectories(globals)
        bindRemoveDirectory(globals)
    }

    private fun bindListFilesByExtensions(globals: Globals) {
        bindSpelled(
            globals, "listFilesByExtensions"
        ) { name, args ->
            toLua(
                RuleMethods.listFilesByExtensions(
                    args.arg(1).pathArgument(name, 1),
                    args.arg(LuaArgumentIndex.SECOND).stringListArgument(name, LuaArgumentIndex.SECOND),
                    args.arg(LuaArgumentIndex.THIRD).booleanArgument(name, LuaArgumentIndex.THIRD)
                ).map { it.toString() }
            )
        }
    }

    private fun bindListFilesByPatterns(globals: Globals) {
        bindSpelled(
            globals, "listFilesByPatterns"
        ) { name, args ->
            toLua(
                RuleMethods.listFilesByPatterns(
                    args.arg(1).pathArgument(name, 1),
                    args.arg(LuaArgumentIndex.SECOND).stringListArgument(name, LuaArgumentIndex.SECOND),
                    args.arg(LuaArgumentIndex.THIRD).booleanArgument(name, LuaArgumentIndex.THIRD)
                ).map { it.toString() }
            )
        }
    }

    private fun bindWriteToFile(globals: Globals) {
        bindSpelled(
            globals, "writeToFile"
        ) { name, args ->
            val path = args.arg(1).pathArgument(name, 1)
            // nil content is not a value a rule can write, and neither
            // is a map-shaped table: the typed boundary rejects both
            // instead of inventing the text a plain coercion would
            // produce
            val content = fromLua(args.arg(LuaArgumentIndex.SECOND))
                ?: throw badArgument(name, LuaArgumentIndex.SECOND, "string or list", args.arg(LuaArgumentIndex.SECOND))
            if (content is Map<*, *>) {
                throw badArgument(name, LuaArgumentIndex.SECOND, "string or list", args.arg(LuaArgumentIndex.SECOND))
            }
            val append = args.arg(LuaArgumentIndex.THIRD).booleanArgument(name, LuaArgumentIndex.THIRD)
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
    }

    private fun bindReadFromFile(globals: Globals) {
        bindSpelled(
            globals, "readFromFile"
        ) { name, args ->
            toLua(
                RuleMethods.readFromFile(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
    }

    private fun bindIsSubdirectory(globals: Globals) {
        bindSpelled(
            globals, "isSubdirectory"
        ) { name, args ->
            LuaValue.valueOf(
                RuleMethods.isSubdirectory(
                    args.arg(1).pathArgument(name, 1)
                )
            )
        }
    }

    private fun bindNormalizePath(globals: Globals) {
        bindSpelled(
            globals, "normalizePath"
        ) { name, args ->
            LuaValue.valueOf(
                args.arg(1).pathArgument(name, 1)
                    .normalize()
                    .toString()
            )
        }
    }

    private fun bindCreateDirectories(globals: Globals) {
        bindSpelled(
            globals, "createDirectories"
        ) { name, args ->
            args.arg(1).pathArgument(name, 1)
                .createDirectories()
            LuaValue.valueOf(true)
        }
    }

    private fun bindRemoveDirectory(globals: Globals) {
        bindSpelled(
            globals, "removeDirectory"
        ) { name, args ->
            args.arg(1).pathArgument(name, 1)
                .removeDirectory()
            LuaValue.valueOf(true)
        }
    }
}

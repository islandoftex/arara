// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.lua.rules.LuaConversion.badArgument
import org.islandoftex.arara.lua.rules.LuaConversion.booleanArgument
import org.islandoftex.arara.lua.rules.LuaConversion.luaFunction
import org.islandoftex.arara.lua.rules.LuaConversion.stringArgument
import org.islandoftex.arara.lua.rules.LuaConversion.toLua
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

/** Explicit path operations without reflective access to the underlying runtime. */
internal class LuaPath(private val path: MPPPath) : LuaUserdata(path) {
    // Each branch is one member of the limited Lua path API; numbers are argument positions.
    @Suppress("CyclomaticComplexMethod", "LongMethod", "MagicNumber")
    override fun get(key: LuaValue): LuaValue = when (key.tojstring()) {
        "file_name", "fileName" -> toLua(path.fileName)
        "file_size", "fileSize" -> toLua(path.fileSize)
        "parent" -> toLua(path.parent)
        "exists" -> toLua(path.exists)
        "is_absolute", "isAbsolute" -> toLua(path.isAbsolute)
        "is_directory", "isDirectory" -> toLua(path.isDirectory)
        "is_regular_file", "isRegularFile" -> toLua(path.isRegularFile)
        "normalize" -> method(key) { receiver, _, _ -> toLua(receiver.normalize()) }
        "resolve" -> method(key) { receiver, args, name ->
            val child = args.arg(2)
            toLua(if (child is LuaString) receiver.resolve(child.tojstring())
                else receiver.resolve(child.pathReceiver(name, 2)))
        }
        "resolve_sibling", "resolveSibling" -> method(key) { receiver, args, name ->
            val sibling = args.arg(2)
            toLua(if (sibling is LuaString) receiver.resolveSibling(sibling.tojstring())
                else receiver.resolveSibling(sibling.pathReceiver(name, 2)))
        }
        "starts_with", "startsWith" -> method(key) { receiver, args, name ->
            toLua(receiver.startsWith(args.arg(2).pathReceiver(name, 2)))
        }
        "read_text", "readText" -> method(key) { receiver, _, _ -> toLua(receiver.readText()) }
        "read_lines", "readLines" -> method(key) { receiver, _, _ -> toLua(receiver.readLines()) }
        "write_text", "writeText" -> method(key) { receiver, args, name ->
            receiver.writeText(args.arg(2).stringArgument(name, 2), args.optionalBoolean(name, 3))
            LuaValue.NIL
        }
        "create_directories", "createDirectories" -> method(key) { receiver, _, _ ->
            receiver.createDirectories()
            LuaValue.NIL
        }
        "list_entries", "listEntries" -> method(key) { receiver, _, _ -> toLua(receiver.listEntries()) }
        "list_files", "listFiles" -> method(key) { receiver, args, name ->
            toLua(receiver.listFiles(args.optionalBoolean(name, 2)))
        }
        else -> LuaValue.NIL
    }

    private fun method(key: LuaValue, body: (MPPPath, Varargs, String) -> LuaValue): LuaValue =
        luaFunction { args ->
            val name = "path.${key.tojstring()}"
            body(args.arg(1).pathReceiver(name, 1), args, name)
        }

    private fun LuaValue.pathReceiver(name: String, index: Int): MPPPath =
        (takeIf { it.isuserdata() }?.touserdata() as? MPPPath)
            ?: throw badArgument(name, index, "path", this)

    private fun Varargs.optionalBoolean(name: String, index: Int): Boolean =
        if (arg(index).isnil()) false else arg(index).booleanArgument(name, index)
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.lua.rules.LuaArguments.badArgument
import org.islandoftex.arara.lua.rules.LuaArguments.booleanArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaConversion.toLua
import org.islandoftex.arara.lua.rules.LuaValues.luaFunction
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

/** Explicit path operations without reflective access to the underlying runtime. */
internal class LuaPath(private val path: MPPPath) : LuaUserdata(path) {
    override fun get(key: LuaValue): LuaValue {
        val name = key.tojstring()
        return properties[name]?.invoke(path)?.let(::toLua)
            ?: operations[name]?.let { method(key, it) }
            ?: LuaValue.NIL
    }

    private val properties: Map<String, (MPPPath) -> Any> = aliases(mapOf(
        "fileName" to { it.fileName },
        "fileSize" to { it.fileSize },
        "parent" to { it.parent },
        "exists" to { it.exists },
        "isAbsolute" to { it.isAbsolute },
        "isDirectory" to { it.isDirectory },
        "isRegularFile" to { it.isRegularFile }
    ))

    private val operations = aliases(algebra() + reading() + writing())

    private fun algebra(): Map<String, PathOperation> = mapOf(
        "normalize" to { receiver, _, _ -> toLua(receiver.normalize()) },
        "resolve" to { receiver, args, name ->
            val child = args.arg(LuaArgumentIndex.SECOND)
            toLua(if (child is LuaString) receiver.resolve(child.tojstring())
            else receiver.resolve(child.pathReceiver(name, LuaArgumentIndex.SECOND)))
        },
        "resolveSibling" to { receiver, args, name ->
            val sibling = args.arg(LuaArgumentIndex.SECOND)
            toLua(if (sibling is LuaString) receiver.resolveSibling(sibling.tojstring())
            else receiver.resolveSibling(sibling.pathReceiver(name, LuaArgumentIndex.SECOND)))
        },
        "startsWith" to { receiver, args, name ->
            toLua(receiver.startsWith(args.arg(LuaArgumentIndex.SECOND).pathReceiver(name, LuaArgumentIndex.SECOND)))
        }
    )

    private fun reading(): Map<String, PathOperation> = mapOf(
        "readText" to { receiver, _, _ -> toLua(receiver.readText()) },
        "readLines" to { receiver, _, _ -> toLua(receiver.readLines()) },
        "listEntries" to { receiver, _, _ -> toLua(receiver.listEntries()) },
        "listFiles" to { receiver, args, name ->
            toLua(receiver.listFiles(args.optionalBoolean(name, LuaArgumentIndex.SECOND)))
        }
    )

    private fun writing(): Map<String, PathOperation> = mapOf(
        "writeText" to { receiver, args, name ->
            receiver.writeText(args.arg(LuaArgumentIndex.SECOND).stringArgument(name, LuaArgumentIndex.SECOND),
                args.optionalBoolean(name, LuaArgumentIndex.THIRD))
            LuaValue.NIL
        },
        "createDirectories" to { receiver, _, _ ->
            receiver.createDirectories()
            LuaValue.NIL
        }
    )

    private fun <T> aliases(values: Map<String, T>): Map<String, T> = values.flatMap { (name, value) ->
        listOf(name to value, name.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase() to value)
    }.toMap()

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

private typealias PathOperation = (MPPPath, Varargs, String) -> LuaValue

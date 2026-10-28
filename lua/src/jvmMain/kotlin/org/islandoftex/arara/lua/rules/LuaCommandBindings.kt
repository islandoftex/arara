// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaArguments.badArgument
import org.islandoftex.arara.lua.rules.LuaArguments.pathArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

/** Register command helpers without changing their script names. */
internal object LuaCommandBindings {
    fun bind(globals: Globals) {
        bindThrowError(globals)
        bindGetCommand(globals)
        bindNamedCommand(globals)
        bindGetCommandWithWorkingDirectory(globals)
        bindUnsafelyExecuteSystemCommand(globals)
    }

    private fun bindThrowError(globals: Globals) {
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
    }

    private fun bindGetCommand(globals: Globals) {
        bindSpelled(
            globals, "getCommand"
        ) { _, args ->
            LuaValue.userdataOf(
                org.islandoftex.arara.core.rules.CommandRuleHelpers.commandFromValues(args.kotlinArgs())
            )
        }
    }

    private fun bindNamedCommand(globals: Globals) {
        bindSpelled(globals, "namedCommand") { name, args ->
            val label = args.arg(1).stringArgument(name, 1)
            val command = LuaValues.toCommand(args.arg(LuaArgumentIndex.SECOND))
                ?: throw badArgument(name, LuaArgumentIndex.SECOND, "command", args.arg(LuaArgumentIndex.SECOND))
            LuaValue.userdataOf(
                org.islandoftex.arara.core.session.Command(command.elements, command.workingDirectory, label)
            )
        }
    }

    private fun bindGetCommandWithWorkingDirectory(globals: Globals) {
        bindSpelled(
            globals, "getCommandWithWorkingDirectory"
        ) { name, args ->
            LuaValue.userdataOf(
                org.islandoftex.arara.core.rules.CommandRuleHelpers.commandFromValues(
                    args.arg(1).pathArgument(name, 1),
                    args.kotlinArgs(from = LuaArgumentIndex.SECOND)
                )
            )
        }
    }

    private fun bindUnsafelyExecuteSystemCommand(globals: Globals) {
        bindSpelled(
            globals, "unsafelyExecuteSystemCommand"
        ) { name, args ->
            val command = LuaValues.toCommand(args.arg(1))
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
    }
}

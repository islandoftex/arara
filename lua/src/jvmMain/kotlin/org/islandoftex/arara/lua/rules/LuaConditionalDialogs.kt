// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.lua.rules.LuaArguments.numberArgument
import org.islandoftex.arara.lua.rules.LuaArguments.stringArgument
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue

internal object LuaConditionalDialogs {
    fun bind(globals: Globals) {
        // interactive dialogs
        bindDialog(
            globals, "showOptions"
        ) { type, title, text, values ->
            LuaValue.valueOf(
                org.islandoftex.arara.core.rules.ConditionalDialogHelpers.showOptions(
                    type, title, text, values
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
            globals, "showList"
        ) { type, title, text, values ->
            LuaValue.valueOf(
                org.islandoftex.arara.core.rules.ConditionalDialogHelpers.showList(
                    type, title, text, values
                )
            )
        }

    }

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
            val title = args.arg(LuaArgumentIndex.SECOND).stringArgument(bound, LuaArgumentIndex.SECOND)
            val text = args.arg(LuaArgumentIndex.THIRD).stringArgument(bound, LuaArgumentIndex.THIRD)
            val values = args.kotlinArgs(LuaArgumentIndex.FOURTH)
                .filterNotNull()
                .toTypedArray()
            body(type, title, text, values)
        }
    }

}

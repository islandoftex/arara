// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.lua.rules.LuaBindings.bindSpelled
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs

/** Register value helpers without changing their script names. */
internal object LuaValueBindings {
    fun bind(globals: Globals) {
        bindIsTrue(globals)
        bindIsFalse(globals)
        bindIsEmpty(globals)
        bindIsNotEmpty(globals)
    }

    private fun bindIsTrue(globals: Globals) {
        bindSpelled(
            globals, "isTrue"
        ) { name, args ->
            conditionalValue(args, positive = true, name = name)
        }
    }

    private fun bindIsFalse(globals: Globals) {
        bindSpelled(
            globals, "isFalse"
        ) { name, args ->
            conditionalValue(args, positive = false, name = name)
        }
    }

    private fun bindIsEmpty(globals: Globals) {
        bindSpelled(
            globals, "isEmpty"
        ) { _, args ->
            when (args.narg()) {
                1 -> LuaValue.valueOf(isEmptyValue(args.arg(1)))
                else -> if (isEmptyValue(args.arg(1)))
                    args.arg(LuaArgumentIndex.SECOND)
                else
                    args.arg(LuaArgumentIndex.THIRD).takeUnless { it.isnil() }
                        ?: LuaValue.valueOf("")
            }
        }
    }

    private fun bindIsNotEmpty(globals: Globals) {
        bindSpelled(
            globals, "isNotEmpty"
        ) { _, args ->
            when (args.narg()) {
                1 -> LuaValue.valueOf(!isEmptyValue(args.arg(1)))
                else -> if (!isEmptyValue(args.arg(1)))
                    args.arg(LuaArgumentIndex.SECOND)
                else
                    args.arg(LuaArgumentIndex.THIRD).takeUnless { it.isnil() }
                        ?: LuaValue.valueOf("")
            }
        }
    }

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
                val yes = args.arg(LuaArgumentIndex.SECOND)
                val no = args.arg(LuaArgumentIndex.THIRD).takeUnless { it.isnil() }
                    ?: LuaValue.valueOf("")
                val fallback = args.arg(LuaArgumentIndex.FOURTH).takeUnless { it.isnil() }
                when {
                    isEmptyValue(value) -> fallback ?: no
                    truthiness(value) == positive -> yes
                    else -> no
                }
            }
        }
    }
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.luaj.vm2.Globals
import org.luaj.vm2.lib.common.CommonPlatform

/**
 * Binds shared helpers under camelCase and snake_case names.
 * Paths expose selected operations; commands remain opaque handles.
 */
internal object LuaRuleEnvironment {
    private const val PRELUDE = """
        function contains(list, value)
            for _, v in ipairs(list) do
                if v == value then return true end
            end
            return false
        end
        function keys(map)
            local result = {}
            for k in pairs(map) do result[#result + 1] = tostring(k) end
            table.sort(result)
            return result
        end
        function join(list, separator)
            local strings = {}
            for i, v in ipairs(list) do strings[i] = tostring(v) end
            return table.concat(strings, separator or " ")
        end
        function isList(value) return type(value) == "table" end
        function isMap(value) return type(value) == "table" end
        function isString(value) return type(value) == "string" end
        function isBoolean(value) return type(value) == "boolean" end
        function is_list(value) return isList(value) end
        function is_map(value) return isMap(value) end
        function is_string(value) return isString(value) end
        function is_boolean(value) return isBoolean(value) end
    """

    fun newGlobals(): Globals {
        val globals = CommonPlatform.standardGlobals()
        LuaConditionalEnvironment.bindConditionalHelpers(globals)
        LuaCommandBindings.bind(globals)
        LuaValueBindings.bind(globals)
        LuaReferenceBindings.bind(globals)
        LuaStringBindings.bind(globals)
        LuaPlatformBindings.bind(globals)
        LuaFileBindings.bind(globals)
        LuaMessageBindings.bind(globals)
        globals.load(PRELUDE, "arara-prelude").call()
        return globals
    }

}

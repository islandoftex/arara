// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for lua messages. */
interface LuaMessages {
    /** Wrapper for Lua option interpolation failures. */
    val errorLuaInterpolationFailure: String

    /** MVEL interpolation syntax was used with the Lua engine. */
    val errorYamlTagsInLua: String

    /** Lua interpolation syntax was used with the YAML engine. */
    val errorLuaTagsInYaml: String

    /** Missing closing delimiter in a Lua interpolation tag. */
    val errorLuaUnterminatedTag: String

    /** Missing closing quote in a Lua interpolation expression. */
    val errorLuaUnterminatedQuote: String

    /** Missing closing delimiter in a Lua long string. */
    val errorLuaUnterminatedLongString: String

    /** Distinct Kotlin map keys produce the same Lua string key. */
    val errorLuaMapKeys: String

    /** Distinct Lua table keys produce the same Kotlin string key. */
    val errorLuaTableKeys: String

    /** Lossy Long-to-Lua conversion; argument: integer value. */
    fun errorLuaExactInteger(value: String): String

    /** A cyclic collection crossed the Kotlin/Lua value boundary. */
    val errorLuaCyclicValues: String

    /** Helper argument error; arguments: position, helper name, expected type and actual type. */
    fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String

    /** Helper result error; arguments: helper name and actual result type. */
    fun errorLuaBooleanResult(helper: String, actual: String): String

    /** Missing helper arguments; argument: helper name. */
    fun errorLuaHelperNoArgs(helper: String): String
}

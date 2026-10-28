// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for lua messages. */
public interface LuaMessages {
    /** Wrapper for Lua option interpolation failures. */
    public val errorLuaInterpolationFailure: String

    /** MVEL interpolation syntax was used with the Lua engine. */
    public val errorYamlTagsInLua: String

    /** Lua interpolation syntax was used with the YAML engine. */
    public val errorLuaTagsInYaml: String

    /** Missing closing delimiter in a Lua interpolation tag. */
    public val errorLuaUnterminatedTag: String

    /** Missing closing quote in a Lua interpolation expression. */
    public val errorLuaUnterminatedQuote: String

    /** Missing closing delimiter in a Lua long string. */
    public val errorLuaUnterminatedLongString: String

    /** Distinct Kotlin map keys produce the same Lua string key. */
    public val errorLuaMapKeys: String

    /** Distinct Lua table keys produce the same Kotlin string key. */
    public val errorLuaTableKeys: String

    /** Lossy Long-to-Lua conversion; argument: integer value. */
    public fun errorLuaExactInteger(value: String): String

    /** A cyclic collection crossed the Kotlin/Lua value boundary. */
    public val errorLuaCyclicValues: String

    /** Helper argument error; arguments: position, helper name, expected type and actual type. */
    public fun errorLuaBadArgument(index: Int, helper: String, expected: String, actual: String): String

    /** Helper result error; arguments: helper name and actual result type. */
    public fun errorLuaBooleanResult(helper: String, actual: String): String

    /** Missing helper arguments; argument: helper name. */
    public fun errorLuaHelperNoArgs(helper: String): String
}

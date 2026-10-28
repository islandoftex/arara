// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for script type messages. */
public interface ScriptTypeMessages {
    /** Script field type error; argument: field path. */
    public fun errorScriptString(field: String): String

    /** Script table type error; argument: field path. */
    public fun errorScriptTable(field: String): String

    /** Script boolean type error; argument: field path. */
    public fun errorScriptBoolean(field: String): String

    /** Script integer type error; argument: field path. */
    public fun errorScriptInteger(field: String): String

    /** Non-contiguous script list; argument: field path. */
    public fun errorScriptList(field: String): String

    /** Script function type error; argument: field path. */
    public fun errorScriptFunction(field: String): String

    /** Non-literal script value; argument: field path. */
    public fun errorScriptLiteral(field: String): String
}

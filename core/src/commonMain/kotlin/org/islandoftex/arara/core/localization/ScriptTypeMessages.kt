// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for script type messages. */
interface ScriptTypeMessages {
    /** Script field type error; argument: field path. */
    fun errorScriptString(field: String): String

    /** Script table type error; argument: field path. */
    fun errorScriptTable(field: String): String

    /** Script boolean type error; argument: field path. */
    fun errorScriptBoolean(field: String): String

    /** Script integer type error; argument: field path. */
    fun errorScriptInteger(field: String): String

    /** Non-contiguous script list; argument: field path. */
    fun errorScriptList(field: String): String

    /** Script function type error; argument: field path. */
    fun errorScriptFunction(field: String): String

    /** Non-literal script value; argument: field path. */
    fun errorScriptLiteral(field: String): String
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for script structure messages. */
interface ScriptStructureMessages {
    /** Unsupported value for the field; expected: allowed values. */
    fun errorScriptChoice(field: String, choices: String): String

    /** Unknown script key; argument: key path. */
    fun errorScriptUnknownKey(field: String): String

    /** Missing script value; argument: field path. */
    fun errorScriptRequired(field: String): String

    /** Blank script string; argument: field path. */
    fun errorScriptNonblank(field: String): String

    /** Duplicate script entry; arguments: field path and repeated value. */
    fun errorScriptDuplicate(field: String, value: String): String

    /** A directory was supplied as a file; argument: field path. */
    fun errorScriptDirectory(field: String): String

    /** Empty required collection; argument: field path. */
    fun errorScriptEmpty(field: String): String

    /** Invalid script mapping keys; argument: field path. */
    fun errorScriptStringKeys(field: String): String

    /** Missing alternative fields; arguments: parent path and permitted alternatives. */
    fun errorScriptAlternatives(field: String, alternatives: String): String
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for script structure messages. */
public interface ScriptStructureMessages {
    /** Unsupported value for the field; expected: allowed values. */
    public fun errorScriptChoice(field: String, choices: String): String

    /** Unknown script key; argument: key path. */
    public fun errorScriptUnknownKey(field: String): String

    /** Missing script value; argument: field path. */
    public fun errorScriptRequired(field: String): String

    /** Blank script string; argument: field path. */
    public fun errorScriptNonblank(field: String): String

    /** Duplicate script entry; arguments: field path and repeated value. */
    public fun errorScriptDuplicate(field: String, value: String): String

    /** A directory was supplied as a file; argument: field path. */
    public fun errorScriptDirectory(field: String): String

    /** Empty required collection; argument: field path. */
    public fun errorScriptEmpty(field: String): String

    /** Invalid script mapping keys; argument: field path. */
    public fun errorScriptStringKeys(field: String): String

    /** Missing alternative fields; arguments: parent path and permitted alternatives. */
    public fun errorScriptAlternatives(field: String, alternatives: String): String
}

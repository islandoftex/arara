// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for file lookup messages. */
interface FileLookupMessages {
    /** Malformed glob pattern; arguments: pattern and parser details. */
    fun errorGlobPatternInvalid(pattern: String, details: String): String

    /**
     * The error shown when the basename helper is handed a directory
     * instead of a file.
     */
    fun errorBasenameNotAFile(path: String): String

    /**
     * The error shown when a referenced file cannot be found.
     */
    fun errorDiscoverFileFileNotFound(file: String, extensions: String): String

    /** A missing reference cannot be completed in safe mode; argument: file. */
    fun errorDiscoverFileSafeModeNoCompletion(file: String): String

    /** An existing file has no configured type; arguments: file and known extensions. */
    fun errorDiscoverFileUnknownExtension(file: String, extensions: String): String

    /** An ordinary Lua file has no configured type; also explains the project suffix. */
    fun errorDiscoverFileUnknownLuaExtension(file: String, extensions: String): String

    /** Invalid configured directive pattern; argument: regular expression. */
    fun errorFiletypeInvalidPattern(pattern: String): String

    /**
     * The error shown when the filetype helper is handed a directory
     * instead of a file.
     */
    fun errorFiletypeNotAFile(path: String): String

    /**
     * The error shown when an unknown extension is to be resolved
     * without an explicit file type.
     */
    fun errorFiletypeUnknownExtension(extension: String, extensions: String): String
}

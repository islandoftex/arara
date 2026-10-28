// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for file data messages. */
interface FileDataMessages {
    /**
     * The error shown when the file database cannot be loaded.
     */
    fun errorLoadCouldNotLoadDatabase(name: String): String

    /**
     * The error shown when the file database cannot be saved.
     */
    fun errorSaveCouldNotSaveDatabase(name: String): String

    /**
     * The error shown when the hash of a file cannot be computed due to
     * an IO problem.
     */
    val errorCalculateHashIoException: String

    /**
     * The error shown when a string cannot be interpreted as a boolean
     * value.
     */
    fun errorCheckBooleanNotValidBoolean(value: String): String

    /**
     * The error shown when a file cannot be read during a regex check.
     */
    fun errorCheckRegexIoException(file: String): String

    /** Legacy cache loading diagnostic; argument: database path. */
    fun errorLoadCouldNotLoadXml(name: String): String

    /** Legacy cache writing diagnostic; argument: database path. */
    fun errorSaveCouldNotSaveXml(name: String): String
}

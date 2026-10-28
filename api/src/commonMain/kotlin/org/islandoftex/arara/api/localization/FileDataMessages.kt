// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for file data messages. */
public interface FileDataMessages {
    /**
     * The error shown when the file database cannot be loaded.
     */
    public fun errorLoadCouldNotLoadDatabase(name: String): String

    /**
     * The error shown when the file database cannot be saved.
     */
    public fun errorSaveCouldNotSaveDatabase(name: String): String

    /**
     * The error shown when the hash of a file cannot be computed due to
     * an IO problem.
     */
    public val errorCalculateHashIoException: String

    /**
     * The error shown when a string cannot be interpreted as a boolean
     * value.
     */
    public fun errorCheckBooleanNotValidBoolean(value: String): String

    /**
     * The error shown when a file cannot be read during a regex check.
     */
    public fun errorCheckRegexIoException(file: String): String

    /** Legacy cache loading diagnostic; argument: database path. */
    public fun errorLoadCouldNotLoadXml(name: String): String

    /** Legacy cache writing diagnostic; argument: database path. */
    public fun errorSaveCouldNotSaveXml(name: String): String
}

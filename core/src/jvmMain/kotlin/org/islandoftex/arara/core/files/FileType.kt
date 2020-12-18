// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.util.regex.PatternSyntaxException
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType

data class FileType(
    override val extension: String = FileType.INVALID_EXTENSION,
    override val pattern: String = FileType.INVALID_PATTERN
) : FileType {
    init {
        try {
            pattern.toPattern()
        } catch (e: PatternSyntaxException) {
            throw AraraException(
                    "The pattern you wanted to choose for this file type is invalid.",
                    e
            )
        }
    }

    /**
     * Provides a textual representation of the current file type object.
     * @return A string containing a textual representation of the current file
     * type object.
     */
    override fun toString(): String {
        return ".$extension"
    }

    /**
     * Implements the file type equals method, checking if one file type is
     * equal to another. Note that only the file extension is considered.
     * @param other The object to be analyzed.
     * @return A boolean value indicating if those two objects are equal.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileType
        if (extension != other.extension) return false
        return true
    }

    /**
     * Implements the file type hash code. Note that only the file extension is
     * considered.
     * @return An integer representing the file type hash code.
     */
    override fun hashCode(): Int {
        return extension.hashCode()
    }
}

/**
 * This value identifies an unknown file type.
 */
val FileType.Companion.UNKNOWN_TYPE: FileType
    get() = FileType(INVALID_EXTENSION, "")

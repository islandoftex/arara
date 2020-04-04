// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.model

import java.util.regex.PatternSyntaxException
import kotlinx.serialization.Serializable
import org.islandoftex.arara.AraraException
import org.islandoftex.arara.configuration.ConfigurationUtils
import org.islandoftex.arara.files.FileType
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.utils.CommonUtils

/**
 * Implements the file type model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class FileTypeImpl : FileType {
    // string representing the
    // file extension
    override var extension: String = FileType.INVALID_EXTENSION
        private set

    // string representing the
    // file pattern to be used
    // as directive lookup
    override var pattern: String = FileType.INVALID_PATTERN
        @Throws(AraraException::class)
        get() {
            if (field == FileType.INVALID_PATTERN) {
                field = ConfigurationUtils.defaultFileTypePatterns[extension]
                        ?: throw AraraException(
                                LanguageController.getMessage(
                                        Messages.ERROR_FILETYPE_UNKNOWN_EXTENSION,
                                        extension,
                                        CommonUtils.fileTypesList
                                )
                        )
            }
            return field
        }
        private set

    constructor(extension: String, pattern: String) {
        this.extension = extension
        this.pattern = pattern

        try {
            pattern.toPattern()
        } catch (e: PatternSyntaxException) {
            if (!ConfigurationUtils.defaultFileTypePatterns.containsKey(extension))
                throw AraraException(
                        LanguageController.getMessage(
                                Messages.ERROR_FILETYPE_UNKNOWN_EXTENSION,
                                extension,
                                CommonUtils.fileTypesList
                        )
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
    get() = FileTypeImpl(FileType.INVALID_EXTENSION, "")

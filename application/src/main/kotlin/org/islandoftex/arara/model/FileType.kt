// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.model

import java.util.regex.PatternSyntaxException
import kotlinx.serialization.Serializable
import org.islandoftex.arara.configuration.ConfigurationUtils
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
class FileType {
    // string representing the
    // file extension
    var extension: String = INVALID_EXTENSION
        get() = CommonUtils.removeKeywordNotNull(field)
        private set
    // string representing the
    // file pattern to be used
    // as directive lookup
    var pattern: String = INVALID_PATTERN
        @Throws(AraraException::class)
        get() {
            CommonUtils.removeKeywordNotNull(field)
            if (field == INVALID_PATTERN) {
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

    companion object {
        /**
         * This constant identifies an invalid extension. As unices do not
         * allow a forward and Windows does not allow a backward slash, this
         * should suffice.
         */
        const val INVALID_EXTENSION = "/\\"
        /**
         * This constant identifies an invalid pattern. This is an opening
         * character class which is invalid.
         */
        const val INVALID_PATTERN = ""
        /**
         * This value identifies an unknown file type.
         */
        val UNKNOWN_TYPE = FileType(INVALID_EXTENSION, "")
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

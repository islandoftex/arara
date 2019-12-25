/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.model

import com.github.cereda.arara.configuration.ConfigurationUtils
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.utils.CommonUtils
import kotlinx.serialization.Serializable
import java.util.regex.PatternSyntaxException

/**
 * Implements the file type model.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
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
         * This constant identifies an invalid pattern. This is a opening
         * character class which is invalid.
         */
        const val INVALID_PATTERN = "["
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

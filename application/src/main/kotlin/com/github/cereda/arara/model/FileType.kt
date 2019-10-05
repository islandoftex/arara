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

import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.utils.CommonUtils

/**
 * Implements the file type model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class FileType {
    // string representing the
    // file extension
    var extension: String? = null
        private set

    // string representing the
    // file pattern to be used
    // as directive lookup
    var pattern: String? = null
        private set

    /**
     * Constructor. It takes both file extension and pattern lookup.
     * @param extension The file extension.
     * @param pattern The file pattern.
     */
    constructor(extension: String, pattern: String) {
        this.extension = extension
        this.pattern = pattern
    }

    /**
     * Constructor. It takes the extension, but it might raise an exception if
     * the extension is unknown. This constructor is used when you just want
     * to reorganize the file lookup priority without the need of changing the
     * default extensions.
     * @param extension The file extension.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    constructor(extension: String) {
        if (types.containsKey(extension)) {
            this.extension = extension
            this.pattern = types[extension]
        } else {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_FILETYPE_UNKNOWN_EXTENSION,
                            extension,
                            CommonUtils.fileTypesList
                    )
            )
        }
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
     * Provides a textual representation of the current file type object.
     * @return A string containing a textual representation of the current file
     * type object.
     */
    override fun toString(): String {
        return ".$extension"
    }

    /**
     * Implements the file type hash code. Note that only the file extension is
     * considered.
     * @return An integer representing the file type hash code.
     */
    override fun hashCode(): Int {
        return extension?.hashCode() ?: 0
    }

    companion object {
        // the application messages obtained from the
        // language controller
        private val messages = LanguageController

        // a map containing all file
        // types that arara accepts
        // initialized by setting the default file types and
        // their corresponding patterns.
        private val types = mapOf(
                "tex" to "^\\s*%\\s+",
                "dtx" to "^\\s*%\\s+",
                "ltx" to "^\\s*%\\s+",
                "drv" to "^\\s*%\\s+",
                "ins" to "^\\s*%\\s+"
        )
    }

}

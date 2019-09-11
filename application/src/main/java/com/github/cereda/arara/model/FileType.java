/**
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
package com.github.cereda.arara.model;

import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.utils.CommonUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implements the file type model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class FileType {

    // string representing the
    // file extension
    private String extension;
    
    // string representing the
    // file pattern to be used
    // as directive lookup
    private String pattern;

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    // a map containing all file
    // types that arara accepts
    private static final Map<String, String> types =
            new HashMap<String, String>();

    /**
     * Initializes the file type class by setting the default file types and
     * their corresponding patterns.
     */
    public static void init() {
        types.put("tex", "^\\s*%\\s+");
        types.put("dtx", "^\\s*%\\s+");
        types.put("ltx", "^\\s*%\\s+");
        types.put("drv", "^\\s*%\\s+");
        types.put("ins", "^\\s*%\\s+");
    }

    /**
     * Constructor. It takes both file extension and pattern lookup.
     * @param extension The file extension.
     * @param pattern The file pattern.
     */
    public FileType(String extension, String pattern) {
        this.extension = extension;
        this.pattern = pattern;
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
    public FileType(String extension) throws AraraException {
        if (types.containsKey(extension)) {
            this.extension = extension;
            this.pattern = types.get(extension);
        } else {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_FILETYPE_UNKNOWN_EXTENSION,
                            extension,
                            CommonUtils.getFileTypesList()
                    )
            );
        }
    }

    /**
     * Implements the file type hash code. Note that only the file extension is
     * considered.
     * @return An integer representing the file type hash code.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(extension).toHashCode();
    }

    /**
     * Implements the file type equals method, checking if one file type is
     * equal to another. Note that only the file extension is considered.
     * @param object The object to be analyzed.
     * @return A boolean value indicating if those two objects are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final FileType reference = (FileType) object;
        return new EqualsBuilder().append(extension, reference.extension).isEquals();
    }

    /**
     * Gets the file type extension.
     * @return String representing the file type extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the file type pattern.
     * @return String representing the file type pattern.
     */
    public String getPattern() {
        return pattern;
    }
    
    /**
     * Provides a textual representation of the current file type object.
     * @return A string containing a textual representation of the current file
     * type object.
     */
    @Override
    public String toString() {
        return ".".concat(extension);
    }

}

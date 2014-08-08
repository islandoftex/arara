/**
 * \cond LICENSE
 * Arara -- the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda
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
 * \endcond
 * 
 * AraraFilePattern: This class maps the supported filetypes.
 */
// package definition
package com.github.arara.model;

// needed imports
import com.github.arara.utils.AraraConstants;

/**
 * Maps the supported filetypes.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class AraraFilePattern {

    // file extension
    private String extension;
    // file pattern
    private String pattern;

    /**
     * Getter for the file extension.
     *
     * @return The file extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Setter for the file extension.
     *
     * @param extension The file extension.
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Getter for the file pattern.
     *
     * @return The file pattern.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Setter for the file pattern.
     *
     * @param pattern The file pattern.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern.concat(AraraConstants.DIRECTIVEPREFIX);
    }
}

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

import com.github.cereda.arara.utils.CommonUtils;

/**
 * The rule argument model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Argument {

    // the argument identifier
    private String identifier;
    
    // a boolean indicating if the
    // current argument is required
    private boolean required;
    
    // the flag to hold the argument
    // value manipulation
    private String flag;
    
    // the argument fallback if it is
    // not defined in the directive
    private String fallback;

    /**
     * Gets the identifier.
     * @return The identifier.
     */
    public String getIdentifier() {
        return CommonUtils.removeKeyword(identifier);
    }

    /**
     * Sets the identifier.
     * @param identifier The identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Checks if the argument is required.
     * @return A boolean value indicating if the argument is required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets the argument requirement.
     * @param required A boolean value.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Gets the argument flag.
     * @return The flag.
     */
    public String getFlag() {
        return CommonUtils.removeKeyword(flag);
    }

    /**
     * Sets the argument flag.
     * @param flag The argument flag.
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }

    /**
     * Gets the argument fallback.
     * @return The argument fallback.
     */
    public String getDefault() {
        return CommonUtils.removeKeyword(fallback);
    }

    /**
     * Sets the argument fallback.
     * @param fallback The argument fallback.
     */
    public void setDefault(String fallback) {
        this.fallback = fallback;
    }

}

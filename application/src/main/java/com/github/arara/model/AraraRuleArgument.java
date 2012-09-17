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
 * PlainAraraRuleArgument: This class provides the model for representing a
 * plain Arara rule argument based on the YAML files. It's a plain old Java
 * object.
 */
// package definition
package com.github.arara.model;

// needed import
import com.github.arara.utils.AraraUtils;

/**
 * Provides the model for representing a plain Arara rule argument based on the
 * YAML files. This class will be used to map YAML rules into Java objects.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 1.0
 */
public class AraraRuleArgument {

    // the rule argument identifier
    private String identifier;
    // the command flag
    private String flag;
    // the default value
    private String defaultValue;

    /**
     * Getter for the default value.
     *
     * @return The default value.
     */
    public String getDefault() {
        return defaultValue;
    }

    /**
     * Setter for the default value.
     *
     * @param defaultValue The default value.
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = AraraUtils.removeKeyword(defaultValue);
    }

    /**
     * Getter for flag.
     *
     * @return The command flag.
     */
    public String getFlag() {

        // return it
        return flag;
    }

    /**
     * Setter for flag.
     *
     * @param flag The command flag.
     */
    public void setFlag(String flag) {

        // set the flag
        this.flag = AraraUtils.removeKeyword(flag);
    }

    /**
     * Getter for the argument identifier.
     *
     * @return The argument identifier.
     */
    public String getIdentifier() {

        // return it
        return identifier;
    }

    /**
     * Setter for the argument identifier.
     *
     * @param identifier The argument identifier.
     */
    public void setIdentifier(String identifier) {

        // set the identifier
        this.identifier = identifier;
    }
}

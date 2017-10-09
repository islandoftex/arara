/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2017, Paulo Roberto Massa Cereda 
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

import java.util.List;
import java.util.Map;

/**
 * Implements the directive model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Directive {

    // the directive identifier, it is resolved
    // to the rule identifier later on
    private String identifier;
    
    // a map containing the parameters; they
    // are validated later on in order to
    // ensure they are valid
    private Map<String, Object> parameters;
    
    // a conditional containing the type and
    // the expression to be evaluated later on
    private Conditional conditional;
    
    // a list contained all line numbers from
    // the main file which built the current
    // directive
    private List<Integer> lineNumbers;

    /**
     * Gets the directive identifier.
     * @return A string representing the directive identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the directive identifier.
     * @param identifier A string representing the directive identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the directive parameters.
     * @return A map containing the directive parameters.
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Sets the directive parameters.
     * @param parameters A map containing the directive parameters.
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the conditional object from the current directive.
     * @return The conditional object from the current directive.
     */
    public Conditional getConditional() {
        return conditional;
    }

    /**
     * Sets the conditional object from the current directive.
     * @param conditional The conditional object from the current directive.
     */
    public void setConditional(Conditional conditional) {
        this.conditional = conditional;
    }

    /**
     * Gets the list containing all line numbers from the current directive.
     * @return A list containing all line numbers from the current directive.
     */
    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    /**
     * Sets the list containing all line numbers from the current directive.
     * @param lineNumbers A list containing all line numbers from the current
     * directive.
     */
    public void setLineNumbers(List<Integer> lineNumbers) {
        this.lineNumbers = lineNumbers;
    }

    /**
     * Provides a textual representation of the current directive.
     * @return A string containing a textual representation of the current
     * directive.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Directive: { ");
        builder.append("identifier: ").append(identifier).append(", ");
        builder.append("parameters: ").append(parameters).append(", ");
        builder.append("conditional: ").append(conditional).append(", ");
        builder.append("lines: ").append(lineNumbers).append(" }");
        return builder.toString();
    }

}

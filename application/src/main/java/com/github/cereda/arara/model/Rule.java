/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2018, Paulo Roberto Massa Cereda 
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

/**
 * Implements the rule model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Rule {

    // the rule identifier
    private String identifier;
    
    // the rule name
    private String name;
    
    // the list of authors
    private List<String> authors;
    
    // the list of commands
    private List<RuleCommand> commands;
    
    // the list of arguments
    private List<Argument> arguments;

    /**
     * Gets the rule identifier.
     * @return The rule identifier.
     */
    public String getIdentifier() {
        return CommonUtils.removeKeyword(identifier);
    }

    /**
     * Sets the rule identifier.
     * @param identifier The rule identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the rule identifier.
     * @return The rule identifier.
     */
    public String getName() {
        return CommonUtils.removeKeyword(name);
    }

    /**
     * Sets the rule name.
     * @param name The rule name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of authors.
     * @return A list of authors.
     */
    public List<String> getAuthors() {
        if (authors != null) {
            Collection<String> result = CollectionUtils.collect(
                    authors, new Transformer<String, String>() {
                public String transform(String input) {
                    return CommonUtils.removeKeyword(input);
                }
            });
            authors = new ArrayList<String>(result);
        }
        return authors;
    }

    /**
     * Sets the list of authors.
     * @param authors The list of authors.
     */
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    /**
     * Gets the list of commands.
     * @return The list of commands.
     */
    public List<RuleCommand> getCommands() {
        return commands;
    }

    /**
     * Sets the list of commands.
     * @param commands The list of commands.
     */
    public void setCommands(List<RuleCommand> commands) {
        this.commands = commands;
    }

    /**
     * Gets the list of arguments.
     * @return The list of arguments.
     */
    public List<Argument> getArguments() {
        return arguments;
    }

    /**
     * Sets the list of arguments.
     * @param arguments The list of arguments.
     */
    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

}

/**
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
 *
 * PlainAraraRuleConfig.java: This class provides the model for representing
 * a plain Arara rule configuration based on the YAML files. It's a plain old
 * Java object.
 */

// package definition
package com.github.arara.model;

// needed imports
import java.util.List;

/**
 * Provides the model for representing a plain Arara rule configuration based
 * on the YAML files. This class will be used to map YAML rules into Java
 * objects.
 * @author Paulo Roberto Massa Cereda
 * @version 2.0
 * @since 1.0
 */
public class PlainAraraRuleConfig {

    // the rule identifier
    private String identifier;
    
    // the rule name
    private String name;
    
    // the rule command
    private String command;
    
    // the arguments list
    private List<PlainAraraRuleArgument> arguments;

    /**
     * Getter for the rule identifier.
     * @return The rule identifier.
     */
    public String getIdentifier() {
        
        // return it
        return identifier;
    }

    /**
     * Setter for the rule identifier.
     * @param identifier The rule identifier.
     */
    public void setIdentifier(String identifier) {
        
        // set the identifier
        this.identifier = identifier;
    }

    /**
     * Getter for the rule name.
     * @return The rule name.
     */
    public String getName() {
        
        // return it
        return name;
    }

    /**
     * Setter for the rule name.
     * @param name The rule name.
     */
    public void setName(String name) {
        
        // set the rule name
        this.name = name;
    }
    
    /**
     * Getter for the rule command.
     * @return The rule command.
     */
    public String getCommand() {
        
        // return it
        return command;
    }

    /**
     * Setter for the rule command.
     * @param command The rule command.
     */
    public void setCommand(String command) {
        
        // set the command
        this.command = command;
    }
    
    /**
     * Getter for the arguments list.
     * @return The arguments list.
     */
    public List<PlainAraraRuleArgument> getArguments() {
        
        // return the list
        return arguments;
    }

    /**
     * Setter for the arguments list.
     * @param arguments The arguments list.
     */
    public void setArguments(List<PlainAraraRuleArgument> arguments) {
        
        // set the list
        this.arguments = arguments;
    }
}

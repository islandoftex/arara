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
 * AraraCommand.java: This class provides the model for commands to be
 * executed by the runtime. It's a plain old Java object.
 */

// package definition
package com.github.arara.model;

/**
 * Provides the model for commands to be executed by the runtime.
 * @author Paulo Roberto Massa Cereda
 * @version 2.0
 * @since 1.0
 */
public class AraraCommand {

    // the name to be displayed in the command line
    private String name;
    
    // the command itself
    private String command;

    /**
     * Getter for command.
     * @return The command to be executed by the runtime.
     */
    public String getCommand() {
        
        // return it
        return command;
    }

    /**
     * Setter for command.
     * @param command The command, already expanded.
     */
    public void setCommand(String command) {
        
        // set the command
        this.command = command;
    }

    /**
     * Getter for the command name.
     * @return The command name, provided by the rule.
     */
    public String getName() {
        
        // return it
        return name;
    }

    /**
     * Setter for the command name.
     * @param name The command name.
     */
    public void setName(String name) {
        
        // set the name
        this.name = name;
    }
}

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

import com.github.cereda.arara.utils.CommonUtils;

/**
 * Implements the rule command model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class RuleCommand {

    // the command name
    private String name;
    
    // the command instruction
    private String command;
    
    // the exit status expression
    private String exit;

    /**
     * Gets the command name.
     * @return The command name.
     */
    public String getName() {
        return CommonUtils.removeKeyword(name);
    }

    /**
     * Sets the command name.
     * @param name The command name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the command instruction.
     * @return The command instruction.
     */
    public String getCommand() {
        return CommonUtils.removeKeyword(command);
    }

    /**
     * Sets the command instruction.
     * @param command The command instruction.
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets the exit status expression.
     * @return The exit status expression.
     */
    public String getExit() {
        return CommonUtils.removeKeyword(exit);
    }

    /**
     * Sets the exit status expression.
     * @param exit The exit status expression.
     */
    public void setExit(String exit) {
        this.exit = exit;
    }

}

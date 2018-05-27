/**
 * Rule converter, a tool for Arara
 * Copyright (c) 2018, Paulo Roberto Massa Cereda
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated documentation  files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, sublicense,  and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and this  permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT  SHALL THE AUTHORS OR COPYRIGHT HOLDERS  BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR  OTHER LIABILITY, WHETHER IN AN  ACTION OF CONTRACT,
 * TORT OR  OTHERWISE, ARISING  FROM, OUT  OF OR  IN CONNECTION  WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.cereda.arara.ruleconverter.model;

import java.util.List;

/**
 * New rule format.
 * 
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class NRule {

    // the rule identifier
    private String identifier;
    
    // the rule name
    private String name;
    
    // the list of commands
    private List<NCommand> commands;
    
    // the list of arguments
    private List<NArgument> arguments;

    /**
     * Constructor.
     */
    public NRule() {
    }
    
    /**
     * Getter.
     * @return The identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Setter.
     * @param identifier The identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Getter.
     * @return The name. 
     */
    public String getName() {
        return name;
    }

    /**
     * Setter.
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter.
     * @return The list of commands. 
     */
    public List<NCommand> getCommands() {
        return commands;
    }

    /**
     * Setter.
     * @param commands The list of commands.
     */
    public void setCommands(List<NCommand> commands) {
        this.commands = commands;
    }

    /**
     * Getter.
     * @return The list of arguments. 
     */
    public List<NArgument> getArguments() {
        return arguments;
    }

    /**
     * Setter.
     * @param arguments The list of arguments.
     */
    public void setArguments(List<NArgument> arguments) {
        this.arguments = arguments;
    }

}

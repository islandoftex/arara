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
 * AraraTask: This class provides the model for representing the Arara tasks
 * based on the directives and rules. It's a plain old Java object.
 */
// package definition
package com.github.arara.model;

// needed import
import java.util.HashMap;

/**
 * Provides the model for representing the Arara tasks based on the directives
 * and rules.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 1.0
 */
public class AraraTask {

    // the task name
    private String name;
    // a map to hold the tasks parameters
    private HashMap parameters;

    /**
     * Empty constructor.
     */
    public AraraTask() {
    }

    /**
     * Getter for name.
     *
     * @return The task name.
     */
    public String getName() {

        // return it
        return name;
    }

    /**
     * Setter for name.
     *
     * @param name The task name.
     */
    public void setName(String name) {

        // set the name
        this.name = name;
    }

    /**
     * Getter for parameters.
     *
     * @return A map containing all the task parameters.
     */
    public HashMap getParameters() {

        // return it
        return parameters;
    }

    /**
     * Setter for parameters.
     *
     * @param parameters The task parameters as a map.
     */
    public void setParameters(HashMap parameters) {

        // set parameters
        this.parameters = parameters;
    }
}

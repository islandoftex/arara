/**
 * Arara, the cool TeX automation tool
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
 */
package com.github.cereda.arara.controller;

import java.util.HashMap;
import java.util.Map;
import org.zeroturnaround.exec.ProcessExecutor;

/**
 * Implements a system call controller. This class wraps a map that holds the
 * result of system specific variables not directly available at runtime.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class SystemCallController {
    
    // the controller itself, since we have a singleton;
    // this is the reference instance, instantiated once
    private static final SystemCallController instance =
            new SystemCallController();
    
    // the system call map which holds the result of
    // system specific variables not directly available
    // at runtime; the idea here is to provide wrappers
    // to the map getter, so it could be easily manipulated
    private final Map<String, Object> map;
    
    /**
     * Private constructor. Called once when the singleton is created.
     */
    private SystemCallController() {
        
        // create the new map instance and perform
        // all system calls only once
        map = new HashMap<String, Object>();
        map.put("cygwin", isCygwin());
    }
    
    /**
     * Gets the singleton reference. Since this class is implemented as a
     * singleton, you will get the same controller every single time.
     * @return The system call controller which holds the system call map.
     */
    public static SystemCallController getInstance() {
        return instance;
    }
    
    /**
     * Checks if the result of the 'uname -s' system call indicates if this
     * application is being executed under a Cygwin environment.
     * @return A boolean value indicating if we are running under a Cygwin
     * environment.
     */
    private boolean isCygwin() {
        try {
            
            // execute a new system call to 'uname -s', read the output as
            // an UTF-8 string, lowercase it and check if it starts with the
            // 'cygwin' string; if so, we are inside Cygwin
            return (
                    new ProcessExecutor().command("uname", "-s").
                            readOutput(true).execute().outputUTF8()
                    ).toLowerCase().startsWith("cygwin");
            
        } catch (Exception exception) {
            
            // gracefully fallback in case of any nasty and evil
            // exception, e.g, if the command is unavailable
            return false;
        }
    }
    
    /**
     * Gets the object indexed by the provided key. This method actually holds
     * the map method of the very same name.
     * @param key The provided map key.
     * @return The object indexed by the provided map key.
     */
    public Object get(String key) {
        return map.get(key);
    }
    
}

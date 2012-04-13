/**
 * \cond LICENSE
 * ********************************************************************
 * This is a conditional block for preventing the DoxyGen documentation
 * tool to include this license header within the description of each
 * source code file. If you want to include this block, please define
 * the LICENSE parameter into the provided DoxyFile.
 * ********************************************************************
 *
 * Arara -- the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the project's author nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * ********************************************************************
 * End of the LICENSE conditional block
 * ********************************************************************
 * \endcond
 *
 * AraraDirective.java: This class provides the model for representing the
 * Arara directives found in the <code>tex</code> file. It's a plain old
 * Java object.
 */

// package definition
package com.github.arara.model;

// needed imports
import java.util.Map;

/**
 * Provides the model for representing the Arara directives found
 * in the <code>tex</code> file.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class AraraDirective {

    // the directive name
    private String name;
    
    // the configuration
    private Map config;
    
    // the line number
    private int lineNumber = 0;

    /**
     * Constructor.
     * @param name The directive name.
     * @param config The configuration.
     */
    public AraraDirective(String name, Map config) {
        
        // set the name
        this.name = name;
        
        // set the configuration
        this.config = config;
    }

    /**
     * Constructor.
     */
    public AraraDirective() {
    }

    /**
     * Getter for configuration.
     * @return The configuration.
     */
    public Map getConfig() {
        
        // return it
        return config;
    }

    /**
     * Setter for configuration.
     * @param config The configuration.
     */
    public void setConfig(Map config) {
        
        // set the configuration
        this.config = config;
    }

    /**
     * Getter for the directive name.
     * @return The directive name. 
     */
    public String getName() {
        
        // return it
        return name;
    }

    /**
     * Setter for the directive name.
     * @param name The directive name.
     */
    public void setName(String name) {
        
        // set it
        this.name = name;
    }

    /**
     * Getter for the line number.
     * @return The line number.
     */
    public int getLineNumber() {
        
        // return it
        return lineNumber;
    }

    /**
     * Setter for the line number.
     * @param lineNumber The line number.
     */
    public void setLineNumber(int lineNumber) {
        
        // set it
        this.lineNumber = lineNumber;
    }
}

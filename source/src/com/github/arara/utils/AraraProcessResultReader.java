/**
 * *********************************************************************
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
 * *********************************************************************
 *
 * AraraProcessResultReader.java: This class reads the input stream from
 * the runtime execution.
 */

// package definition
package com.github.arara.utils;

// needed imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads the input stream from the runtime execution.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0.1
 * @since 1.0
 */
public class AraraProcessResultReader extends Thread {
    
    // the input stream
    private final InputStream inputStream;
    
    // the input type, might be from standard error
    // or standard output
    private final String type;
    
    // the output
    private final StringBuilder output;

    /**
     * Constructor.
     * @param inputStream The input stream.
     * @param type The type of stream.
     */
    public AraraProcessResultReader(final InputStream inputStream, String type) {
        
        // set stream
        this.inputStream = inputStream;
        
        // set type
        this.type = type;
        
        // create a new string builder
        this.output = new StringBuilder();
    }

    /**
     * Runs the reader.
     */
    @Override
    public void run() {
        
        // lets try
        try {
            
            // create a new reader from the input stream
            final InputStreamReader streamReader = new InputStreamReader(inputStream);
            
            // create a new buffered reader
            final BufferedReader bufferedReader = new BufferedReader(streamReader);
            
            // the current line
            String line = null;
            
            // while there's still content in buffer
            while ((line = bufferedReader.readLine()) != null) {
                
                // append the line
                this.output.append(line).append("\n");
            }
            
        } catch (final IOException ioe) {
            
            // send a message
            throw new RuntimeException("ERROR\n\nArara was not able to read the input stream.");
        }
    }

    /**
     * Getter for type.
     * @return The input stream type.
     */
    public String getType() {
        
        // return it
        return type;
    }

    /**
     * Returns the captured buffer as string.
     * @return The captured buffer.
     */
    @Override
    public String toString() {
        
        // return the string builder as string
        return output.toString();
    }
}

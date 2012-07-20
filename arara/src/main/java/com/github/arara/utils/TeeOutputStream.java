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
 * TeeOutputStream.java: This class implements an output stream that handles
 * other streams, so the flow is propagated to all of them in an easy way.
 */

// package definition
package com.github.arara.utils;

// needed importa
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implements an output stream that handles other streams, so the flow
 * is propagated to all of them in an easy way.
 * @author Paulo Roberto Massa Cereda
 * @version 2.0
 * @since 2.0
 */
public class TeeOutputStream extends OutputStream
{
    
    // the list of the output streams
    private final OutputStream[] streams;
    
    /**
     * Constructor. 
     * @param ostream An array containing the output streams.
     */
    public TeeOutputStream(OutputStream... ostream) {
        
        // set the array
        streams = ostream;
    }
 
    /**
     * Writes the byte to all the output streams.
     * @param b The byte to write.
     * @throws IOException Exception if an error ocurred.
     */
    @Override
    public void write(int b) throws IOException {
        
        // for every stream
        for (OutputStream ostream : streams) {
            
            // write the byte
            ostream.write(b);
        }
    }
 
    /**
     * Writes an array of bytes to all the output streams.
     * @param b The bytes to write.
     * @param off The offset.
     * @param len Number of bytes.
     * @throws IOException Exception if an error ocurred.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        
        // for every stream
        for (OutputStream ostream : streams) {
            
            // write the array
            ostream.write(b, off, len);
        }
    }
 
    /**
     * Flushes the stream.
     * @throws IOException Exception if an error ocurred. 
     */
    @Override
    public void flush() throws IOException {
        
        // for every stream
        for (OutputStream ostream : streams) {
            
            // flush the stream
            ostream.flush();
        }
    }
 
    /**
     * Closes the stream.
     * @throws IOException Exception if an error ocurred.
     */
    @Override
    public void close() throws IOException {
        
        // for every stream
        for (OutputStream ostream : streams) {
            
            // try to close the stream
            forceClose(ostream);
        }
    }
    
    /**
     * Forces the stream to close without raising exceptions.
     * @param ostream The stream.
     */
    private void forceClose(OutputStream ostream) {
        
        // try to close
        try {
            
            // close it
            ostream.close();
            
        } catch (IOException ex) {
            // do nothing
        }
    }
    
}
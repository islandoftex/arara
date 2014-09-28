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
package com.github.cereda.arara.model;

import java.util.HashMap;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

/**
 * The XML database model, which keeps track on file changes. I am using the
 * Simple framework to marshall and unmarshall objects and XML files.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
@Root(name = "status")
public class Database {

    // the whole database is implemented as a map, where
    // the key is the absolute canonical file and the value
    // is its corresponding CRC32 hash; the XML map is done
    // inline, so it does not clutter the output a lot
    @ElementMap(entry = "hash", key = "file", attribute = true, inline = true)
    private HashMap<String, String> map;

    /**
     * Constructor. It creates a new map.
     */
    public Database() {
        map = new HashMap<String, String>();
    }

    /**
     * Gets the map.
     * @return The map.
     */
    public HashMap<String, String> getMap() {
        return map;
    }

    /**
     * Sets the map.
     * @param map The map.
     */
    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }

}

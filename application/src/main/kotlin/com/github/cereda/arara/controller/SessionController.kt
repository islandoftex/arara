/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
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
package com.github.cereda.arara.controller

import java.util.*

/**
 * Implements a session controller. This class wraps a map that holds the
 * execution session, that is, a dirty maneuver to exchange pretty much any
 * data between commands and even rules.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object SessionController {
    // the session map which holds the execution session;
    // the idea here is to provide wrappers to the map
    // methods, so it could be easily manipulated
    private val map = mutableMapOf<String, Any>()

    /**
     * Gets the object indexed by the provided key. This method actually holds
     * the map method of the very same name.
     * @param key The provided map key.
     * @return The object indexed by the provided map key.
     */
    operator fun get(key: String): Any {
        return map.getValue(key)
    }

    /**
     * Puts the object in the session map and indexes it under the provided
     * key. This method actually holds the map method of the very same name.
     * @param key The provided map key.
     * @param value The object to be indexed under the provided key.
     */
    fun put(key: String, value: Any) {
        map[key] = value
    }

    /**
     * Checks if the session map contains the provided key. This method holds
     * the map method of the very same name.
     * @param key The key to be checked.
     * @return A boolean value indicating if the session map contains the
     * provided key.
     */
    operator fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    /**
     * Remove an entry from the map according to the provided key. This method
     * holds the map method of the same name.
     * @param key The provided key to indicate which session map entry should
     * be removed.
     */
    fun remove(key: String) {
        map.remove(key)
    }

    /**
     * Clears the session map. This method, as usual, holds the map method of
     * the same name.
     */
    fun clear() {
        map.clear()
    }
}

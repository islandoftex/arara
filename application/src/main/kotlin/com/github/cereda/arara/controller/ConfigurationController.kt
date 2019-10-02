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

/**
 * Implements the configuration controller. The idea here is to provide a map
 * that holds all configuration settings used by model and utilitary classes
 * throughout the execution. This controller is implemented as a singleton.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object ConfigurationController {
    // the configuration settings are stored in a map;
    // pretty much everything can be stored in this map,
    // as long as you know what to retrieve later on
    private val map: MutableMap<String, Any> = mutableMapOf()

    /**
     * Returns the object indexed by the provided key. This method provides an
     * easy access to the underlying map.
     * @param key A string representing the key.
     * @return An object indexed by the provided key.
     */
    operator fun get(key: String): Any {
        return map.getValue(key)
    }

    /**
     * Puts the object in the map and indexes it in the provided key. This
     * method provides an easy access to the underlying map.
     * @param key A string representing the key.
     * @param value The object to be indexed by the provided key.
     */
    fun put(key: String, value: Any) {
        map[key] = value
    }

    /**
     * Checks if the map contains the provided key. This is actually a wrapper
     * to the private map's method of the same name.
     * @param key The key to be checked.
     * @return A boolean value indicating if the map contains the provided key.
     */
    operator fun contains(key: String): Boolean {
        return map.containsKey(key)
    }
}

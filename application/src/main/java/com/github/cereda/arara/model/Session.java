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
package com.github.cereda.arara.model;

import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.controller.SessionController;

/**
 * Implements the session model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Session {

    // get the current instance from the
    // session controller
    private static final SessionController session =
            SessionController.getInstance();

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Inserts the object into the session, indexed by the provided key.
     * @param key The provided key.
     * @param value The value to be inserted.
     */
    public void insert(String key, Object value) {
        session.put(key, value);
    }

    /**
     * Removes the entry indexed by the provided key from the session.
     * @param key The provided key.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public void remove(String key) throws AraraException {
        if (session.contains(key)) {
            session.remove(key);
        } else {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_SESSION_REMOVE_UNKNOWN_KEY,
                            key
                    )
            );
        }
    }

    /**
     * Checks if the provided key exists in the session.
     * @param key The provided key.
     * @return A boolean value indicating if the provided key exists in the
     * session.
     */
    public boolean exists(String key) {
        return session.contains(key);
    }

    /**
     * Clears the session.
     */
    public void forget() {
        session.clear();
    }

    /**
     * Gets the object indexed by the provided key from the session.
     * @param key The provided key.
     * @return The object indexed by the provided key.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public Object obtain(String key) throws AraraException {
        if (session.contains(key)) {
            return session.get(key);
        } else {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_SESSION_OBTAIN_UNKNOWN_KEY,
                            key
                    )
            );
        }
    }

}

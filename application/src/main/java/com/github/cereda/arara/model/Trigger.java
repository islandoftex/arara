/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2018, Paulo Roberto Massa Cereda 
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

import com.github.cereda.arara.controller.ConfigurationController;
import com.github.cereda.arara.controller.LanguageController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Implements the trigger model. The tool provides triggers, which are a way
 * to alter its internal behaviour according to a list of parameters.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Trigger {

    // the action name and its
    // list of parameters
    private final String action;
    private final List<Object> parameters;

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Constructor.
     * @param action The action name.
     * @param parameters The list of parameters.
     */
    public Trigger(String action, List<Object> parameters) {
        this.action = action;
        this.parameters = parameters;
    }

    /**
     * Gets the action name.
     * @return The action name.
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the list of parameters.
     * @return The list of parameters.
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Returns a textual representation of the current trigger.
     * @return A string containing a textual representation of the current
     * trigger.
     */
    @Override
    public String toString() {
        return "trigger";
    }

    /**
     * Processes the current trigger.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public void process() throws AraraException {

        Map<String, Callable<Object>> mapping =
                new HashMap<String, Callable<Object>>();
        mapping.put("halt", new Callable<Object>() {
            public Object call() {
                ConfigurationController.getInstance().put("trigger.halt", true);
                return null;
            }
        });
        if (mapping.containsKey(action)) {
            try {
                mapping.get(action).call();
            } catch (Exception exception) {
                throw new AraraException(
                        messages.getMessage(
                                Messages.ERROR_TRIGGER_CALL_EXCEPTION,
                                action
                        ),
                        exception
                );
            }
        } else {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_TRIGGER_ACTION_NOT_FOUND,
                            action
                    )
            );
        }
    }

}

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
package com.github.cereda.arara.model

import com.github.cereda.arara.controller.ConfigurationController
import com.github.cereda.arara.controller.LanguageController
import java.util.*
import java.util.concurrent.Callable

/**
 * Implements the trigger model. The tool provides triggers, which are a way
 * to alter its internal behaviour according to a list of parameters.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class Trigger(
        /**
         * The action name.
         */
        val action: String,
        /**
         * The list of parameters.
         */
        val parameters: List<Any>) {

    /**
     * Returns a textual representation of the current trigger.
     *
     * @return A string containing a textual representation of the current
     * trigger.
     */
    // TODO: this does not contain anything about the current trigger
    override fun toString(): String {
        return "trigger"
    }

    /**
     * Processes the current trigger.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun process() {
        val mapping = HashMap<String, Callable<Any?>>()
        mapping["halt"] = Callable<Any?> {
            ConfigurationController.put("trigger.halt", true)
            null
        }
        if (mapping.containsKey(action)) {
            try {
                mapping[action]!!.call()
            } catch (exception: Exception) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_TRIGGER_CALL_EXCEPTION,
                                action
                        ),
                        exception
                )
            }

        } else {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_TRIGGER_ACTION_NOT_FOUND,
                            action
                    )
            )
        }
    }

    companion object {
        // the application messages obtained from the
        // language controller
        private val messages = LanguageController
    }
}

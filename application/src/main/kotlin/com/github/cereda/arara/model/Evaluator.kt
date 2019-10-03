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
import com.github.cereda.arara.utils.CommonUtils
import com.github.cereda.arara.utils.Methods
import org.mvel2.templates.TemplateRuntime

/**
 * Implements the evaluator model, on which a conditional can be analyzed and
 * processed.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class Evaluator {
    // this attribute holds the maximum number of
    // loops arara will accept; it's like
    // reaching infinity
    private val loops: Long = ConfigurationController["execution.loops"] as Long

    // the counter for the current execution, it
    // helps us keep track of the number of times
    // this evaluation has happened, and also to
    // prevent potential infinite loops
    private var counter: Long = 0

    // a flag that indicates the
    // evaluation to halt regardless
    // of the the result
    private var halt: Boolean = false

    /**
     * Evaluate the provided conditional.
     *
     * @param conditional The conditional object.
     * @return A boolean value indicating if the conditional holds.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun evaluate(conditional: Conditional): Boolean {
        // when in dry-run mode, arara
        // always ignore conditional evaluations
        if (ConfigurationController["execution.dryrun"] as Boolean) {
            return false
        }

        // TODO: make exhaustive
        when (conditional.type) {
            Conditional.ConditionalType.NONE -> return false
            Conditional.ConditionalType.IF,
            Conditional.ConditionalType.UNLESS -> if (!halt) {
                halt = true
            } else {
                return false
            }
        }

        // check counters and see if the execution
        // has reached our concept of infinity,
        // thus breaking the cycles
        counter++
        if (conditional.type === Conditional.ConditionalType.WHILE
                && counter > loops
                || conditional.type === Conditional.ConditionalType.UNTIL
                && counter >= loops) {
            return false
        } else {
            val context = Methods.getConditionalMethods()

            try {
                val result = TemplateRuntime.eval("@{ " + conditional.condition + " }", context)
                if (result !is Boolean) {
                    throw AraraException(
                            messages.getMessage(
                                    Messages.ERROR_EVALUATE_NOT_BOOLEAN_VALUE
                            )
                    )
                } else {
                    var value = result
                    if (conditional.type == Conditional.ConditionalType.UNLESS ||
                            conditional.type == Conditional.ConditionalType.UNTIL)
                        value = !value
                    return value
                }
            } catch (exception: RuntimeException) {
                throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_EVALUATE_COMPILATION_FAILED
                        ),
                        exception
                )
            }
        }
    }

    companion object {
        // the application messages obtained from the
        // language controller
        private val messages = LanguageController
    }
}

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
package com.github.cereda.arara.ruleset

/**
 * The conditional class, it represents the type of conditional available
 * for a directive and its corresponding expression to be evaluated.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class Conditional {
    // the conditional type, specified above; the
    // default fallback, as seen in the constructor,
    // is set to NONE, that is, no conditional at all
    var type: ConditionalType = ConditionalType.NONE

    // the expression to be evaluated according to its
    // type; the default fallback, as seen in the
    // constructor, is set to an empty string
    var condition: String = ""

    // these are all types of conditionals arara
    // is able to recognize; personally, I believe
    // they are more than sufficient to cover the
    // majority of test cases
    enum class ConditionalType {
        // evaluated beforehand, directive is interpreted
        // if and only if the result is true
        IF,

        // there is no evaluation, directive is interpreted,
        // no extra effort is needed
        NONE,

        // evaluated beforehand, directive is interpreted
        // if and only if the result is false
        UNLESS,

        // directive is interpreted the first time, then the
        // evaluation is done; while the result is false,
        // the directive is interpreted again and again
        UNTIL,

        // evaluated beforehand, directive is interpreted if
        // and oly if the result is true, and the process is
        // repeated while the result still holds true
        WHILE
    }

    /**
     * Provides a textual representation of the conditional object.
     * @return A string representation of this object.
     */
    override fun toString(): String {
        return "{ $type" +
                if (type != ConditionalType.NONE)
                    ", expression: ${condition.trim()}"
                else "" + " }"
    }

}

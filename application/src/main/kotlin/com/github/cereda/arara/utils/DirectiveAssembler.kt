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
package com.github.cereda.arara.utils

/**
 * Implements a directive assembler in order to help build a directive from a
 * list of strings.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
class DirectiveAssembler {
    // this variable holds a list of
    // line numbers indicating which
    // lines composed the resulting
    // potential directive
    private val lineNumbers = mutableListOf<Int>()

    // this variable holds the textual
    // representation of the directive
    private var text: String = ""

    /**
     * Checks if an append operation is allowed.
     * @return A boolean value indicating if an append operation is allowed.
     */
    val isAppendAllowed: Boolean
        get() = lineNumbers.isNotEmpty()

    /**
     * Adds a line number to the assembler.
     * @param line An integer representing the line number.
     */
    fun addLineNumber(line: Int) {
        lineNumbers.add(line)
    }

    /**
     * Appends the provided line to the assembler text.
     * @param line The provided line.
     */
    fun appendLine(line: String) {
        text = text + " " + line.trim()
    }

    /**
     * Gets the list of line numbers.
     * @return The list of line numbers.
     */
    fun getLineNumbers(): List<Int> {
        return lineNumbers
    }

    /**
     * Gets the text.
     * @return The assembler text, properly trimmed.
     */
    fun getText(): String {
        return text.trim()
    }
}

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
package com.github.cereda.arara.localization;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the language report model.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 5.0
 * @since 5.0
 */
public class LanguageReport {

    // the file reference
    private File reference;

    // list of problematic lines and
    // their corresponding error types
    private final Map<Integer, Character> lines = new HashMap<>();

    // total of checked lines
    private int total;

    /**
     * Constructor.
     */
    public LanguageReport() {
    }

    /**
     * Gets the file reference.
     *
     * @return The file reference.
     */
    public File getReference() {
        return reference;
    }

    /**
     * Gets the problematic lines.
     *
     * @return Problematic lines as list.
     */
    public Map<Integer, Character> getLines() {
        return lines;
    }

    /**
     * Sets the file reference.
     *
     * @param reference The file reference.
     */
    public void setReference(File reference) {
        this.reference = reference;
    }

    /**
     * Add the line to the list of lines.
     *
     * @param line      Line.
     * @param character Character.
     */
    public void addLine(int line, char character) {
        lines.put(line, character);
    }

    /**
     * Gets the total of lines.
     *
     * @return Total of lines.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total of lines.
     *
     * @param total Total of lines.
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Gets the language coverage.
     *
     * @return The language coverage.
     */
    public float getCoverage() {
        if (lines.isEmpty()) {
            return 100f;
        } else {
            return (1f - (float) lines.size() / total) * 100f;
        }
    }

}

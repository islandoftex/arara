/**
 * Language checker, a tool for Arara
 * Copyright (c) 2015, Paulo Roberto Massa Cereda
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated documentation  files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, sublicense,  and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and this  permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT  SHALL THE AUTHORS OR COPYRIGHT HOLDERS  BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR  OTHER LIABILITY, WHETHER IN AN  ACTION OF CONTRACT,
 * TORT OR  OTHERWISE, ARISING  FROM, OUT  OF OR  IN CONNECTION  WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.cereda.arara.langchecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the language report model.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class LanguageReport {

    // the file reference
    private File reference;
    
    // list of problematic lines and
    // their corresponding error types
    private final List<Pair<Integer, Character>> lines;
    
    // total of checked lines
    private int total;

    /**
     * Constructor.
     */
    public LanguageReport() {
        lines = new ArrayList<>();
    }

    /**
     * Gets the file reference.
     * @return The file reference.
     */
    public File getReference() {
        return reference;
    }

    /**
     * Gets the problematic lines.
     * @return Problematic lines as list.
     */
    public List<Pair<Integer, Character>> getLines() {
        return lines;
    }

    /**
     * Sets the file reference.
     * @param reference The file reference.
     */
    public void setReference(File reference) {
        this.reference = reference;
    }
    
    /**
     * Add the line to the list of lines.
     * @param line Line.
     * @param character Character.
     */
    public void addLine(int line, char character) {
        Pair<Integer, Character> pair = new Pair<>();
        pair.setFirst(line);
        pair.setSecond(character);
        lines.add(pair);
    }

    /**
     * Gets the total of lines.
     * @return Total of lines.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total of lines.
     * @param total Total of lines.
     */
    public void setTotal(int total) {
        this.total = total;
    }
    
    /**
     * Gets the language coverage.
     * @return The language coverage.
     */
    public float getCoverage() {
        if (lines.isEmpty()) {
            return 100;
        }
        else {
            return (float) (1 - (float) lines.size() / total) * 100;
        }
    }
    
}

/**
 * Rule checker, a tool for Arara
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
package com.github.cereda.arara.rulechecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the analysis class.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class RuleReport {
    
    // the file reference
    private final File reference;
    
    // the list of tasks
    private List<Pair<Boolean, String>> tasks;

    /**
     * Constructor.
     * @param reference The file reference.
     */
    public RuleReport(File reference) {
        this.reference = reference;
        this.tasks = new ArrayList<>();
    }
    
    /**
     * Get the rule coverage.
     * @return A float value indicating the rule coverage.
     */
    public float getCoverage() {
        if (tasks.isEmpty()) {
            return 0;
        }
        else {
            int done = 0;
            for (Pair<Boolean, String> task : tasks) {
                if (task.getFirst()) {
                    done++;
                }
            }
            return (float) done / tasks.size() * 100;
        }
    }

    /**
     * Gets the file reference.
     * @return The file reference.
     */
    public File getReference() {
        return reference;
    }

    /**
     * Gets the list of tasks.
     * @return The list of tasks.
     */
    public List<Pair<Boolean, String>> getTasks() {
        return tasks;
    }

    /**
     * Sets the list of tasks.
     * @param tasks The list of tasks.
     */
    public void setTasks(List<Pair<Boolean, String>> tasks) {
        this.tasks = tasks;
    }
    
}

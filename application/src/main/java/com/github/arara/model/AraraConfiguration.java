/**
 * \cond LICENSE
 * Arara -- the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda
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
 * \endcond
 * 
 * AraraConfiguration: This class holds the model for the arara configuration
 * file.
 */
// package definition
package com.github.arara.model;

// needed imports
import com.github.arara.exception.AraraException;
import com.github.arara.utils.AraraUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.mvel2.templates.TemplateRuntime;

/**
 * Holds the model for the arara configuration file.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class AraraConfiguration {

    // the application language
    private String language;
    // the list of search paths
    private List<String> paths;
    // the list of filetypes
    private List<AraraFilePattern> filetypes;
    // the path runtime exception
    private RuntimeException pathRuntimeException;
    // the path IO exception
    private IOException pathIOException;

    /**
     * Getter for the search paths.
     *
     * @return The search paths.
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Getter for the language.
     *
     * @return The language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Setter for the language.
     *
     * @param language The language.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Setter for the search paths.
     *
     * @param rules The search paths.
     */
    public void setPaths(List<String> rules) throws AraraException, Exception {
        this.paths = rules;

        // refresh paths
        refreshPaths();
    }

    /**
     * Getter for the filetypes.
     *
     * @return The filetypes.
     */
    public List<AraraFilePattern> getFiletypes() {
        return filetypes;
    }

    /**
     * Setter for the filetypes.
     *
     * @param filetypes The filetypes.
     */
    public void setFiletypes(List<AraraFilePattern> filetypes) {
        this.filetypes = filetypes;
    }

    /**
     * Constructor.
     */
    public AraraConfiguration() {

        // creates both lists
        paths = new ArrayList<String>();
        filetypes = new ArrayList<AraraFilePattern>();
        pathRuntimeException = null;
    }

    /**
     * Refreshes all paths, replacing possible variables in the template
     */
    private void refreshPaths() throws AraraException, Exception {

        // create a map
        HashMap map = new HashMap();

        // add the variables
        map.put("userhome", SystemUtils.USER_HOME);

        // for every path in the list
        for (int i = 0; i < paths.size(); i++) {

            try {

                // remove the keyword
                String fullPath = AraraUtils.removeKeyword(paths.get(i));

                // apply the template
                fullPath = (String) TemplateRuntime.eval(fullPath, map);

                try {

                    // attempt to create a file reference and
                    // get the canonical path, if the operation
                    // succeeds, it's a valid path
                    File validFile = new File(fullPath);
                    validFile.getCanonicalPath();

                    // sanitize the path in order to be displayed correctly
                    fullPath = new File(new File(fullPath).toURI()).getPath();

                    // set the path in the list
                    paths.set(i, fullPath);

                } catch (IOException ioException) {

                    // the path is terribly invalid, get the exception,
                    // if null
                    if (pathIOException == null) {

                        // set the path IO exception
                        pathIOException = ioException;
                    }
                }

            } catch (RuntimeException runtimeException) {

                // an error has occurred, register the exception for
                // later access

                // if there isn't an exception yet
                if (pathRuntimeException == null) {

                    // set the exception
                    pathRuntimeException = runtimeException;
                }

            }
        }
    }

    /**
     * Getter for the path runtime exception.
     *
     * @return The path runtime exception.
     */
    public RuntimeException getPathRuntimeException() {
        return pathRuntimeException;
    }

    /**
     * Getter for the path IO exception.
     *
     * @return The path IO exception.
     */
    public IOException getPathIOException() {
        return pathIOException;
    }
}

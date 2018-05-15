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

import com.github.cereda.arara.utils.CommonUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang.SystemUtils;
import org.mvel2.templates.TemplateRuntime;

/**
 * Implements the configuration resource model.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Resource {

    // rule paths
    private List<String> paths;
    
    // file types
    private List<FileTypeResource> filetypes;
    
    // the application language
    private String language;
    
    // maximum number of loops
    private long loops;
    
    // verbose flag
    private boolean verbose;
    
    // logging flag
    private boolean logging;
    
    // database name
    private String dbname;
    
    // log name
    private String logname;
    
    // header flag
    private boolean header;

    // map of preambles
    private Map<String, String> preambles;
    
    /**
     * Gets the rule paths.
     * @return The rule paths.
     */
    public List<String> getPaths() {
        if (paths != null) {
            
            final Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Object> user = new HashMap<String, Object>();
            user.put("home", SystemUtils.USER_HOME);
            user.put("dir", SystemUtils.USER_DIR);
            user.put("name", SystemUtils.USER_NAME);
            map.put("user", user);
            
            Collection<String> result = CollectionUtils.collect(
                    paths, new Transformer<String, String>() {
                public String transform(String input) {
                    String path = CommonUtils.removeKeyword(input);
                    try {
                        path = (String) TemplateRuntime.eval(path, map);
                    }
                    catch (RuntimeException nothandled) {
                        // do nothing, gracefully fallback to
                        // the default, unparsed path
                    }
                    return path;
                }
            });
            paths = new ArrayList<String>(result);
        }
        return paths;
    }

    /**
     * Sets the rule paths.
     * @param paths The rule paths.
     */
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    /**
     * Gets the list of file types.
     * @return The list of file types.
     */
    public List<FileTypeResource> getFiletypes() {
        return filetypes;
    }
    
    /**
     * Sets the list of file types.
     * @param filetypes The list of file types.
     */
    public void setFiletypes(List<FileTypeResource> filetypes) {
        this.filetypes = filetypes;
    }

    /**
     * Gets the language.
     * @return The language.
     */
    public String getLanguage() {
        return CommonUtils.removeKeyword(language);
    }

    /**
     * Sets the language.
     * @param language The language.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get the maximum number of loops.
     * @return The maximum number of loops.
     */
    public long getLoops() {
        return loops;
    }

    /**
     * Sets the maximum number of loops.
     * @param loops The maximum number of loops.
     */
    public void setLoops(long loops) {
        this.loops = loops;
    }

    /**
     * Checks if verbose mode is active.
     * @return A boolean value.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the verbose mode.
     * @param verbose A boolean value.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Checks if logging mode is active.
     * @return A boolean value.
     */
    public boolean isLogging() {
        return logging;
    }

    /**
     * Sets the logging mode.
     * @param logging A boolean value.
     */
    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    /**
     * Gets the database name.
     * @return The database name.
     */
    public String getDbname() {
        return CommonUtils.removeKeyword(dbname);
    }

    /**
     * Sets the database name.
     * @param dbname The database name.
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    /**
     * Gets the log name.
     * @return The log name.
     */
    public String getLogname() {
        return CommonUtils.removeKeyword(logname);
    }

    /**
     * Sets the log name.
     * @param logname The log name.
     */
    public void setLogname(String logname) {
        this.logname = logname;
    }

    /**
     * Gets the map of preambles.
     * @return Map of preambles.
     */
    public Map<String, String> getPreambles() {
        return preambles;
    }

    /**
     * Sets the map of preambles.
     * @param preambles Map of preambles.
     */
    public void setPreambles(Map<String, String> preambles) {
        this.preambles = preambles;
    }

    /**
     * Gets the logical value of the header flag.
     * @return Logical value of the header flag.
     */
    public boolean isHeader() {
        return header;
    }

    /**
     * Sets the logical value of the header flag.
     * @param header The header flag.
     */
    public void setHeader(boolean header) {
        this.header = header;
    }

}

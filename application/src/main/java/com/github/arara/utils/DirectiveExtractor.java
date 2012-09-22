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
 * DirectiveExtractor: This class reads the file and extracts all the directives
 * it could find.
 */
// package definition
package com.github.arara.utils;

// needed imports
import com.github.arara.exception.AraraException;
import com.github.arara.model.AraraDirective;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Reads the file and extracts all the directives it could find.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 1.0
 */
public class DirectiveExtractor {

    // the logger
    final static Logger logger = LoggerFactory.getLogger(DirectiveExtractor.class);
    // the list of directives
    private List<AraraDirective> directives;
    // the file
    private File file;
    // the current line number
    private int currentLineNumber;
    // the configuration object
    private ConfigurationLoader configuration;
    // the localization class
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * Constructor.
     */
    public DirectiveExtractor() {

        // create new list
        directives = new ArrayList<AraraDirective>();

        // set file to null
        file = null;
    }

    /**
     * Constructor.
     *
     * @param file The file.
     */
    public DirectiveExtractor(File file, ConfigurationLoader configuration) {

        // create a new list
        directives = new ArrayList<AraraDirective>();

        this.configuration = configuration;

        // set file
        this.file = file;
    }

    /**
     * Setter for file.
     *
     * @param file The file.
     */
    public void setFile(File file) {

        // set the file
        this.file = file;
    }

    /**
     * Getter for Arara directives.
     *
     * @return A list containing all Arara directives.
     */
    public List<AraraDirective> getDirectives() {

        // return the list
        return directives;
    }

    /**
     * Extracts the directives from the file.
     *
     * @throws FileNotFoundException Raised when file not found.
     * @throws IOException Raised when an IO error happened.
     * @throws AraraException Raised when there's a problem with the directive.
     */
    public void extract() throws FileNotFoundException, IOException, AraraException {
        
        // create a new file reader
        FileReader fileReader = new FileReader(file);

        // create a new buffer
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        
        // variable to hold the current line
        String currentLine;

        // the line count
        currentLineNumber = 0;

        // log directives
        logger.info("Reading directives from {}.", file.getName());

        // while not EOF
        while ((currentLine = bufferedReader.readLine()) != null) {

            // increment the current line number
            currentLineNumber++;
            
            // extract directive
            extractDirective(currentLine);
        }
        
        // close the buffer
        bufferedReader.close();

        // close the reader
        fileReader.close();

    }

    /**
     * Extracts the Arara directives from the current line.
     *
     * @param currentLine The current line.
     * @throws AraraException Raised when there is a problem with the directive.
     */
    private void extractDirective(String currentLine) throws AraraException {
        
        // get the pattern for the chosen filetype
        Pattern linePattern = Pattern.compile(configuration.getChosenFilePattern().getPattern());
        
        // create the matcher according to the pattern
        Matcher matcher = linePattern.matcher(currentLine);
        
        // if the pattern is found
        if (matcher.find()) {

            // get the substring
            currentLine = (currentLine.substring(matcher.end(), currentLine.length())).trim();
            
            // there is actually something after the keyword
            if (!currentLine.isEmpty()) {

                // log directive
                logger.trace(localization.getMessage("Log_DirectiveFound", currentLineNumber, currentLine));

                // if there are arguments
                if (currentLine.contains("{")) {

                    // call the directive method
                    addAraraDirective(currentLine);

                } else {

                    // call the empty directive
                    addEmptyAraraDirective(currentLine);
                }
            }
        }
    }

    /**
     * Extracts an empty Arara directive.
     *
     * @param currentLine The current line.
     */
    private void addEmptyAraraDirective(String currentLine) {

        // create a new directive
        AraraDirective araraDirective = new AraraDirective();

        // set the name
        araraDirective.setName(currentLine);

        // set the config
        araraDirective.setConfig(new HashMap());

        // set the line number, in case of error
        araraDirective.setLineNumber(currentLineNumber);

        // add the directive to the list
        directives.add(araraDirective);

    }

    /**
     * Extracts am Arara directive.
     *
     * @param currentLine The current line.
     * @throws AraraException Raised when there is a problem with the directive.
     */
    private void addAraraDirective(String currentLine) throws AraraException {

        // lets try
        try {

            // create a new YAML parser
            Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new AraraResolver());

            // create a new map and load the current line
            Map config = (Map) yaml.load(currentLine);

            // create a new directive
            AraraDirective ad = new AraraDirective();

            // set the name
            ad.setName(currentLine);

            // set the config
            ad.setConfig(config);

            // set line number. in case of error
            ad.setLineNumber(currentLineNumber);

            // add the directive to the list
            directives.add(ad);

        } catch (MarkedYAMLException yamlException) {

            // malformed directive, throw exception
            throw new AraraException(localization.getMessage("Error_InvalidYAMLDirective", currentLineNumber).concat("\n\n").concat(AraraUtils.extractInformationFromYAMLException(yamlException)));
        }
    }
}

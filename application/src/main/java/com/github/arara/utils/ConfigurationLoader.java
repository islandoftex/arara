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
 * ConfigurationLoader: This class provides an object handler for the arara
 * configuration file.
 */
// package definition
package com.github.arara.utils;

// needed imports
import com.github.arara.Arara;
import com.github.arara.exception.AraraException;
import com.github.arara.model.AraraConfiguration;
import com.github.arara.model.AraraFilePattern;
import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Provides an object handler for the arara configuration file.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class ConfigurationLoader {

    // list of search paths
    private List<String> paths;
    // the application path, that is, where arara is located
    private String applicationPath;
    // list of file patterns
    private List<AraraFilePattern> filePatterns;
    // list of valid extensions
    private String[] validExtensions;
    // the chosen pattern
    private AraraFilePattern chosenFilePattern;
    // the localization class
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * Constructor.
     */
    public ConfigurationLoader() {

        // create a new list of paths
        paths = new ArrayList<String>();

        // try to obtain the application path
        try {
            applicationPath = Arara.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            applicationPath = URLDecoder.decode(applicationPath, "UTF-8");
            applicationPath = new File(applicationPath).getParentFile().getPath();
        } catch (Exception exception) {
            applicationPath = "";
        }
    }

    /**
     * Loads the configuration settings, either the default setup or through the
     * configuration file.
     *
     * @throws Exception An exception is thrown if something unexpected happens
     * during the execution of this method.
     */
    public void load() throws Exception {

        // create the default patterns

        // for .tex files
        AraraFilePattern texPattern = new AraraFilePattern();
        texPattern.setExtension("tex");
        texPattern.setPattern("^(\\s)*%\\s+");

        // for .dtx files
        AraraFilePattern dtxPattern = new AraraFilePattern();
        dtxPattern.setExtension("dtx");
        dtxPattern.setPattern("^(\\s)*%\\s+");

        // for .ltx files
        AraraFilePattern ltxPattern = new AraraFilePattern();
        ltxPattern.setExtension("ltx");
        ltxPattern.setPattern("^(\\s)*%\\s+");

        // for .sk files
        AraraFilePattern skPattern = new AraraFilePattern();
        skPattern.setExtension("sk");
        skPattern.setPattern("^(\\s)*[%#]\\s+");

        // create a list of default patterns
        List<AraraFilePattern> defaultPatterns = new ArrayList<AraraFilePattern>();

        // add the default ones
        defaultPatterns.add(texPattern);
        defaultPatterns.add(dtxPattern);
        defaultPatterns.add(ltxPattern);
        defaultPatterns.add(skPattern);

        // create a list of file patterns
        filePatterns = new ArrayList<AraraFilePattern>();

        // create a file reference
        File configurationFile = new File(SystemUtils.USER_HOME + File.separator + AraraConstants.ARARACONFIG);

        // if there is not a configuration file
        if (!configurationFile.exists()) {

            // there's only one path to look for rules
            paths.add(applicationPath + File.separator + "rules");

            // the only file patterns are the default ones
            filePatterns.addAll(defaultPatterns);

            // create an array of valid extensions
            validExtensions = new String[filePatterns.size()];

            // add them according to the file patterns
            for (int i = 0; i < filePatterns.size(); i++) {
                validExtensions[i] = ".".concat(filePatterns.get(i).getExtension());
            }

            // simply return, there's nothing more to do
            return;
        }

        // create the YAML representer
        Representer representer = new Representer();
        representer.addClassTag(AraraConfiguration.class, new Tag("!config"));
        representer.addClassTag(AraraFilePattern.class, new Tag("!pattern"));

        // create the YAML parser
        Yaml yaml = new Yaml(new Constructor(AraraConfiguration.class), representer);

        // create the file reader
        FileReader fileReader = new FileReader(configurationFile);

        // set the new configuration object
        AraraConfiguration configuration = null;

        try {

            // try to read the configuration object
            configuration = (AraraConfiguration) yaml.load(fileReader);

        } catch (MarkedYAMLException yamlException) {
            
            // there's an error with the YAML file, so an exception is thrown
            AraraLogging.enableLogging(false);
            throw new AraraException(localization.getMessage("Error_InvalidYAMLConfigurationFile").concat("\n\n").concat(AraraUtils.extractInformationFromYAMLException(yamlException)));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // close the file reader
        fileReader.close();

        // check if the configuration is invalid
        if (configuration == null) {

            // it's invalid, raise an exception
            AraraLogging.enableLogging(false);
            throw new AraraException(localization.getMessage("Error_InvalidConfigurationFile"));
        }

        // check if there's an available language
        if (configuration.getLanguage() != null) {

            // create a new language control
            LanguageController languageController = new LanguageController();

            // if the language is not valid
            if (languageController.setLanguage(configuration.getLanguage()) == false) {

                // it's invalid, raise an exception
                AraraLogging.enableLogging(false);
                throw new AraraException(localization.getMessage("Error_InvalidLanguageConfigurationFile", languageController.getLanguagesList()));
            }
        }

        // check if there are new paths
        if (configuration.getPaths() != null) {
            
            // a runtime exception happened when resolving some
            // path names
            if (configuration.getPathRuntimeException() != null) {
                
                // raise an exception
                AraraLogging.enableLogging(false);
                throw new AraraException(localization.getMessage("Error_PathRuntimeErrorConfigurationFile", AraraUtils.getVariableFromException(configuration.getPathRuntimeException())));
            }
            
            // a IO exception happened when checking if it was
            // a valid path entry
            if (configuration.getPathIOException() != null) {
                
                // raise an exception
                AraraLogging.enableLogging(false);
                throw new AraraException(localization.getMessage("Error_PathIOErrorConfigurationFile"));
            }

            // for every path set
            for (String currentPath : configuration.getPaths()) {

                // add it to the paths list
                paths.add(currentPath);
            }
        }

        // the last path to be added is the application path
        paths.add(applicationPath + File.separator + "rules");

        // check if there are new file patterns
        if (configuration.getFiletypes() != null) {

            // get all file patterns
            List<AraraFilePattern> filetypes = configuration.getFiletypes();

            // reorder the list of filetypes
            for (int i = 0; i < defaultPatterns.size(); i++) {

                // if the filetype is not in the default list
                if (!isAlreadyDefined(defaultPatterns.get(i), filetypes)) {

                    // simply add it
                    filePatterns.add(defaultPatterns.get(i));
                }
            }

            // merge the two lists
            filePatterns.addAll(filetypes);

            // create a new array of valid extensions
            validExtensions = new String[filePatterns.size()];
            int counter = 0;

            // sanity check, if there are valid extensions and patterns
            for (AraraFilePattern currentPattern : filePatterns) {

                // check if either of those fields is missing
                if ((currentPattern.getPattern() == null) || (currentPattern.getExtension() == null)) {

                    // it's invalid, raise an exception
                    AraraLogging.enableLogging(false);
                    throw new AraraException(localization.getMessage("Error_InvalidFiletypesConfigurationFile"));

                }

                // add the dot to the extension
                validExtensions[counter] = ".".concat(currentPattern.getExtension());
                counter++;
            }
        } else {

            // no new filetypes, simply merge the lists
            filePatterns.addAll(defaultPatterns);

            // create a new array for extensions
            validExtensions = new String[filePatterns.size()];

            // iterate through all filetypes and add the extension to the array
            for (int i = 0; i < filePatterns.size(); i++) {
                validExtensions[i] = ".".concat(filePatterns.get(i).getExtension());
            }
        }

    }

    /**
     * Checks if the filetype is already defined in the list, and updates the
     * latter if needed.
     *
     * @param filePattern The filetype object.
     * @param list The list to be inspected.
     * @return A boolean indicating if the filetype is already defined in the
     * list.
     */
    private boolean isAlreadyDefined(AraraFilePattern filePattern, List<AraraFilePattern> list) {

        // iterate through all elements
        for (int i = 0; i < list.size(); i++) {

            // check if it already exists
            if (filePattern.getExtension().equalsIgnoreCase(list.get(i).getExtension())) {

                // update the element if needed
                if (list.get(i).getPattern() == null) {
                    list.set(i, filePattern);
                }

                // already defined
                return true;
            }
        }

        // not found
        return false;
    }

    /**
     * Getter for the paths.
     *
     * @return The paths.
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Getter for the file patterns.
     *
     * @return The file patterns.
     */
    public List<AraraFilePattern> getFilePatterns() {
        return filePatterns;
    }

    /**
     * Getter for the valid extensions available.
     *
     * @return An array containing the valid extensions.
     */
    public String[] getValidExtensions() {
        return validExtensions;
    }

    /**
     * Setter for the chosen file pattern.
     *
     * @param extension A string containing a reference for the file extension.
     */
    public void setChosenFilePattern(String extension) {

        // for every file pattern
        for (AraraFilePattern currentPattern : filePatterns) {

            // if the extension matches
            if (".".concat(currentPattern.getExtension()).equals(extension)) {

                // we found our pattern
                chosenFilePattern = currentPattern;

                // return
                return;
            }
        }

        // nothing found
        chosenFilePattern = null;
    }

    /**
     * Getter for the chosen file pattern.
     *
     * @return The chosen file pattern.
     */
    public AraraFilePattern getChosenFilePattern() {
        return chosenFilePattern;
    }
}

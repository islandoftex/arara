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
 * @version 4.0
 */
public class DirectiveExtractor {

    // the logger
    /** Constant <code>logger</code> */
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
    /** Constant <code>localization</code> */
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
     * @param configuration The configuration loader object.
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
     * Getter for arara directives.
     *
     * @return A list containing all arara directives.
     */
    public List<AraraDirective> getDirectives() {

        // return the list
        return directives;
    }

    /**
     * Extracts the directives from the file.
     *
     * @throws java.io.FileNotFoundException Raised when file not found.
     * @throws java.io.IOException Raised when an IO error happened.
     * @throws com.github.arara.exception.AraraException Raised when there's a
     * problem with the directive.
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
     * Extracts the arara directives from the current line.
     *
     * @param currentLine The current line.
     * @throws com.github.arara.exception.AraraException Raised when there is a
     * problem with the directive.
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
            if (!"".equals(currentLine)) {

                // log directive
                logger.trace(localization.getMessage("Log_DirectiveFound", currentLineNumber, currentLine));

                // look for a full directive
                if (AraraUtils.checkForValidDirective(currentLine, AraraConstants.FULLDIRECTIVEPATTERN)) {
                    
                    // add it
                    addAraraDirective(currentLine, false);
                }
                else {
                    
                    // look for an empty directive
                    if (AraraUtils.checkForValidDirective(currentLine, AraraConstants.EMPTYDIRECTIVEPATTERN)) {
                        
                        // add it
                        addEmptyAraraDirective(currentLine, false);
                    }
                    else {
                        
                        // look for a full directive with conditional
                        if (AraraUtils.checkForValidDirective(currentLine, AraraConstants.FULLDIRECTIVECONDITIONALPATTERN)) {
                            
                            // add it
                            addAraraDirective(currentLine, true);
                            
                        }
                        else {
                            
                            // look for an empty directive with conditional
                            if (AraraUtils.checkForValidDirective(currentLine, AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN)) {
                                
                                // add it
                                addEmptyAraraDirective(currentLine, true);
                                
                            }
                            else {
                         
                                // an invalid directive was found, throw error
                                throw new AraraException(localization.getMessage("Error_InvalidDirective", currentLineNumber));
                                
                            }
                            
                        }
                    }
                }
                
            }
        }
    }

    /**
     * Extracts an empty arara directive.
     *
     * @param currentLine The current line.
     * @param conditional a boolean.
     */
    private void addEmptyAraraDirective(String currentLine, boolean conditional) {

        // get the result from the directive extraction
        String[] result = AraraUtils.extractDirective(currentLine, (conditional == true ? AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN : AraraConstants.EMPTYDIRECTIVEPATTERN));
        
        // create a new directive
        AraraDirective araraDirective = new AraraDirective();

        // set the name
        araraDirective.setName(result[0]);

        // set the config
        araraDirective.setConfig(new HashMap());

        // set the line number, in case of error
        araraDirective.setLineNumber(currentLineNumber);
        
        // we have a conditional
        if (conditional) {
            
            // let's add it
            araraDirective.setConditional(result[2], AraraUtils.getConditionalType(result[1]));
        }
        
        // add the directive to the list
        directives.add(araraDirective);

    }

    /**
     * Extracts am arara directive.
     *
     * @param currentLine The current line.
     * @throws com.github.arara.exception.AraraException Raised when there is a
     * problem with the directive.
     * @param conditional a boolean.
     */
    private void addAraraDirective(String currentLine, boolean conditional) throws AraraException {
        
        // lets try
        try {

            // get the result from the directive extraction
            String[] result = AraraUtils.extractDirective(currentLine, (conditional == true ? AraraConstants.FULLDIRECTIVECONDITIONALPATTERN : AraraConstants.FULLDIRECTIVEPATTERN));
            
            // create a new YAML parser
            Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new AraraResolver());

            // create a new map and load the current line
            Map config = (Map) yaml.load(result[0].concat(": ".concat(result[1])));

            // create a new directive
            AraraDirective ad = new AraraDirective();

            // set the name
            ad.setName(result[0]);

            // set the config
            ad.setConfig(config);

            // set line number. in case of error
            ad.setLineNumber(currentLineNumber);
            
            // we have a conditional
            if (conditional) {
                
                // add it
                ad.setConditional(result[3], AraraUtils.getConditionalType(result[2]));
            }

            // add the directive to the list
            directives.add(ad);

        } catch (MarkedYAMLException yamlException) {

            // malformed directive, throw exception
            throw new AraraException(localization.getMessage("Error_InvalidYAMLDirective", currentLineNumber).concat("\n\n").concat(AraraUtils.extractInformationFromYAMLException(yamlException)));
        }
    }
    
}

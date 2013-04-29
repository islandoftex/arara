package com.github.arara.model;

// needed imports
import java.util.Map;

/**
 * Provides the model for representing the arara directives found in the source
 * file.
 *
 * @since 1.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraDirective {

    // the directive name
    private String name;
    // the configuration
    private Map config;
    // the line number
    private int lineNumber = 0;
    // the conditional associated to the
    // current directive, if any
    private AraraConditional conditional;

    /**
     * Constructor.
     *
     * @param name The directive name.
     * @param config The configuration.
     */
    public AraraDirective(String name, Map config) {

        // set the name
        this.name = name;

        // set the configuration
        this.config = config;

        // create a new object
        conditional = new AraraConditional();
    }

    /**
     * Constructor.
     */
    public AraraDirective() {

        // create a new object
        conditional = new AraraConditional();
    }

    /**
     * Getter for configuration.
     *
     * @return The configuration.
     */
    public Map getConfig() {

        // return it
        return config;
    }

    /**
     * Setter for configuration.
     *
     * @param config The configuration.
     */
    public void setConfig(Map config) {

        // set the configuration
        this.config = config;
    }

    /**
     * Getter for the directive name.
     *
     * @return The directive name.
     */
    public String getName() {

        // return it
        return name;
    }

    /**
     * Setter for the directive name.
     *
     * @param name The directive name.
     */
    public void setName(String name) {

        // set it
        this.name = name;
    }

    /**
     * Getter for the line number.
     *
     * @return The line number.
     */
    public int getLineNumber() {

        // return it
        return lineNumber;
    }

    /**
     * Setter for the line number.
     *
     * @param lineNumber The line number.
     */
    public void setLineNumber(int lineNumber) {

        // set it
        this.lineNumber = lineNumber;
    }

    /**
     * Setter for the directive conditional.
     *
     * @param text The condition itself.
     * @param type The type of conditional.
     */
    public void setConditional(String text, AraraConditionalType type) {

        // set values
        conditional.setCondition(text);
        conditional.setType(type);
    }

    /**
     * Getter for the directive conditional.
     *
     * @return A conditional object.
     */
    public AraraConditional getConditional() {
        return conditional;
    }
    
}

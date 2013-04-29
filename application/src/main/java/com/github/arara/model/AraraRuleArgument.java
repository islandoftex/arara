package com.github.arara.model;

// needed import
import com.github.arara.utils.AraraUtils;

/**
 * Provides the model for representing a plain Arara rule argument based on the
 * YAML files. This class will be used to map YAML rules into Java objects.
 *
 * @since 1.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraRuleArgument {

    // the rule argument identifier
    private String identifier;
    // the command flag
    private String flag;
    // the default value
    private String defaultValue;

    /**
     * Getter for the default value.
     *
     * @return The default value.
     */
    public String getDefault() {
        return defaultValue;
    }

    /**
     * Setter for the default value.
     *
     * @param defaultValue The default value.
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = AraraUtils.removeKeyword(defaultValue);
    }

    /**
     * Getter for flag.
     *
     * @return The command flag.
     */
    public String getFlag() {

        // return it
        return flag;
    }

    /**
     * Setter for flag.
     *
     * @param flag The command flag.
     */
    public void setFlag(String flag) {

        // set the flag
        this.flag = AraraUtils.removeKeyword(flag);
    }

    /**
     * Getter for the argument identifier.
     *
     * @return The argument identifier.
     */
    public String getIdentifier() {

        // return it
        return identifier;
    }

    /**
     * Setter for the argument identifier.
     *
     * @param identifier The argument identifier.
     */
    public void setIdentifier(String identifier) {

        // set the identifier
        this.identifier = identifier;
    }
    
}

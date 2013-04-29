package com.github.arara.model;

// needed imports
import com.github.arara.utils.AraraConstants;

/**
 * Maps the supported filetypes.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraFilePattern {

    // file extension
    private String extension;
    // file pattern
    private String pattern;

    /**
     * Getter for the file extension.
     *
     * @return The file extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Setter for the file extension.
     *
     * @param extension The file extension.
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Getter for the file pattern.
     *
     * @return The file pattern.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Setter for the file pattern.
     *
     * @param pattern The file pattern.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern.concat(AraraConstants.DIRECTIVEPREFIX);
    }
    
}

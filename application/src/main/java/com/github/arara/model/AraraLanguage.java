package com.github.arara.model;

// needed import
import java.util.Locale;

/**
 * Holds the data model for the arara language specification.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraLanguage {

    // language name
    private String name;
    // language code
    private String code;
    // language locale
    private Locale locale;

    /**
     * Getter for the language name.
     *
     * @return The language name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the language name.
     *
     * @param name The language name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the language code.
     *
     * @return The language code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter for the language code.
     *
     * @param code The language code.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter for the language locale.
     *
     * @return The language locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Setter for the language locale.
     *
     * @param locale The language locale.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Constructor.
     *
     * @param name The language name.
     * @param code The language code.
     * @param locale The language locale.
     */
    public AraraLanguage(String name, String code, Locale locale) {
        this.name = name;
        this.code = code;
        this.locale = locale;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        return name.concat(" [").concat(code).concat("]");
    }
}

package com.github.arara.utils;

// needed imports
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contains a few helper methods in order to ease the localization process
 * during the execution. This class is a singleton reference.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraLocalization {

    // the singleton reference
    /** Constant <code>selfRef</code> */
    private static AraraLocalization selfRef;
    // the resource bundle for the whole application
    private ResourceBundle resources;

    /**
     * Constructs the singleton instance.
     */
    private AraraLocalization() {

        // set the self-reference
        selfRef = this;

        // instead of relying in the default locale from the
        // operating system, always fallback to english
        Locale.setDefault(Locale.ENGLISH);
        
        // get the resource bundle
        resources = ResourceBundle.getBundle("com.github.arara.localization.Messages");
    }

    /**
     * Provides reference to the singleton object.
     *
     * @return The singleton instance.
     */
    public static final AraraLocalization getInstance() {

        // if there's no current object, call the private constructor
        if (selfRef == null) {
            selfRef = new AraraLocalization();
        }

        // return the actual object
        return selfRef;
    }

    /**
     * Returns a simple message from the resource bundle.
     *
     * @param message The message identifier.
     * @return A string from the resource bundle.
     */
    public String getMessage(String message) {
        return resources.getString(message);
    }

    /**
     * Returns a message containing parameters from the resource bundle.
     *
     * @param message The message identifier.
     * @param parameters A list of parameters to be replaced in the message.
     * @return A string from the resource bundle, with the correct parameters.
     */
    public String getMessage(String message, Object... parameters) {
        return MessageFormat.format(resources.getString(message), parameters);
    }

    /**
     * Refresh the resource bundle reference, if by any chance, the application
     * language is changed in runtime.
     */
    public void refresh() {

        // clear cache and reload the resource bundle
        resources = ResourceBundle.getBundle("com.github.arara.localization.Messages");
    }
    
}

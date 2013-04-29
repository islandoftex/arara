package com.github.arara.utils;

// needed imports
import com.github.arara.model.AraraLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Holds the language model controller for arara.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class LanguageController {

    // list of available languages
    private List<AraraLanguage> languages;
    // the localization singleton instance
    /** Constant <code>localization</code> */
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * Constructor.
     */
    public LanguageController() {

        // create a new list of languages
        languages = new ArrayList<AraraLanguage>();

        // add the resources
        languages.add(new AraraLanguage("English", "en", new Locale("en")));
//        languages.add(new AraraLanguage("Brazilian Portuguese", "ptbr", new Locale("pt", "BR")));
//        languages.add(new AraraLanguage("Italian", "it", new Locale("it")));
//        languages.add(new AraraLanguage("French", "fr", new Locale("fr")));
//        languages.add(new AraraLanguage("Spanish", "es", new Locale("es")));
//        languages.add(new AraraLanguage("German", "de", new Locale("de")));
//        languages.add(new AraraLanguage("Turkish", "tr", new Locale("tr", "TR")));
//        languages.add(new AraraLanguage("Russian", "ru", new Locale("ru")));

    }

    /**
     * Sets the current language according to the provided country code.
     *
     * @param code The country code.
     * @return A boolean value indicating if the new language was properly
     * applied.
     */
    public boolean setLanguage(String code) {
        
        // get the index of the language
        int i = getIndex(code);
        
        // if it's valid
        if (i != -1) {
            
            // set locale and refresh the resource bundle
            Locale.setDefault(languages.get(i).getLocale());
            localization.refresh();
            
            // everything went fine
            return true;
        }
        
        // the country code is invalid
        return false;
    }

    /**
     * Returns a string containing the list of available languages.
     *
     * @return A string containing the list of available languages.
     */
    public String getLanguagesList() {
        
        // get the first one in the list
        String result = languages.get(0).toString();
        
        // iterate through the rest
        for (int i = 1; i < languages.size(); i++) {
            
            // separate them by commas
            result = result.concat(", ").concat(languages.get(i).toString());
        }
        
        // return the string
        return result;
    }

    /**
     * Prints the language help.
     */
    public void printLanguageHelp() {
        
        // print message
        System.out.println(AraraUtils.wrap(localization.getMessage("Error_InvalidLanguage").concat("\n\n").concat(getLanguagesList()).concat("\n")));
    }

    /**
     * Gets the index for the country code.
     *
     * @param code The country code.
     * @return An integer representing the index.
     */
    private int getIndex(String code) {
        
        // for every language
        for (int i = 0; i < languages.size(); i++) {
            
            // if the codes are equal
            if (languages.get(i).getCode().equalsIgnoreCase(code)) {
                
                // return the current counter
                return i;
            }
        }
        
        // nothing was found, return a negative value
        return -1;
    }
}

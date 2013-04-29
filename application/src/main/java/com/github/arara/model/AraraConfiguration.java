package com.github.arara.model;

// needed imports
import com.github.arara.exception.AraraException;
import com.github.arara.utils.AraraUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.mvel2.templates.TemplateRuntime;

/**
 * Holds the model for the arara configuration file.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraConfiguration {

    // the application language
    private String language;
    // the list of search paths
    private List<String> paths;
    // the list of filetypes
    private List<AraraFilePattern> filetypes;
    // the path runtime exception
    private RuntimeException pathRuntimeException;
    // the path IO exception
    private IOException pathIOException;

    /**
     * Getter for the search paths.
     *
     * @return The search paths.
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Getter for the language.
     *
     * @return The language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Setter for the language.
     *
     * @param language The language.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Setter for the search paths.
     *
     * @param rules The search paths.
     * @throws com.github.arara.exception.AraraException if any.
     * @throws com.github.arara.exception.AraraException if any.
     * @throws java.lang.Exception if any.
     */
    public void setPaths(List<String> rules) throws AraraException, Exception {
        this.paths = rules;

        // refresh paths
        refreshPaths();
    }

    /**
     * Getter for the filetypes.
     *
     * @return The filetypes.
     */
    public List<AraraFilePattern> getFiletypes() {
        return filetypes;
    }

    /**
     * Setter for the filetypes.
     *
     * @param filetypes The filetypes.
     */
    public void setFiletypes(List<AraraFilePattern> filetypes) {
        this.filetypes = filetypes;
    }

    /**
     * Constructor.
     */
    public AraraConfiguration() {

        // creates both lists
        paths = new ArrayList<String>();
        filetypes = new ArrayList<AraraFilePattern>();
        pathRuntimeException = null;
    }

    /**
     * Refreshes all paths, replacing possible variables in the template.
     *
     * @throws com.github.arara.exception.AraraException if any.
     * @throws java.lang.Exception if any.
     */
    private void refreshPaths() throws AraraException, Exception {

        // create a map
        HashMap map = new HashMap();

        // add the variables
        map.put("userhome", SystemUtils.USER_HOME);

        // for every path in the list
        for (int i = 0; i < paths.size(); i++) {

            try {

                // remove the keyword
                String fullPath = AraraUtils.removeKeyword(paths.get(i));

                // apply the template
                fullPath = (String) TemplateRuntime.eval(fullPath, map);

                try {

                    // attempt to create a file reference and
                    // get the canonical path, if the operation
                    // succeeds, it's a valid path
                    File validFile = new File(fullPath);
                    validFile.getCanonicalPath();

                    // sanitize the path in order to be displayed correctly
                    fullPath = new File(new File(fullPath).toURI()).getPath();

                    // set the path in the list
                    paths.set(i, fullPath);

                } catch (IOException ioException) {

                    // the path is terribly invalid, get the exception,
                    // if null
                    if (pathIOException == null) {

                        // set the path IO exception
                        pathIOException = ioException;
                    }
                }

            } catch (RuntimeException runtimeException) {

                // an error has occurred, register the exception for
                // later access

                // if there isn't an exception yet
                if (pathRuntimeException == null) {

                    // set the exception
                    pathRuntimeException = runtimeException;
                }

            }
        }
    }

    /**
     * Getter for the path runtime exception.
     *
     * @return The path runtime exception.
     */
    public RuntimeException getPathRuntimeException() {
        return pathRuntimeException;
    }

    /**
     * Getter for the path IO exception.
     *
     * @return The path IO exception.
     */
    public IOException getPathIOException() {
        return pathIOException;
    }
    
}

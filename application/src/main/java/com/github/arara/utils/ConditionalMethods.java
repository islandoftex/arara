package com.github.arara.utils;

// needed imports
import com.github.arara.database.AraraDatabase;
import com.github.arara.exception.AraraException;
import com.google.common.hash.HashCode;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides helper static methods for the conditional context.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class ConditionalMethods {

    // the file basename reference
    /** Constant <code>basenameReference</code> */
    private static String basenameReference;

    /**
     * Returns a logic value indicating if the provided file reference exists.
     *
     * @param filename The filename.
     * @return A boolean indicating if the file reference exists.
     */
    public static boolean exists(String filename) {

        // discover the filename
        filename = discoverName(filename, basenameReference);

        // return the call to an inner method
        return (new File(filename)).exists();
    }

    /**
     * Calculates the file hash.
     *
     * @param filename The filename.
     * @return A string containing the file hash.
     */
    private static String calculateHash(String filename) {

        try {

            // obtain the hash code and return it
            HashCode hashCode = Files.hash(new File(filename), AraraConstants.HASHFUNCTION);
            return hashCode.toString();

        } catch (IOException ioe) {

            // something bad happened,
            // just return null
            return null;
        }
    }

    /**
     * Checks if the provided file reference hash changed, based on a previous
     * hash calculation.
     *
     * @param filename A file reference.
     * @return A boolean indicating if the provided file has changed.
     * @throws com.github.arara.exception.AraraException If something bad
     * happens, an exception is thrown.
     */
    public static boolean changed(String filename) throws AraraException {

        // discover name
        filename = discoverName(filename, basenameReference);

        try {

            // create a map
            HashMap map;

            // we have our XML file
            if (AraraDatabase.exists()) {

                // load it
                map = AraraDatabase.load();

            } else {

                // no XML, simply instantiate
                // the new map
                map = new HashMap();
            }

            // the file is new in the
            // map context
            if (map.get(filename) == null) {

                // if the file exists
                if (exists(filename)) {

                    // update hash and save it
                    map.put(filename, calculateHash(filename));
                    AraraDatabase.save(map);

                    // file is changed
                    return true;

                } else {

                    // file does not exist as well,
                    // hence not changed
                    return false;

                }
            } else {

                // we have previous info
                // on that file

                // if the file exists
                if (exists(filename)) {

                    // compute hash
                    String hash = (String) map.get(filename);
                    String result = calculateHash(filename);

                    // if they match
                    if (hash.equals(result)) {

                        // file hasn't changed
                        return false;

                    } else {

                        // they don't match, update
                        // map and save
                        map.put(filename, result);
                        AraraDatabase.save(map);
                        return true;
                    }

                } else {

                    // file does not exist, remove
                    // reference from map and save
                    // it for future generations
                    map.remove(filename);
                    AraraDatabase.save(map);
                    return true;
                }

            }
        } catch (AraraException araraException) {
            
            // simply forward the exception
            throw araraException;
        }
    }

    /**
     * Discovers name based on some tricky rules.
     *
     * @param value The value to be analyzed.
     * @param prefix The file prefix.
     * @return The new discovered name.
     */
    private static String discoverName(String value, String prefix) {
        
        // if we don't have a dot, we are
        // dealing with extensions
        if (value.lastIndexOf(".") == -1) {
            
            // concat prefix with value
            // and return
            return prefix.concat(".").concat(value);
            
        } else {
            
            // we have a dot
            
            // if the dot is in the end, simply
            // return the value without the last
            // dot
            if (value.endsWith(".")) {
                
                // return it
                return value.substring(0, value.length() - 1);
                
            } else {
                
                // return it as it is
                return value;
            }
        }
    }

    /**
     * Setter for the basename reference.
     *
     * @param basenameReference The basename reference.
     */
    public static void setBasenameReference(String basenameReference) {
        ConditionalMethods.basenameReference = basenameReference;
    }

    /**
     * Check if the regular expression is available in the provided file
     * reference.
     *
     * @param filename The filename reference.
     * @param regex The regular expression.
     * @return A boolean indicating if the regular expression is available in
     * the provided file reference.
     */
    public static boolean contains(String filename, String regex) {

        // discover name
        filename = discoverName(filename, basenameReference);

        // if our file exists
        if (exists(filename)) {
            
            try {
                
                // create reader
                FileReader fileReader = new FileReader(filename);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                
                // some flags
                String currentLine;
                boolean found = false;
                Pattern pattern = Pattern.compile(regex);
                
                // let' iterate through the file lines
                while ((currentLine = bufferedReader.readLine()) != null) {
                    
                    // match the current line
                    Matcher matcher = pattern.matcher(currentLine);
                    
                    // if the pattern is found,
                    // break the loop
                    if (matcher.find()) {
                        found = true;
                        break;
                    }
                }
                
                // close readers
                bufferedReader.close();
                fileReader.close();
                
                // return flag
                return found;
            } catch (FileNotFoundException fnfe) {
                // do nothing
            } catch (IOException ex) {
                // do nothing
            }
            
            // in case of exceptions, this
            // line is reached
            return false;
            
        } else {
            
            // no file, simply
            // return
            return false;
        }
    }
    
}

package com.github.arara.database;

// needed imports.
import com.github.arara.exception.AraraException;
import com.github.arara.utils.AraraConstants;
import com.github.arara.utils.AraraLocalization;
import com.github.arara.utils.AraraUtils;
import com.github.arara.utils.ConditionalMethods;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Encapsulates the XML mapping for changed files. This class basically loads
 * and saves a hashmap containing all data we need from or to a XML file, which
 * is in fact only an excerpt of a real XML file (no header, for example).
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraDatabase {
    
    // the localization class
    /** Constant <code>localization</code> */
    final static AraraLocalization localization = AraraLocalization.getInstance();
    // the absolute path reference
    /** Constant <code>absolutePath</code> */
    private static String absolutePath;

    /**
     * Loads the XML file and maps the content to a hashmap.
     *
     * @return A hashmap containing all data from the XML file, hopefully the
     * filename and its corresponding hash.
     * @throws com.github.arara.exception.AraraException if something bad
     * happens, including a malformed XML or an encoding problem.
     */
    public static HashMap load() throws AraraException {
        
        try {
            
            // get an input from the XML file
            FileInputStream inputStream = new FileInputStream(absolutePath.concat(File.separator).concat(AraraConstants.ARARADATABASE));
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            
            // create the mapping
            XStream xstream = new XStream();
            
            // get the hashmap
            HashMap map = (HashMap) xstream.fromXML(reader);
            
            // close handlers
            reader.close();
            inputStream.close();
            
            // return the map and we are done
            return map;
            
        } catch (FileNotFoundException fnfe) {
            
            // generate exception
            throw new AraraException(localization.getMessage("Error_XMLFileNotFound", AraraConstants.ARARADATABASE));
            
        } catch (UnsupportedEncodingException uee) {
            
            // generate exception
            throw new AraraException(localization.getMessage("Error_XMLBadEncoding", AraraConstants.ARARADATABASE));
            
        } catch (XStreamException xe) {
            
            // generate exception
            throw new AraraException(localization.getMessage("Error_XMLMalformedFile", AraraConstants.ARARADATABASE));
            
        } catch (IOException ioe) {
            
            // generate exception
            throw new AraraException(localization.getMessage("Error_XMLAccess", AraraConstants.ARARADATABASE));
            
        }
    }

    /**
     * Saves the hashmap into a XML file.
     *
     * @param map A hashmap containing all data from files and their
     * corresponding hashes.
     * @throws com.github.arara.exception.AraraException if something bad
     * happens.
     */
    public static void save(HashMap map) throws AraraException {
        
        try {
            
            // get the writers
            FileOutputStream outputStream = new FileOutputStream(absolutePath.concat(File.separator).concat(AraraConstants.ARARADATABASE));
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            
            // create the mapping
            XStream xstream = new XStream();
            
            // write hashmap to the XML file
            xstream.toXML(map, writer);
            
            // close writers
            writer.close();
            outputStream.close();
            
        } catch (UnsupportedEncodingException uee) {
            
            // generate exception
            throw new AraraException(localization.getMessage("Error_XMLBadEncoding", AraraConstants.ARARADATABASE));
            
        } catch (XStreamException xe) {
            
            // generate exception
            throw new AraraException(localization.getMessage("Error_XMLMalformedFile", AraraConstants.ARARADATABASE));
            
        } catch (IOException ioe) {
            
            // generate exception
            throw new AraraException(localization.getMessage("Error_XMLAccess", AraraConstants.ARARADATABASE));
            
        }
    }

    /**
     * Checks if the XML file exists in the current directory.
     *
     * @return a boolean indicating if the XML file exists in the current
     * directory.
     */
    public static boolean exists() {
        
        // return the result from a static method of
        // another helper class
        return ConditionalMethods.exists(absolutePath.concat(File.separator).concat(AraraConstants.ARARADATABASE));
    }
    
    /**
     * Set the absolute path based on the current file processed by arara.
     * 
     * @param file The current file processed by arara.
     */
    public static void setAbsolutePath(File file) {
        
        // set the absolute path reference
        AraraDatabase.absolutePath = AraraUtils.getAbsolutePath(file);
    }
}

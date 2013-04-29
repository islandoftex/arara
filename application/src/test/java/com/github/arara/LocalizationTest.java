package com.github.arara;

// needed imports
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Checks if the localization files are in place in order to Maven correctly
 * build the application.
 */
public class LocalizationTest extends TestCase {

    /**
     * Create the test case.
     *
     * @param testName The name of the test case.
     */
    public LocalizationTest(String testName) {
        super(testName);
    }

    /**
     * Returns the test suite.
     * 
     * @return The suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(LocalizationTest.class);
    }

    /**
     * Checks if the localization resource exists.
     */
    public void testLocalizationFile() {
        
        // at least the default localization
        URL url = Arara.class.getResource("/com/github/arara/localization/Messages.properties");
        
        // nothing was found
        if (url == null) {
            
            // raise error
            fail("arara requires at least the default localization file Messages.properties " +
                 "located at the 'translations/' directory in the project repository. There " +
                 "is a shell script named 'genlanguages' inside the directory, please run it " +
                 "in order to generate the localization files, and then build arara again.");
            
        }
    }
    
}

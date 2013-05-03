package com.github.arara;

// needed imports
import com.github.arara.exception.AraraException;
import com.github.arara.utils.ConditionalEvaluator;
import com.github.arara.utils.ConditionalMethods;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the evaluation class.
 */
public class EvaluationTest extends TestCase {

    /**
     * Constructor.
     * 
     * @param testName Test name.
     */
    public EvaluationTest(String testName) {
        super(testName);
    }

    /**
     * Returns the test suite.
     * 
     * @return The suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(EvaluationTest.class);
    }

    /**
     * Test conditionals.
     * 
     * @throws AraraException An exception is thrown in case of an error.
     */
    public void testConditionals() throws AraraException {

        // list of results
        List<Boolean> results = new ArrayList<Boolean>();
        results.add(ConditionalEvaluator.evaluate(""));
        results.add(ConditionalEvaluator.evaluate("1 == 1"));
        results.add(ConditionalEvaluator.evaluate("1 == 2"));
        results.add(ConditionalEvaluator.evaluate("true"));
        results.add(ConditionalEvaluator.evaluate("false"));

        // expected results
        List<Boolean> expected = Arrays.asList(true, true, false, true, false);
        assertEquals(results, expected);

    }

    /**
     * Test filenames.
     * 
     * @throws IllegalAccessException Throw exception in case of error.
     * @throws IllegalArgumentException Throw exception in case of error.
     * @throws NoSuchMethodException Throw exception in case of error.
     * @throws InvocationTargetException Throw exception in case of error.
     */
    public void testFilenames() throws IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {

        // create access for private method via reflection
        Method method = ConditionalMethods.class.getDeclaredMethod("discoverName", String.class, String.class);
        method.setAccessible(true);

        // create acess for private field via reflection
        Field field = ConditionalMethods.class.getDeclaredField("absolutePath");
        field.setAccessible(true);
        field.set(null, "");
        
        // list of results
        List<String> results = new ArrayList<String>();
        results.add((String) method.invoke(this, "tex", "foo"));
        results.add((String) method.invoke(this, "file.type", "foo"));
        results.add((String) method.invoke(this, "file.", "foo"));
        results.add((String) method.invoke(this, ".gitignore", "foo"));

        // expected results
        List<String> expected = Arrays.asList("/foo.tex", "/file.type", "/file", "/.gitignore");
        assertEquals(results, expected);
    }
}

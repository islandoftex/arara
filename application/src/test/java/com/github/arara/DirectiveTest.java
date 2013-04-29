package com.github.arara;

// needed imports
import com.github.arara.utils.AraraConstants;
import com.github.arara.utils.AraraUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for directives.
 */
public class DirectiveTest extends TestCase {

    /**
     * Costructor.
     * 
     * @param testName Test name.
     */
    public DirectiveTest(String testName) {
        super(testName);
    }

    /**
     * The test suite.
     * 
     * @return The test.
     */
    public static Test suite() {
        return new TestSuite(DirectiveTest.class);
    }

    /**
     * Test empty directives.
     */
    public void testEmptyDirective() {

        // list of results
        List<Boolean> results = new ArrayList<Boolean>();
        results.add(AraraUtils.checkForValidDirective("foo", AraraConstants.EMPTYDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("123", AraraConstants.EMPTYDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo123", AraraConstants.EMPTYDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("123foo", AraraConstants.EMPTYDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo 123", AraraConstants.EMPTYDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("123 foo", AraraConstants.EMPTYDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("FOO", AraraConstants.EMPTYDIRECTIVEPATTERN));

        // expected values
        List<Boolean> expected = Arrays.asList(true, true, true, true, false, false, true);
        assertEquals(results, expected);

    }

    /**
     * Test full directives.
     */
    public void testFullDirective() {

        // list of results
        List<Boolean> results = new ArrayList<Boolean>();
        results.add(AraraUtils.checkForValidDirective("foo: abc", AraraConstants.FULLDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: { abc }", AraraConstants.FULLDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: {}", AraraConstants.FULLDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo : abc", AraraConstants.FULLDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo : { abc }", AraraConstants.FULLDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: {abc}", AraraConstants.FULLDIRECTIVEPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: {{{{{abc}}}}}}}}}}}", AraraConstants.FULLDIRECTIVEPATTERN));

        // expected values
        List<Boolean> expected = Arrays.asList(false, true, true, false, true, true, true);
        assertEquals(results, expected);

    }

    /**
     * Test full directives with conditionals.
     */
    public void testFullDirectiveWithConditional() {

        // list of results
        List<Boolean> results = new ArrayList<Boolean>();
        results.add(AraraUtils.checkForValidDirective("foo: { abc }", AraraConstants.FULLDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: { abc } if", AraraConstants.FULLDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: { abc } if hello", AraraConstants.FULLDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: { abc } if         ", AraraConstants.FULLDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: { abc }}}}}{}{}{}}}}} if hello", AraraConstants.FULLDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo: { abc } if { hello world }", AraraConstants.FULLDIRECTIVECONDITIONALPATTERN));

        // expected values
        List<Boolean> expected = Arrays.asList(false, false, true, false, true, true);
        assertEquals(results, expected);

    }

    /**
     * Test empty directives with conditionals.
     */
    public void testEmptyDirectiveWithConditional() {

        // list of results
        List<Boolean> results = new ArrayList<Boolean>();
        results.add(AraraUtils.checkForValidDirective("foo", AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo if", AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo if hello", AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo if         ", AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo if hello", AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN));
        results.add(AraraUtils.checkForValidDirective("foo if   { hello world }", AraraConstants.EMPTYDIRECTIVECONDITIONALPATTERN));

        // expected values
        List<Boolean> expected = Arrays.asList(false, false, true, false, true, true);
        assertEquals(results, expected);

    }
    
}

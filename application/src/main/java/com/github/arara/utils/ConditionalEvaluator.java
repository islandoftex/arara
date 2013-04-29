package com.github.arara.utils;

// needed imports
import com.github.arara.exception.AraraException;
import java.util.HashMap;
import org.mvel2.templates.TemplateRuntime;

/**
 * Evaluates the conditionals from directives.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class ConditionalEvaluator {

    // the localization class
    /** Constant <code>localization</code> */
    final static AraraLocalization localization = AraraLocalization.getInstance();

    /**
     * Evaluates the conditional.
     *
     * @param conditional The conditional.
     * @return A boolean indicating the conditional resolution.
     * @throws com.github.arara.exception.AraraException An exception is raised
     * in case of error.
     */
    public static boolean evaluate(String conditional) throws AraraException {

        // we don't have anything to evaluate, let's
        // just return true
        if ("".equals(conditional)) {
            return true;
        }

        // create a new map
        HashMap map = new HashMap();

        try {

            // let's put a few methods
            // in order to ease our lives
            map.put("exists", ConditionalMethods.class.getMethod("exists", String.class));
            map.put("changed", ConditionalMethods.class.getMethod("changed", String.class));
            map.put("contains", ConditionalMethods.class.getMethod("contains", String.class, String.class));

        } catch (NoSuchMethodException nsme) {
            // this will never happen
        }

        try {

            // evaluate the conditional
            Object result = TemplateRuntime.eval("@{ ".concat(conditional).concat(" }"), map);

            // we are expecting a boolean value
            if (!(result instanceof Boolean)) {

                // throw exception, no return
                throw new AraraException(localization.getMessage("Error_ConditionalNotBoolean").concat("\n\n").concat(conditional));

            } else {

                // we have a boolean value, so
                // let's return it
                return ((Boolean) result).booleanValue();
                
            }
        } catch (RuntimeException runtimeException) {

            // throw exception, no return
            throw new AraraException(localization.getMessage("Error_ConditionalRuntimeError", AraraUtils.getVariableFromException(runtimeException)));
        }
    }
    
}

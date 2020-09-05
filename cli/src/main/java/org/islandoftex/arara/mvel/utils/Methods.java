// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils;

import org.islandoftex.arara.api.localization.Messages;
import org.islandoftex.arara.core.localization.LanguageController;
import org.islandoftex.arara.core.session.Session;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * Implements some auxiliary methods for runtime evaluation.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@SuppressWarnings("unused")
public class Methods {
    // the language controller
    private static final Messages messages = LanguageController.getMessages();
    // the session controller
    private static final Session session = Session.INSTANCE;

    /**
     * Get rule methods.
     *
     * @return A map of method names to method pointers.
     */
    public static Map<String, Object> getRuleMethods() {
        Map<String, Object> map = new HashMap<>(getConditionalMethods());
        try {
            Method[] methodsKotlin = KtRuleMethods.class.getMethods();
            Arrays.asList("halt", "getOriginalFile", "getOriginalReference",
                    "trimSpaces", "getBasename", "getFiletype", "replicatePattern",
                    "throwError", "getSession", "buildString", "getCommand",
                    "getCommandWithWorkingDirectory", "isVerboseMode",
                    "showMessage", "isOnPath", "unsafelyExecuteSystemCommand",
                    "listFilesByExtensions", "listFilesByPatterns",
                    "writeToFile", "readFromFile", "isSubdirectory",
                    "isEmpty", "isNotEmpty", "isTrue", "isFalse",
                    "isWindows", "isLinux", "isMac", "isUnix", "isCygwin",
                    "checkClass", "isString", "isList", "isMap", "isBoolean").forEach(name ->
                    map.put(name, Stream.of(methodsKotlin).filter(
                            m -> m.getName().equals(name)).findFirst().get()));
        } catch (Exception exception) {
            // quack, quack, quack
        }
        return map;
    }

    /**
     * Get conditional methods.
     *
     * @return A map of method names to method pointers.
     */
    public static Map<String, Object> getConditionalMethods() {
        Map<String, Object> map = new HashMap<>();
        try {
            Method[] methodsKotlin = KtConditionalMethods.class.getMethods();
            Arrays.asList("exists", "missing", "changed", "unchanged",
                    "found", "toFile", "showDropdown", "showInput",
                    "showOptions", "currentFile", "loadClass", "loadObject").forEach(name ->
                    map.put(name, Stream.of(methodsKotlin).filter(
                            m -> m.getName().equals(name)).findFirst().get()));
        } catch (Exception exception) {
            // quack, quack, quack
        }
        return map;
    }
}

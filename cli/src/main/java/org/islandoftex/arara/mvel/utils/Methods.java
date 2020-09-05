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
            Method[] methods = Methods.class.getMethods();
            Method[] methodsKotlin = KtRuleMethods.class.getMethods();
            Arrays.asList("checkClass", "isString", "isList", "isMap", "isBoolean").forEach(name ->
                    map.put(name, Stream.of(methods).filter(
                            m -> m.getName().equals(name)).findFirst().get()));
            Arrays.asList("halt", "getOriginalFile", "getOriginalReference",
                    "trimSpaces", "getBasename", "getFiletype", "replicatePattern",
                    "throwError", "getSession", "buildString", "getCommand",
                    "getCommandWithWorkingDirectory", "isVerboseMode",
                    "showMessage", "isOnPath", "unsafelyExecuteSystemCommand",
                    "listFilesByExtensions", "listFilesByPatterns",
                    "writeToFile", "readFromFile", "isSubdirectory",
                    "isEmpty", "isNotEmpty", "isTrue", "isFalse",
                    "isWindows", "isLinux", "isMac", "isUnix", "isCygwin").forEach(name ->
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

    /**
     * Checks if the object is an instance of the provided class.
     *
     * @param clazz  The class.
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean checkClass(Class<?> clazz, Object object) {
        return clazz.isInstance(object);
    }

    /**
     * Checks if the object is a string.
     *
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isString(Object object) {
        return checkClass(String.class, object);
    }

    /**
     * Checks if the object is a list.
     *
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isList(Object object) {
        return checkClass(List.class, object);
    }

    /**
     * Checks if the object is a map.
     *
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isMap(Object object) {
        return checkClass(Map.class, object);
    }

    /**
     * Checks if the object is a boolean.
     *
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isBoolean(Object object) {
        return checkClass(Boolean.class, object);
    }
}

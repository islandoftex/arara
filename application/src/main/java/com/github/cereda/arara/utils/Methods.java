/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda 
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.utils;

import com.github.cereda.arara.controller.ConfigurationController;
import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.model.AraraException;
import com.github.cereda.arara.model.Command;
import com.github.cereda.arara.model.Messages;
import com.github.cereda.arara.model.Session;
import com.github.cereda.arara.model.Trigger;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implements some auxiliary methods for runtime evaluation.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Methods {

    // the language controller
    private static final LanguageController messages =
            LanguageController.getInstance();
    
    // the session controller
    private static final Session session = new Session();

    /**
     * Adds the rule methods to the provided map.
     * @param map The map.
     */
    public static void addRuleMethods(Map<String, Object> map) {
        addConditionalMethods(map);
        try {
            map.put("getOriginalFile", Methods.class.getMethod("getOriginalFile"));
            map.put("getOriginalReference", Methods.class.getMethod("getOriginalReference"));
            map.put("isEmpty", Methods.class.getMethod("isEmpty", String.class));
            map.put("isNotEmpty", Methods.class.getMethod("isNotEmpty", String.class));
            map.put("isEmpty", Methods.class.getMethod("isEmpty", String.class, Object.class));
            map.put("isNotEmpty", Methods.class.getMethod("isNotEmpty", String.class, Object.class));
            map.put("isEmpty", Methods.class.getMethod("isEmpty", String.class, Object.class, Object.class));
            map.put("isNotEmpty", Methods.class.getMethod("isNotEmpty", String.class, Object.class, Object.class));
            map.put("isTrue", Methods.class.getMethod("isTrue", String.class));
            map.put("isFalse", Methods.class.getMethod("isFalse", String.class));
            map.put("isTrue", Methods.class.getMethod("isTrue", String.class, Object.class));
            map.put("isFalse", Methods.class.getMethod("isFalse", String.class, Object.class));
            map.put("isTrue", Methods.class.getMethod("isTrue", String.class, Object.class, Object.class));
            map.put("isFalse", Methods.class.getMethod("isFalse", String.class, Object.class, Object.class));
            map.put("isTrue", Methods.class.getMethod("isTrue", String.class, Object.class, Object.class, Object.class));
            map.put("isFalse", Methods.class.getMethod("isFalse", String.class, Object.class, Object.class, Object.class));
            map.put("trimSpaces", Methods.class.getMethod("trimSpaces", String.class));
            map.put("isTrue", Methods.class.getMethod("isTrue", boolean.class, Object.class));
            map.put("isFalse", Methods.class.getMethod("isFalse", boolean.class, Object.class));
            map.put("isTrue", Methods.class.getMethod("isTrue", boolean.class, Object.class, Object.class));
            map.put("isFalse", Methods.class.getMethod("isFalse", boolean.class, Object.class, Object.class));
            map.put("getBasename", Methods.class.getMethod("getBasename", File.class));
            map.put("getBasename", Methods.class.getMethod("getBasename", String.class));
            map.put("getFiletype", Methods.class.getMethod("getFiletype", File.class));
            map.put("getFiletype", Methods.class.getMethod("getFiletype", String.class));
            map.put("throwError", Methods.class.getMethod("throwError", String.class));
            map.put("getSession", Methods.class.getMethod("getSession"));
            map.put("isWindows", Methods.class.getMethod("isWindows"));
            map.put("isLinux", Methods.class.getMethod("isLinux"));
            map.put("isMac", Methods.class.getMethod("isMac"));
            map.put("isUnix", Methods.class.getMethod("isUnix"));
            map.put("isAIX", Methods.class.getMethod("isAIX"));
            map.put("isIrix", Methods.class.getMethod("isIrix"));
            map.put("isOS2", Methods.class.getMethod("isOS2"));
            map.put("isSolaris", Methods.class.getMethod("isSolaris"));
            map.put("isCygwin", Methods.class.getMethod("isCygwin"));
            map.put("isWindows", Methods.class.getMethod("isWindows", Object.class, Object.class));
            map.put("isLinux", Methods.class.getMethod("isLinux", Object.class, Object.class));
            map.put("isMac", Methods.class.getMethod("isMac", Object.class, Object.class));
            map.put("isUnix", Methods.class.getMethod("isUnix", Object.class, Object.class));
            map.put("isAIX", Methods.class.getMethod("isAIX", Object.class, Object.class));
            map.put("isIrix", Methods.class.getMethod("isIrix", Object.class, Object.class));
            map.put("isOS2", Methods.class.getMethod("isOS2", Object.class, Object.class));
            map.put("isSolaris", Methods.class.getMethod("isSolaris", Object.class, Object.class));
            map.put("isCygwin", Methods.class.getMethod("isCygwin", Object.class, Object.class));
            map.put("replicatePattern", Methods.class.getMethod("replicatePattern", String.class, List.class));
            map.put("buildString", Methods.class.getMethod("buildString", Object[].class));
            map.put("addQuotes", Methods.class.getMethod("addQuotes", Object.class));
            map.put("getCommand", Methods.class.getMethod("getCommand", List.class));
            map.put("getCommand", Methods.class.getMethod("getCommand", Object[].class));
            map.put("getTrigger", Methods.class.getMethod("getTrigger", String.class));
            map.put("getTrigger", Methods.class.getMethod("getTrigger", String.class, Object[].class));
            map.put("checkClass", Methods.class.getMethod("checkClass", Class.class, Object.class));
            map.put("isString", Methods.class.getMethod("isString", Object.class));
            map.put("isList", Methods.class.getMethod("isList", Object.class));
            map.put("isMap", Methods.class.getMethod("isMap", Object.class));
            map.put("isBoolean", Methods.class.getMethod("isBoolean", Object.class));
            map.put("isVerboseMode", Methods.class.getMethod("isVerboseMode"));
            map.put("showMessage", Methods.class.getMethod("showMessage", int.class, String.class, String.class));
            map.put("showMessage", Methods.class.getMethod("showMessage", int.class, int.class, String.class, String.class));
            map.put("isOnPath", Methods.class.getMethod("isOnPath", String.class));
        } catch (Exception exception) {
            // quack, quack, quack
        }
    }

    /**
     * Adds conditional methods to the provided map.
     * @param map The map.
     */
    public static void addConditionalMethods(Map<String, Object> map) {
        try {
            map.put("exists", Methods.class.getMethod("exists", String.class));
            map.put("exists", Methods.class.getMethod("exists", File.class));
            map.put("missing", Methods.class.getMethod("missing", String.class));
            map.put("missing", Methods.class.getMethod("missing", File.class));
            map.put("changed", Methods.class.getMethod("changed", String.class));
            map.put("changed", Methods.class.getMethod("changed", File.class));
            map.put("unchanged", Methods.class.getMethod("unchanged", String.class));
            map.put("unchanged", Methods.class.getMethod("unchanged", File.class));
            map.put("found", Methods.class.getMethod("found", String.class, String.class));
            map.put("found", Methods.class.getMethod("found", File.class, String.class));
            map.put("toFile", Methods.class.getMethod("toFile", String.class));
            map.put("showDropdown", Methods.class.getMethod("showDropdown", int.class, String.class, String.class, Object[].class));
            map.put("showDropdown", Methods.class.getMethod("showDropdown", int.class, int.class, String.class, String.class, Object[].class));
            map.put("showInput", Methods.class.getMethod("showInput", int.class, String.class, String.class));
            map.put("showInput", Methods.class.getMethod("showInput", int.class, int.class, String.class, String.class));
            map.put("showOptions", Methods.class.getMethod("showOptions", int.class, String.class, String.class, Object[].class));
            map.put("showOptions", Methods.class.getMethod("showOptions", int.class, int.class, String.class, String.class, Object[].class));
        } catch (Exception exception) {
            // quack, quack, quack
        }
    }

    /**
     * Gets the original file.
     * @return The original file.
     */
    public static String getOriginalFile() {
        File file = (File) ConfigurationController.getInstance().get("execution.reference");
        return file.getName();
    }

    /**
     * Gets the original reference.
     * @return The original reference.
     */
    public static File getOriginalReference() {
        return (File) ConfigurationController.getInstance().get("execution.reference");
    }

    /**
     * Checks if the string is empty.
     * @param string The string.
     * @return A boolean value.
     */
    public static boolean isEmpty(String string) {
        return CommonUtils.checkEmptyString(string);
    }

    /**
     * Checks if the string is not empty.
     * @param string The string.
     * @return A boolean value.
     */
    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    /**
     * Checks if the string is empty.
     * @param string The string.
     * @param yes Object to return if true.
     * @return An object or empty string.
     */
    public static Object isEmpty(String string, Object yes) {
        return isEmpty(string) ? yes : "";
    }

    /**
     * Checks if the string is not empty.
     * @param string The string.
     * @param yes Object to return if true.
     * @return An object or empty string.
     */
    public static Object isNotEmpty(String string, Object yes) {
        return isNotEmpty(string) ? yes : "";
    }

    /**
     * Checks if the string is empty.
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    public static Object isEmpty(String string, Object yes, Object no) {
        return isEmpty(string) ? yes : no;
    }

    /**
     * Checks if the string is not empty.
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    public static Object isNotEmpty(String string, Object yes, Object no) {
        return isNotEmpty(string) ? yes : no;
    }

    /**
     * Checks if the string holds a true value.
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isTrue(String string) throws AraraException {
        return isEmpty(string) ? false : CommonUtils.checkBoolean(string);
    }

    /**
     * Checks if the string holds a false value.
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isFalse(String string) throws AraraException {
        return isEmpty(string) ? false : !CommonUtils.checkBoolean(string);
    }

    /**
     * Checks if the string holds a true value.
     * @param string The string.
     * @param yes Object to return if true.
     * @return An object or an empty string.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isTrue(String string, Object yes)
            throws AraraException {
        return isTrue(string) ? yes : "";
    }

    /**
     * Checks if the string holds a false value.
     * @param string The string.
     * @param yes Object to return if true.
     * @return An object or an empty string.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isFalse(String string, Object yes)
            throws AraraException {
        return (isFalse(string) ? yes : "");
    }

    /**
     * Checks if the string holds a true value.
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isTrue(String string, Object yes, Object no)
            throws AraraException {
        return (isTrue(string) ? yes : no);
    }

    /**
     * Checks if the string holds a false value.
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isFalse(String string, Object yes, Object no)
            throws AraraException {
        return (isFalse(string) ? yes : no);
    }

    /**
     * Checks if the string holds a true value.
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isTrue(String string, Object yes, Object no,
            Object fallback) throws AraraException {
        return isEmpty(string) ? fallback : (isTrue(string) ? yes : no);
    }

    /**
     * Checks if the string holds a false value.
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isFalse(String string, Object yes, Object no,
            Object fallback) throws AraraException {
        return isEmpty(string) ? fallback : (isFalse(string) ? yes : no);
    }

    /**
     * Trim spaces from the string.
     * @param string The string.
     * @return A trimmed string.
     */
    public static String trimSpaces(String string) {
        return string.trim();
    }

    /**
     * Checks if the expression resolves to true.
     * @param value The expression.
     * @param yes Object to return if true.
     * @return An object or an empty string.
     */
    public static Object isTrue(boolean value, Object yes) {
        return value ? yes : "";
    }

    /**
     * Checks if the expression resolves to false.
     * @param value The expression.
     * @param yes Object to return if true.
     * @return An object or an empty string.
     */
    public static Object isFalse(boolean value, Object yes) {
        return !value ? yes : "";
    }

    /**
     * Checks if the expression resolves to true.
     * @param value The expression.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    public static Object isTrue(boolean value, Object yes, Object no) {
        return value ? yes : no;
    }

    /**
     * Checks if the expression resolves to false.
     * @param value The expression.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    public static Object isFalse(boolean value, Object yes, Object no) {
        return !value ? yes : no;
    }

    /**
     * Gets the basename.
     * @param file The file.
     * @return The basename of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static String getBasename(File file) throws AraraException {
        if (file.isFile()) {
            return CommonUtils.getBasename(file);
        } else {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_BASENAME_NOT_A_FILE,
                                    file.getName()
                            )
                    )
            );
        }
    }

    /**
     * Gets the basename.
     * @param filename The string.
     * @return The basename.
     */
    public static String getBasename(String filename) {
        return CommonUtils.getBasename(filename);
    }

    /**
     * Gets the file type.
     * @param file The provided file.
     * @return The file type.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels. 
     */
    public static String getFiletype(File file) throws AraraException {
        if (file.isFile()) {
            return CommonUtils.getFiletype(file);
        } else {
            throw new AraraException(
                    CommonUtils.getRuleErrorHeader().concat(
                            messages.getMessage(
                                    Messages.ERROR_FILETYPE_NOT_A_FILE,
                                    file.getName()
                            )
                    )
            );
        }
    }

    /**
     * Gets the file type.
     * @param filename The provided string.
     * @return The file type.
     */
    public static String getFiletype(String filename) {
        return CommonUtils.getFiletype(filename);
    }

    /**
     * Replicates the pattern to each element of a list.
     * @param pattern The pattern.
     * @param values The list.
     * @return A list of strings containing the pattern applied to the list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static List<Object> replicatePattern(String pattern,
            List<Object> values) throws AraraException {
        return CommonUtils.replicateList(pattern, values);
    }

    /**
     * Throws an exception.
     * @param text The text to be thrown as the exception message.
     * @throws AraraException The exception to be thrown by this method.
     */
    public static void throwError(String text) throws AraraException {
        throw new AraraException(text);
    }

    /**
     * Gets the session.
     * @return The session.
     */
    public static Session getSession() {
        return session;
    }

    /**
     * Checks if Windows is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isWindows() throws AraraException {
        return CommonUtils.checkOS("windows");
    }
    
    /**
     * Checks if we are inside a Cygwin environment.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isCygwin() throws AraraException {
        return CommonUtils.checkOS("cygwin");
    }

    /**
     * Checks if Linux is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isLinux() throws AraraException {
        return CommonUtils.checkOS("linux");
    }

    /**
     * Checks if Mac is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isMac() throws AraraException {
        return CommonUtils.checkOS("mac");
    }

    /**
     * Checks if Unix is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isUnix() throws AraraException {
        return CommonUtils.checkOS("unix");
    }

    /**
     * Checks if AIX is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isAIX() throws AraraException {
        return CommonUtils.checkOS("aix");
    }

    /**
     * Checks if Irix is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isIrix() throws AraraException {
        return CommonUtils.checkOS("irix");
    }

    /**
     * Checks if OS2 is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isOS2() throws AraraException {
        return CommonUtils.checkOS("os2");
    }

    /**
     * Checks if Solaris is the underlying operating system.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean isSolaris() throws AraraException {
        return CommonUtils.checkOS("solaris");
    }

    /**
     * Checks if Windows is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isWindows(Object yes, Object no)
            throws AraraException {
        return CommonUtils.checkOS("windows") ? yes : no;
    }
    
    /**
     * Checks if we are inside a Cygwin environment.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isCygwin(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("cygwin") ? yes : no;
    }

    /**
     * Checks if Linux is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isLinux(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("linux") ? yes : no;
    }

    /**
     * Checks if Mac is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isMac(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("mac") ? yes : no;
    }

    /**
     * Checks if Unix is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isUnix(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("unix") ? yes : no;
    }

    /**
     * Checks if AIX is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isAIX(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("aix") ? yes : no;
    }

    /**
     * Checks if Irix is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isIrix(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("irix") ? yes : no;
    }

    /**
     * Checks if OS2 is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isOS2(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("os2") ? yes : no;
    }

    /**
     * Checks if Solaris is the underlying operating system.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Object isSolaris(Object yes, Object no) throws AraraException {
        return CommonUtils.checkOS("solaris") ? yes : no;
    }

    /**
     * Checks if the file exists according to its extension.
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean exists(String extension) throws AraraException {
        return CommonUtils.exists(extension);
    }

    /**
     * Checks if the file is missing according to its extension.
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean missing(String extension) throws AraraException {
        return !exists(extension);
    }

    /**
     * Checks if the file has changed, according to its extension.
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean changed(String extension) throws AraraException {
        return CommonUtils.hasChanged(extension);
    }

    /**
     * Checks if the file is unchanged according to its extension.
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean unchanged(String extension) throws AraraException {
        return !changed(extension);
    }

    /**
     * Checks if the file exists.
     * @param filename The file.
     * @return A boolean value.
     */
    public static boolean exists(File filename) {
        return CommonUtils.exists(filename);
    }

    /**
     * Checks if the file is missing.
     * @param filename The file.
     * @return A boolean value.
     */
    public static boolean missing(File filename) {
        return !exists(filename);
    }

    /**
     * Checks if the file has changed.
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean changed(File filename) throws AraraException {
        return CommonUtils.hasChanged(filename);
    }

    /**
     * Checks if the file is unchanged.
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean unchanged(File filename) throws AraraException {
        return !changed(filename);
    }

    /**
     * Build a string based on an array of objects.
     * @param objects Array of objects.
     * @return A string built from the array.
     */
    public static String buildString(Object... objects) {
        return CommonUtils.generateString(objects);
    }

    /**
     * Encloses the provided object into double quotes.
     * @param object The object.
     * @return The object enclosed in double quotes.
     */
    public static String addQuotes(Object object) {
        return CommonUtils.addQuotes(object);
    }

    /**
     * Checks if the file contains the regex, based on its extension.
     * @param extension The extension.
     * @param regex The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean found(String extension, String regex)
            throws AraraException {
        return CommonUtils.checkRegex(extension, regex);
    }

    /**
     * Checks if the file contains the provided regex.
     * @param file The file.
     * @param regex The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean found(File file, String regex) 
            throws AraraException {
        return CommonUtils.checkRegex(file, regex);
    }

    /**
     * Gets the command based on a list of strings.
     * @param elements The list of strings.
     * @return A command.
     */
    public static Command getCommand(List<String> elements) {
        return new Command(elements);
    }

    /**
     * Gets the command based on an array of objects.
     * @param elements Array of objects.
     * @return A command.
     */
    public static Command getCommand(Object... elements) {
        return new Command(elements);
    }

    /**
     * Gets the trigger.
     * @param action The action name.
     * @return The trigger.
     */
    public static Trigger getTrigger(String action) {
        return new Trigger(action, null);
    }

    /**
     * Gets the trigger.
     * @param action The action name.
     * @param parameters The trigger parameters.
     * @return A trigger.
     */
    public static Trigger getTrigger(String action, Object... parameters) {
        return new Trigger(action, Arrays.asList(parameters));
    }

    /**
     * Checks if the object is an intance of the provided class.
     * @param clazz The class.
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean checkClass(Class clazz, Object object) {
        return CommonUtils.checkClass(clazz, object);
    }

    /**
     * Checks if the object is a string.
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isString(Object object) {
        return checkClass(String.class, object);
    }

    /**
     * Checks if the object is a list.
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isList(Object object) {
        return checkClass(List.class, object);
    }

    /**
     * Checks if the object is a map.
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isMap(Object object) {
        return checkClass(Map.class, object);
    }

    /**
     * Checks if the object is a boolean.
     * @param object The object.
     * @return A boolean value.
     */
    public static boolean isBoolean(Object object) {
        return checkClass(Boolean.class, object);
    }
    
    /**
     * Checks if the execution is in verbose mode.
     * @return A boolean value indicating if the execution is in verbose mode.
     */
    public static boolean isVerboseMode() {
        return (Boolean) ConfigurationController.
                getInstance().get("execution.verbose");
    }
    
    /**
     * Returns a file object based on the provided name.
     * @param name The file name.
     * @return A file object.
     */
    public static File toFile(String name) {
        return new File(name);
    }
    
    /**
     * Shows the message.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    public static void showMessage(int width, int type,
            String title, String text) {
        MessageUtils.showMessage(width, type, title, text);
    }
    
    /**
     * Shows the message. It relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    public static void showMessage(int type, String title, String text) {
        MessageUtils.showMessage(type, title, text);
    }
    
    /**
     * Shows a message with options presented as an array of buttons.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    public static int showOptions(int width, int type, String title,
            String text, Object... buttons) {
        return MessageUtils.showOptions(width, type, title, text, buttons);
    }
    
    /**
     * Shows a message with options presented as an array of buttons. It relies
     * on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return  The index of the selected button, starting from 1.
     */
    public static int showOptions(int type, String title,
            String text, Object... buttons) {
        return MessageUtils.showOptions(type, title, text, buttons);
    }
    
    /**
     * Shows a message with a text input.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    public static String showInput(int width, int type,
            String title, String text) {
        return MessageUtils.showInput(width, type, title, text);
    }
    
    /**
     * Shows a message with a text input. It relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    public static String showInput(int type, String title, String text) {
        return MessageUtils.showInput(type, title, text);
    }
    
    /**
     * Shows a message with options presented as a dropdown list of elements.
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    public static int showDropdown(int width, int type, String title,
            String text, Object... elements) {
        return MessageUtils.showDropdown(width, type, title, text, elements);
    }
    
    /**
     * Shows a message with options presented as a dropdown list of elements. It
     * relies on the default width.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    public static int showDropdown(int type, String title,
            String text, Object... elements) {
        return MessageUtils.showDropdown(type, title, text, elements);
    }
    
    /**
     * Checks if the provided command name is reachable from the system path.
     * @param command A string representing the command.
     * @return A logic value.
     */
    public static boolean isOnPath(String command) {
        return CommonUtils.isOnPath(command);
    }

}

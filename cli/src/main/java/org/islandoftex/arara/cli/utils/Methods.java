// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils;

import kotlin.Pair;
import org.islandoftex.arara.Arara;
import org.islandoftex.arara.api.AraraException;
import org.islandoftex.arara.cli.configuration.AraraSpec;
import org.islandoftex.arara.cli.filehandling.FileHandlingUtils;
import org.islandoftex.arara.cli.filehandling.FileSearchingUtils;
import org.islandoftex.arara.cli.localization.LanguageController;
import org.islandoftex.arara.cli.localization.Messages;
import org.islandoftex.arara.core.session.Session;
import org.islandoftex.arara.cli.ruleset.CommandImpl;
import org.islandoftex.arara.api.session.Command;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final LanguageController messages =
            LanguageController.INSTANCE;

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
            Arrays.asList("getOriginalFile", "getOriginalReference", "isEmpty",
                    "isNotEmpty", "isTrue", "isFalse", "trimSpaces",
                    "getBasename", "getFiletype", "throwError", "getSession",
                    "isWindows", "isLinux", "isMac", "isUnix", "isCygwin",
                    "replicatePattern", "buildString", "getCommand",
                    "checkClass", "isString", "isList", "isMap", "isBoolean",
                    "isVerboseMode", "showMessage", "isOnPath",
                    "unsafelyExecuteSystemCommand",
                    "getCommandWithWorkingDirectory", "listFilesByExtensions",
                    "listFilesByPatterns", "writeToFile", "readFromFile",
                    "isSubdirectory", "halt").forEach(name ->
                    map.put(name, Stream.of(methods).filter(
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
            Method[] methods = Methods.class.getMethods();
            Arrays.asList("exists", "missing", "changed", "unchanged", "found",
                    "toFile", "showDropdown", "showInput", "showOptions",
                    "currentFile", "loadClass", "loadObject").forEach(name ->
                    map.put(name, Stream.of(methods).filter(
                            m -> m.getName().equals(name)).findFirst().get()));
        } catch (Exception exception) {
            // quack, quack, quack
        }
        return map;
    }

    /**
     * Exit the application normally
     */
    public static void halt() {
        halt(0);
    }

    /**
     * Exit the application with status code.
     *
     * @param status The exit value
     */
    public static void halt(int status) {
        session.put("arara:" + getOriginalFile() + ":halt", status);
    }

    /**
     * Gets the original file.
     *
     * @return The original file.
     */
    public static String getOriginalFile() {
        File file = Arara.INSTANCE.getConfig()
                .get(AraraSpec.Execution.INSTANCE.getReference());
        return file.getName();
    }

    /**
     * Gets the original reference.
     *
     * @return The original reference.
     */
    public static File getOriginalReference() {
        return Arara.INSTANCE.getConfig()
                .get(AraraSpec.Execution.INSTANCE.getReference());
    }

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    public static boolean isEmpty(String string) {
        return string.equals("");
    }

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or empty string.
     */
    public static Object isEmpty(String string, Object yes) {
        return isEmpty(string) ? yes : "";
    }

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or empty string.
     */
    public static Object isNotEmpty(String string, Object yes) {
        return isNotEmpty(string) ? yes : "";
    }

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     */
    public static Object isEmpty(String string, Object yes, Object no) {
        return isEmpty(string) ? yes : no;
    }

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     */
    public static Object isNotEmpty(String string, Object yes, Object no) {
        return isNotEmpty(string) ? yes : no;
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean isTrue(String string) throws AraraException {
        return !isEmpty(string) && CommonUtils.INSTANCE.checkBoolean(string);
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean isFalse(String string) throws AraraException {
        return !isEmpty(string) && !CommonUtils.INSTANCE.checkBoolean(string);
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or an empty string.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isTrue(String string, Object yes)
            throws AraraException {
        return isTrue(string) ? yes : "";
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or an empty string.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isFalse(String string, Object yes)
            throws AraraException {
        return (isFalse(string) ? yes : "");
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isTrue(String string, Object yes, Object no)
            throws AraraException {
        return (isTrue(string) ? yes : no);
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isFalse(String string, Object yes, Object no)
            throws AraraException {
        return (isFalse(string) ? yes : no);
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string   The string.
     * @param yes      Object to return if true.
     * @param no       Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isTrue(String string, Object yes, Object no,
                                Object fallback) throws AraraException {
        return isEmpty(string) ? fallback : (isTrue(string) ? yes : no);
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string   The string.
     * @param yes      Object to return if true.
     * @param no       Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isFalse(String string, Object yes, Object no,
                                 Object fallback) throws AraraException {
        return isEmpty(string) ? fallback : (isFalse(string) ? yes : no);
    }

    /**
     * Trim spaces from the string.
     *
     * @param string The string.
     * @return A trimmed string.
     */
    public static String trimSpaces(String string) {
        return string.trim();
    }

    /**
     * Checks if the expression resolves to true.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @return An object or an empty string.
     */
    public static Object isTrue(boolean value, Object yes) {
        return value ? yes : "";
    }

    /**
     * Checks if the expression resolves to false.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @return An object or an empty string.
     */
    public static Object isFalse(boolean value, Object yes) {
        return !value ? yes : "";
    }

    /**
     * Checks if the expression resolves to true.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @param no    Object to return if false.
     * @return One of the two objects.
     */
    public static Object isTrue(boolean value, Object yes, Object no) {
        return value ? yes : no;
    }

    /**
     * Checks if the expression resolves to false.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @param no    Object to return if false.
     * @return One of the two objects.
     */
    public static Object isFalse(boolean value, Object yes, Object no) {
        return !value ? yes : no;
    }

    /**
     * Gets the basename.
     *
     * @param file The file.
     * @return The basename of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static String getBasename(File file) throws AraraException {
        if (file.isFile()) {
            return FileHandlingUtils.INSTANCE.getBasename(file);
        } else {
            throw new AraraException(
                    CommonUtils.INSTANCE.getRuleErrorHeader().concat(
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
     *
     * @param filename The string.
     * @return The basename.
     */
    public static String getBasename(String filename) {
        return FileHandlingUtils.INSTANCE.getBasename(new File(filename));
    }

    /**
     * Gets the file type.
     *
     * @param file The provided file.
     * @return The file type.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static String getFiletype(File file) throws AraraException {
        if (file.isFile()) {
            return FileHandlingUtils.INSTANCE.getFileExtension(file);
        } else {
            throw new AraraException(
                    CommonUtils.INSTANCE.getRuleErrorHeader().concat(
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
     *
     * @param filename The provided string.
     * @return The file type.
     */
    public static String getFiletype(String filename) {
        return FileHandlingUtils.INSTANCE.getFileExtension(new File(filename));
    }

    /**
     * Replicates the pattern to each element of a list.
     *
     * @param pattern The pattern.
     * @param values  The list.
     * @return A list of strings containing the pattern applied to the list.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static List<Object> replicatePattern(String pattern,
                                                List<Object> values)
            throws AraraException {
        return CommonUtils.INSTANCE.replicateList(pattern, values);
    }

    /**
     * Throws an exception.
     *
     * @param text The text to be thrown as the exception message.
     * @throws AraraException The exception to be thrown by this method.
     */
    public static void throwError(String text) throws AraraException {
        throw new AraraException(text);
    }

    /**
     * Gets the session.
     *
     * @return The session.
     */
    public static Session getSession() {
        return session;
    }

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean isWindows() throws AraraException {
        return CommonUtils.INSTANCE.checkOS("windows");
    }

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean isCygwin() throws AraraException {
        return CommonUtils.INSTANCE.checkOS("cygwin");
    }

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean isLinux() throws AraraException {
        return CommonUtils.INSTANCE.checkOS("linux");
    }

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean isMac() throws AraraException {
        return CommonUtils.INSTANCE.checkOS("mac");
    }

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean isUnix() throws AraraException {
        return CommonUtils.INSTANCE.checkOS("unix");
    }

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isWindows(Object yes, Object no)
            throws AraraException {
        return CommonUtils.INSTANCE.checkOS("windows") ? yes : no;
    }

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isCygwin(Object yes, Object no) throws AraraException {
        return CommonUtils.INSTANCE.checkOS("cygwin") ? yes : no;
    }

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isLinux(Object yes, Object no) throws AraraException {
        return CommonUtils.INSTANCE.checkOS("linux") ? yes : no;
    }

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isMac(Object yes, Object no) throws AraraException {
        return CommonUtils.INSTANCE.checkOS("mac") ? yes : no;
    }

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static Object isUnix(Object yes, Object no) throws AraraException {
        return CommonUtils.INSTANCE.checkOS("unix") ? yes : no;
    }

    /**
     * Checks if the file exists according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean exists(String extension) throws AraraException {
        return FileHandlingUtils.INSTANCE.exists(extension);
    }

    /**
     * Checks if the file is missing according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean missing(String extension) throws AraraException {
        return !exists(extension);
    }

    /**
     * Checks if the file has changed, according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean changed(String extension) throws AraraException {
        return FileHandlingUtils.INSTANCE.hasChanged(extension);
    }

    /**
     * Checks if the file is unchanged according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean unchanged(String extension) throws AraraException {
        return !changed(extension);
    }

    /**
     * Checks if the file exists.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    public static boolean exists(File filename) {
        return filename.exists();
    }

    /**
     * Checks if the file is missing.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    public static boolean missing(File filename) {
        return !exists(filename);
    }

    /**
     * Checks if the file has changed.
     *
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean changed(File filename) throws AraraException {
        return FileHandlingUtils.INSTANCE.hasChanged(filename);
    }

    /**
     * Checks if the file is unchanged.
     *
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean unchanged(File filename) throws AraraException {
        return !changed(filename);
    }

    /**
     * Build a string based on an array of objects.
     *
     * @param objects Array of objects.
     * @return A string built from the array.
     */
    public static String buildString(Object... objects) {
        return CommonUtils.INSTANCE.generateString(objects);
    }

    /**
     * Checks if the file contains the regex, based on its extension.
     *
     * @param extension The extension.
     * @param regex     The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean found(String extension, String regex)
            throws AraraException {
        return CommonUtils.INSTANCE.checkRegex(extension, regex);
    }

    /**
     * Checks if the file contains the provided regex.
     *
     * @param file  The file.
     * @param regex The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     *                        higher levels.
     */
    public static boolean found(File file, String regex)
            throws AraraException {
        return CommonUtils.INSTANCE.checkRegex(file, regex);
    }

    /**
     * Gets the command based on a list of strings.
     *
     * @param elements The list of strings.
     * @return A command.
     */
    public static Command getCommand(List<String> elements) {
        return new CommandImpl(elements);
    }

    /**
     * Gets the command based on an array of objects.
     *
     * @param elements Array of objects.
     * @return A command.
     */
    public static Command getCommand(Object... elements) {
        return new CommandImpl(elements);
    }

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as string.
     *
     * @param path     String path representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    public static Command getCommandWithWorkingDirectory(String path,
                                                         Object... elements) {
        CommandImpl command = new CommandImpl(elements);
        command.setWorkingDirectory(Paths.get(path));
        return command;
    }

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as file.
     *
     * @param file     File representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    public static Command getCommandWithWorkingDirectory(File file,
                                                         Object... elements) {
        CommandImpl command = new CommandImpl(elements);
        command.setWorkingDirectory(file.toPath());
        return command;
    }

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as string.
     *
     * @param path     String path representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    public static Command getCommandWithWorkingDirectory(String path,
                                                         List<String> elements) {
        CommandImpl command = new CommandImpl(elements);
        command.setWorkingDirectory(Paths.get(path));
        return command;
    }

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as file.
     *
     * @param file     File representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    public static Command getCommandWithWorkingDirectory(File file,
                                                         List<String> elements) {
        CommandImpl command = new CommandImpl(elements);
        command.setWorkingDirectory(file.toPath());
        return command;
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

    /**
     * Checks if the execution is in verbose mode.
     *
     * @return A boolean value indicating if the execution is in verbose mode.
     */
    public static boolean isVerboseMode() {
        return Arara.INSTANCE.getConfig()
                .get(AraraSpec.Execution.INSTANCE.getVerbose());
    }

    /**
     * Returns a file object based on the provided name.
     *
     * @param name The file name.
     * @return A file object.
     */
    public static File toFile(String name) {
        return new File(name);
    }

    /**
     * Shows the message.
     *
     * @param width Integer value, in pixels.
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     */
    public static void showMessage(int width, int type,
                                   String title, String text) {
        MessageUtils.INSTANCE.showMessage(width, type, title, text);
    }

    /**
     * Shows the message. It relies on the default width.
     *
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     */
    public static void showMessage(int type, String title, String text) {
        MessageUtils.INSTANCE.showMessage(type, title, text);
    }

    /**
     * Shows a message with options presented as an array of buttons.
     *
     * @param width   Integer value, in pixels.
     * @param type    Type of message.
     * @param title   Title of the message.
     * @param text    Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    public static int showOptions(int width, int type, String title,
                                  String text, Object... buttons) {
        return MessageUtils.INSTANCE.showOptions(width, type, title, text, buttons);
    }

    /**
     * Shows a message with options presented as an array of buttons. It relies
     * on the default width.
     *
     * @param type    Type of message.
     * @param title   Title of the message.
     * @param text    Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    public static int showOptions(int type, String title,
                                  String text, Object... buttons) {
        return MessageUtils.INSTANCE.showOptions(type, title, text, buttons);
    }

    /**
     * Shows a message with a text input.
     *
     * @param width Integer value, in pixels.
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     * @return The string representing the input text.
     */
    public static String showInput(int width, int type,
                                   String title, String text) {
        return MessageUtils.INSTANCE.showInput(width, type, title, text);
    }

    /**
     * Shows a message with a text input. It relies on the default width.
     *
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     * @return The string representing the input text.
     */
    public static String showInput(int type, String title, String text) {
        return MessageUtils.INSTANCE.showInput(type, title, text);
    }

    /**
     * Shows a message with options presented as a dropdown list of elements.
     *
     * @param width    Integer value, in pixels.
     * @param type     Type of message.
     * @param title    Title of the message.
     * @param text     Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    public static int showDropdown(int width, int type, String title,
                                   String text, Object... elements) {
        return MessageUtils.INSTANCE.showDropdown(width, type, title, text, elements);
    }

    /**
     * Shows a message with options presented as a dropdown list of elements. It
     * relies on the default width.
     *
     * @param type     Type of message.
     * @param title    Title of the message.
     * @param text     Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    public static int showDropdown(int type, String title,
                                   String text, Object... elements) {
        return MessageUtils.INSTANCE.showDropdown(type, title, text, elements);
    }

    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    public static boolean isOnPath(String command) {
        return CommonUtils.INSTANCE.isOnPath(command);
    }

    /**
     * Unsafely executes a system command from the underlying operating system
     * and returns a pair containing the exit status and the command output as a
     * string.
     *
     * @param command The system command to be executed.
     * @return A pair containing the exit status and the system command output
     * as a string.
     */
    public static Pair<Integer, String> unsafelyExecuteSystemCommand(Command command) {
        return SystemCallUtils.INSTANCE.executeSystemCommand(command);
    }

    /**
     * Gets the file reference for the current directive. It is important to
     * observe that version 4.0 of arara replicates the directive when 'files'
     * is detected amongst the parameters, so each instance will have a
     * different reference.
     *
     * @return A file reference for the current directive.
     */
    public static File currentFile() {
        return Arara.INSTANCE.getConfig().get(AraraSpec.Execution
                .INSTANCE.getFile());
    }

    /**
     * Loads a class from the provided file, potentially a Java archive.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    // TODO: refactor to use the enum
    public static Pair<Integer, Class<?>> loadClass(File file, String name) {
        Pair<ClassLoadingUtils.ClassLoadingStatus, Class<?>> pair =
                ClassLoadingUtils.INSTANCE.loadClass(file, name);
        return new Pair<>(pair.getFirst().ordinal(), pair.getSecond());
    }

    /**
     * Loads a class from the provided string reference, representing a file.
     *
     * @param ref  String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    // TODO: refactor to use the enum
    public static Pair<Integer, Class<?>> loadClass(String ref, String name) {
        Pair<ClassLoadingUtils.ClassLoadingStatus, Class<?>> pair =
                ClassLoadingUtils.INSTANCE.loadClass(new File(ref), name);
        return new Pair<>(pair.getFirst().ordinal(), pair.getSecond());
    }

    /**
     * Loads a class from the provided file, instantiating it.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    // TODO: refactor to use the enum
    public static Pair<Integer, Object> loadObject(File file, String name) {
        Pair<ClassLoadingUtils.ClassLoadingStatus, Object> pair =
                ClassLoadingUtils.INSTANCE.loadObject(file, name);
        return new Pair<>(pair.getFirst().ordinal(), pair.getSecond());
    }

    /**
     * Loads a class from the provided string reference, instantiating it.
     *
     * @param ref  String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    // TODO: refactor to use the enum
    public static Pair<Integer, Object> loadObject(String ref, String name) {
        Pair<ClassLoadingUtils.ClassLoadingStatus, Object> pair =
                ClassLoadingUtils.INSTANCE.loadObject(new File(ref), name);
        return new Pair<>(pair.getFirst().ordinal(), pair.getSecond());
    }

    /**
     * List all files from the provided directory according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     *
     * @param directory  The provided directory.
     * @param extensions The list of extensions.
     * @param recursive  A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    public static List<File> listFilesByExtensions(File directory,
                                                   List<String> extensions, boolean recursive) {
        return FileSearchingUtils.INSTANCE.listFilesByExtensions(
                directory,
                extensions,
                recursive
        );
    }

    /**
     * List all files from the provided string path according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     *
     * @param path       The provided path as plain string.
     * @param extensions The list of extensions.
     * @param recursive  A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    public static List<File> listFilesByExtensions(String path,
                                                   List<String> extensions, boolean recursive) {
        return FileSearchingUtils.INSTANCE.listFilesByExtensions(
                new File(path),
                extensions,
                recursive
        );
    }

    /**
     * List all files from the provided directory matching the list of file
     * name patterns. Such list can contain wildcards.
     *
     * @param directory The provided directory.
     * @param patterns  The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    public static List<File> listFilesByPatterns(File directory,
                                                 List<String> patterns, boolean recursive) {
        return FileSearchingUtils.INSTANCE.listFilesByPatterns(
                directory,
                patterns,
                recursive
        );
    }

    /**
     * List all files from the provided path matching the list of file
     * name patterns. Such list can contain wildcards.
     *
     * @param path      The provided path as plain string.
     * @param patterns  The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    public static List<File> listFilesByPatterns(String path,
                                                 List<String> patterns, boolean recursive) {
        return FileSearchingUtils.INSTANCE.listFilesByPatterns(
                new File(path),
                patterns,
                recursive
        );
    }

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param file   The file.
     * @param text   The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    public static boolean writeToFile(File file, String text, boolean append) {
        return FileHandlingUtils.INSTANCE.writeToFile(file, text, append);
    }

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param path   The path.
     * @param text   The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    public static boolean writeToFile(String path, String text,
                                      boolean append) {
        return FileHandlingUtils.INSTANCE.writeToFile(new File(path), text, append);
    }

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param file   The file.
     * @param lines  The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    public static boolean writeToFile(File file, List<String> lines,
                                      boolean append) {
        return FileHandlingUtils.INSTANCE.writeToFile(file, lines, append);
    }

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param path   The path.
     * @param lines  The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    public static boolean writeToFile(String path, List<String> lines,
                                      boolean append) {
        return FileHandlingUtils.INSTANCE.writeToFile(new File(path), lines, append);
    }

    /**
     * Reads the provided file into a list of strings.
     *
     * @param file The file.
     * @return A list of strings.
     */
    public static List<String> readFromFile(File file) {
        return FileHandlingUtils.INSTANCE.readFromFile(file);
    }

    /**
     * Reads the provided file into a list of strings.
     *
     * @param path The path.
     * @return A list of strings.
     */
    public static List<String> readFromFile(String path) {
        return FileHandlingUtils.INSTANCE.readFromFile(new File(path));
    }

    /**
     * Checks whether a directory is under the project directory.
     *
     * @param directory The directory to be inspected.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    public static boolean isSubdirectory(File directory)
            throws AraraException {
        return FileHandlingUtils.INSTANCE.isSubDirectory(
                directory, getOriginalReference());
    }
}

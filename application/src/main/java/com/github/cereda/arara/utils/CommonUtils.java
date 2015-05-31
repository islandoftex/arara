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
import com.github.cereda.arara.controller.SystemCallController;
import com.github.cereda.arara.model.AraraException;
import com.github.cereda.arara.model.Argument;
import com.github.cereda.arara.model.Database;
import com.github.cereda.arara.model.FileType;
import com.github.cereda.arara.model.Messages;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * Implements common utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class CommonUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Checks if the input string is equal to a valid boolean value.
     * @param value The input string.
     * @return A boolean value represented by the provided string.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean checkBoolean(String value) throws AraraException {
        List<String> yes = Arrays.asList(
                new String[]{"yes", "true", "1", "on"}
        );
        List<String> no = Arrays.asList(
                new String[]{"no", "false", "0", "off"}
        );
        if (!union(yes, no).contains(value.toLowerCase())) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_CHECKBOOLEAN_NOT_VALID_BOOLEAN,
                            value
                    )
            );
        } else {
            return yes.contains(value.toLowerCase());
        }
    }

    /**
     * Provides a union set operation between two lists.
     * @param <T> The list type.
     * @param list1 The first list.
     * @param list2 The second list.
     * @return The union of those two lists.
     */
    private static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> elements = new HashSet<T>();
        elements.addAll(list1);
        elements.addAll(list2);
        return new ArrayList<T>(elements);
    }

    /**
     * Build a system-dependant path based on the path and the file.
     * @param path A string representing the path to be prepended.
     * @param file A string representing the file to be appended.
     * @return The full path as a string.
     */
    public static String buildPath(String path, String file) {
        return path.endsWith(File.separator) ?
                path.concat(file) : path.concat(File.separator).concat(file);
    }

    /**
     * Checks if the provided string is empty. It does not handle a null value.
     * @param string A string.
     * @return A boolean value indicating if the string is empty.
     */
    public static boolean checkEmptyString(String string) {
        return "".equals(string);
    }

    /**
     * Removes the keyword from the beginning of the provided string.
     * @param line A string to be analyzed.
     * @return The provided string without the keyword.
     */
    public static String removeKeyword(String line) {
        if (line != null) {
            Pattern pattern = Pattern.compile("^(\\s)*<arara>\\s");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                line = (line.substring(matcher.end(), line.length()));
            }
            line = line.trim();
        }
        return line;
    }

    /**
     * Discovers the file through string reference lookup and sets the
     * configuration accordingly.
     * @param reference The string reference.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static void discoverFile(String reference) throws AraraException {
        File file = lookupFile(reference);
        if (file == null) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_DISCOVERFILE_FILE_NOT_FOUND,
                            reference,
                            getFileTypesList()
                    )
            );
        }
    }

    /**
     * Performs a file lookup based on a string reference.
     * @param reference The file reference as a string.
     * @return The file as result of the lookup operation.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private static File lookupFile(String reference) throws AraraException {
        @SuppressWarnings("unchecked")
        List<FileType> types = (List<FileType>) ConfigurationController.
                getInstance().get("execution.filetypes");
        File file = new File(reference);
        String name = file.getName();
        String parent = getParentCanonicalPath(file);
        String path = buildPath(parent, name);
        
        // direct search, so we are considering
        // the reference as a complete name
        for (FileType type : types) {
            if (path.endsWith(".".concat(type.getExtension()))) {
                file = new File(path);
                if (file.exists()) {
                    if (file.isFile()) {
                        ConfigurationController.
                                getInstance().
                                put("execution.file.pattern",
                                        type.getPattern());
                        ConfigurationController.
                                getInstance().
                                put("execution.reference", file);
                        return file;
                    }
                }
            }
        }
        
        // indirect search; in this case, we are considering
        // that the file reference has an implict extension,
        // so we need to add it and look again
        for (FileType type : types) {
            path = buildPath(parent, name.concat(".").
                    concat(type.getExtension()));
            file = new File(path);
            if (file.exists()) {
                if (file.isFile()) {
                    ConfigurationController.getInstance().
                            put("execution.file.pattern", type.getPattern());
                    ConfigurationController.getInstance().
                            put("execution.reference", file);
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Gets the parent canonical path of a file.
     * @param file The file.
     * @return The parent canonical path of a file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static String getParentCanonicalPath(File file)
            throws AraraException {
        try {
            String path = file.getCanonicalFile().getParent();
            return path;
        } catch (IOException exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_GETPARENTCANONICALPATH_IO_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Gets the canonical file from a file.
     * @param file The file.
     * @return The canonical file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static File getCanonicalFile(String file) throws AraraException {
        try {
            return (new File(file)).getCanonicalFile();
        } catch (IOException exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_GETCANONICALFILE_IO_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Checks if the provided object is from a certain class.
     * @param clazz The class.
     * @param object The object.
     * @return A boolean value indicating if the provided object is from a
     * certain class.
     */
    public static boolean checkClass(Class clazz, Object object) {
        return clazz.isInstance(object);
    }

    /**
     * Helper method to flatten a potential list of lists into a list of
     * objects.
     * @param list First list.
     * @param flat Second list.
     */
    private static void flatten(List<?> list, List<Object> flat) {
        for (Object item : list) {
            if (item instanceof List<?>) {
                flatten((List<?>) item, flat);
            } else {
                flat.add(item);
            }
        }
    }

    /**
     * Flattens a potential list of lists into a list of objects.
     * @param list The list to be flattened.
     * @return The flattened list.
     */
    public static List<Object> flatten(List<?> list) {
        List<Object> result = new ArrayList<Object>();
        flatten(list, result);
        return result;
    }

    /**
     * Gets the list of file types, in order.
     * @return A string representation of the list of file types, in order.
     */
    public static String getFileTypesList() {
        @SuppressWarnings("unchecked")
        List<FileType> types = (List<FileType>) ConfigurationController.
                getInstance().get("execution.filetypes");
        return getCollectionElements(types, "[ ", " ]", " | ");
    }

    /**
     * Gets a string representation of a collection.
     * @param collection The collection.
     * @param open The opening string.
     * @param close The closing string.
     * @param separator The element separator.
     * @return A string representation of the provided collection.
     */
    public static String getCollectionElements(Collection collection,
            String open, String close, String separator) {
        StringBuilder builder = new StringBuilder();
        builder.append(open);
        builder.append(StringUtils.join(collection, separator));
        builder.append(close);
        return builder.toString();
    }

    /**
     * Gets a set of strings containing unknown keys from a map and a list. It
     * is a set difference from the keys in the map and the entries in the list.
     * @param parameters The map of parameters.
     * @param arguments The list of arguments.
     * @return A set of strings representing unknown keys from a map and a list.
     */
    public static Set<String> getUnknownKeys(Map<String, Object> parameters,
            List<Argument> arguments) {
        Collection<String> found = parameters.keySet();
        Collection<String> expected = CollectionUtils.collect(
                arguments, new Transformer<Argument, String>() {
            public String transform(Argument argument) {
                return argument.getIdentifier();
            }
        });
        Collection<String> difference = CollectionUtils.
                subtract(found, expected);
        return new HashSet<String>(difference);
    }

    /**
     * Gets the rule error header, containing the identifier and the path, if
     * any.
     * @return A string representation of the rule error header, containing the
     * identifier and the path, if any.
     */
    public static String getRuleErrorHeader() {
        if ((ConfigurationController.getInstance().
                contains("execution.info.rule.id")) &&
                (ConfigurationController.getInstance().
                        contains("execution.info.rule.path"))) {
            String id = (String) ConfigurationController.getInstance().
                    get("execution.info.rule.id");
            String path = (String) ConfigurationController.getInstance().
                    get("execution.info.rule.path");
            return messages.getMessage(
                    Messages.ERROR_RULE_IDENTIFIER_AND_PATH,
                    id,
                    path
            ).concat(" ");
        } else {
            return "";
        }
    }

    /**
     * Trims spaces from every string of a list of strings.
     * @param input The list of strings.
     * @return A new list of strings, with each element trimmed.
     */
    public static List<String> trimSpaces(List<String> input) {
        Collection<String> result = CollectionUtils.collect(
                input, new Transformer<String, String>() {
            public String transform(String input) {
                return input.trim();
            }
        });
        return new ArrayList<String>(result);
    }

    /**
     * Gets a human readable representation of a file size.
     * @param file The file.
     * @return A string representation of the file size.
     */
    public static String calculateFileSize(File file) {
        return FileUtils.byteCountToDisplaySize(file.length());
    }

    /**
     * Gets the date the provided file was last modified.
     * @param file The file.
     * @return A string representation of the date the provided file was last
     * modified.
     */
    public static String getLastModifiedInformation(File file) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return format.format(file.lastModified());
    }

    /**
     * Gets a list of all rule paths.
     * @return A list of all rule paths.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static List<String> getAllRulePaths() throws AraraException {
        @SuppressWarnings("unchecked")
        List<String> paths = (List<String>) ConfigurationController.
                getInstance().get("execution.rule.paths");
        List<String> result = new ArrayList<String>();
        for (String path : paths) {
            File location = new File(InterpreterUtils.construct(path, "quack"));
            result.add(getParentCanonicalPath(location));
        }
        return result;
    }

     /**
     * Gets the reference of the current file in execution. Note that this
     * method might return a value different than the main file provided in
     * the command line.
     * @return A reference of the current file in execution. Might be different
     * than the main file provided in the command line.
     */
    private static File getCurrentReference() {
        return (File) ConfigurationController.getInstance().
                get("execution.file");
    }

    /**
     * Calculates the CRC32 checksum of the provided file.
     * @param file The file.
     * @return A string containing the CRC32 checksum of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static String calculateHash(File file) throws AraraException {
        try {
            long result = FileUtils.checksumCRC32(file);
            return String.format("%08x", result);
        } catch (IOException exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_CALCULATEHASH_IO_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Gets the file type of a file.
     * @param file The file.
     * @return The corresponding file type.
     */
    public static String getFiletype(File file) {
        return getFiletype(file.getName());
    }

    /**
     * Gets the file type of a string representing the file.
     * @param name A string representing the file.
     * @return The corresponding file type.
     */
    public static String getFiletype(String name) {
        name = name.lastIndexOf(".") != -1 ?
                name.substring(name.lastIndexOf(".") + 1, name.length()) : "";
        return name;
    }

    /**
     * Gets the base name of a file.
     * @param file The file.
     * @return The corresponding base name.
     */
    public static String getBasename(File file) {
        return getBasename(file.getName());
    }

    /**
     * Gets the base name of a string representing the file.
     * @param name A string representing the file.
     * @return The corresponding base name.
     */
    public static String getBasename(String name) {
        int index = name.lastIndexOf(".") != -1 ?
                name.lastIndexOf(".") : name.length();
        return name.substring(0, index);
    }

    /**
     * Encloses the provided object in double quotes.
     * @param object The object.
     * @return A string representation of the provided object enclosed in double
     * quotes.
     */
    public static String addQuotes(Object object) {
        return "\"".concat(String.valueOf(object)).concat("\"");
    }

    /**
     * Generates a string based on a list of objects, separating each one of
     * them by one space.
     * @param objects A list of objects.
     * @return A string based on the list of objects, separating each one of
     * them by one space. Empty values are not considered.
     */
    public static String generateString(Object... objects) {
        List<String> values = new ArrayList<String>();
        for (Object object : objects) {
            if (!CommonUtils.checkEmptyString(String.valueOf(object))) {
                values.add(String.valueOf(object));
            }
        }
        return StringUtils.join(values, " ");
    }

    /**
     * Checks if a file exists.
     * @param file The file.
     * @return A boolean value indicating if the file exists.
     */
    public static boolean exists(File file) {
        return file.exists();
    }

    /**
     * Checks if a file exists based on its extension.
     * @param extension The extension.
     * @return A boolean value indicating if the file exists.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean exists(String extension) throws AraraException {
        File file = new File(getPath(extension));
        return file.exists();
    }

    /**
     * Checks if a file has changed since the last verification.
     * @param file The file.
     * @return A boolean value indicating if the file has changed since the last
     * verification.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean hasChanged(File file) throws AraraException {
        Database database = DatabaseUtils.load();
        HashMap<String, String> map = database.getMap();
        String path = getCanonicalPath(file);
        if (!file.exists()) {
            if (map.containsKey(path)) {
                map.remove(path);
                database.setMap(map);
                DatabaseUtils.save(database);
                return true;
            } else {
                return false;
            }
        } else {
            String hash = calculateHash(file);
            if (map.containsKey(path)) {
                String value = map.get(path);
                if (hash.equals(value)) {
                    return false;
                } else {
                    map.put(path, hash);
                    database.setMap(map);
                    DatabaseUtils.save(database);
                    return true;
                }
            } else {
                map.put(path, hash);
                database.setMap(map);
                DatabaseUtils.save(database);
                return true;
            }
        }
    }

    /**
     * Checks if the file has changed since the last verification based on the
     * provided extension.
     * @param extension The provided extension.
     * @return A boolean value indicating if the file has changed since the last
     * verification.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean hasChanged(String extension) throws AraraException {
        File file = new File(getPath(extension));
        return hasChanged(file);
    }

    /**
     * Gets the full file path based on the provided extension.
     * @param extension The extension.
     * @return A string containing the full file path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private static String getPath(String extension) throws AraraException {
        String name = getBasename(getCurrentReference());
        String path = getParentCanonicalPath(getCurrentReference());
        name = name.concat(".").concat(extension);
        return buildPath(path, name);
    }

    /**
     * Gets the canonical path from the provided file.
     * @param file The file.
     * @return The canonical path from the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static String getCanonicalPath(File file) throws AraraException {
        try {
            return file.getCanonicalPath();
        } catch (IOException exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_GETCANONICALPATH_IO_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Checks if the file based on the provided extension contains the provided
     * regex.
     * @param extension The file extension.
     * @param regex The regex.
     * @return A boolean value indicating if the file contains the provided
     * regex.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean checkRegex(String extension, String regex)
            throws AraraException {
        File file = new File(getPath(extension));
        return checkRegex(file, regex);
    }

    /**
     * Checks if the file contains the provided regex.
     * @param file The file.
     * @param regex The regex.
     * @return A boolean value indicating if the file contains the provided
     * regex.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean checkRegex(File file, String regex)
            throws AraraException {
        Charset charset = (Charset) ConfigurationController.
                getInstance().get("directives.charset");
        try {
            String text = FileUtils.readFileToString(file, charset.name());
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            return matcher.find();
        } catch (IOException exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_CHECKREGEX_IO_EXCEPTION,
                            file.getName()
                    ),
                    exception
            );
        }
    }

    /**
     * Replicates a string pattern based on a list of objects, generating a list
     * as result.
     * @param pattern The string pattern.
     * @param values The list of objects to be merged with the pattern.
     * @return A list containing the string pattern replicated to each object
     * from the list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static List<Object> replicateList(String pattern,
            List<Object> values) throws AraraException {
        List<Object> result = new ArrayList<Object>();
        for (Object value : values) {
            try {
                result.add(String.format(pattern, value));
            } catch (MissingFormatArgumentException exception) {
                throw new AraraException(
                        messages.getMessage(
                                Messages.ERROR_REPLICATELIST_MISSING_FORMAT_ARGUMENTS_EXCEPTION
                        ),
                        exception
                );
            }
        }
        return result;
    }

    /**
     * Checks if the provided operating system string holds according to the
     * underlying operating system.
     * @param value A string representing an operating system.
     * @return A boolean value indicating if the provided string refers to the
     * underlying operating system.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static boolean checkOS(String value) throws AraraException {
        Map<String, Boolean> values = new HashMap<String, Boolean>();
        values.put("windows", SystemUtils.IS_OS_WINDOWS);
        values.put("linux", SystemUtils.IS_OS_LINUX);
        values.put("mac", SystemUtils.IS_OS_MAC_OSX);
        values.put("unix", SystemUtils.IS_OS_UNIX);
        values.put("aix", SystemUtils.IS_OS_AIX);
        values.put("irix", SystemUtils.IS_OS_IRIX);
        values.put("os2", SystemUtils.IS_OS_OS2);
        values.put("solaris", SystemUtils.IS_OS_SOLARIS);
        values.put("cygwin", (Boolean) SystemCallController.
                getInstance().get("cygwin"));
        if (!values.containsKey(value.toLowerCase())) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_CHECKOS_INVALID_OPERATING_SYSTEM,
                            value
                    )
            );
        }
        return values.get(value.toLowerCase());
    }
    
    /**
     * Returns the exit status of the application.
     * @return An integer representing the exit status of the application.
     */
    public static int getExitStatus() {
        return (Integer) ConfigurationController.
                getInstance().get("execution.status");
    }
    
    /**
     * Gets the system property according to the provided key, or resort to the
     * fallback value if an exception is thrown or if the key is invalid.
     * @param key The system property key.
     * @param fallback The fallback value.
     * @return A string containing the system property value or the fallback.
     */
    public static String getSystemProperty(String key, String fallback) {
        try {
            String result = System.getProperty(key, fallback);
            return result.equals("") ? fallback : result;
        } catch (Exception exception) {
            return fallback;
        }
    }
    
    /**
     * Gets the preamble content, converting a single string into a list of
     * strings, based on new lines.
     * @return A list of strings representing the preamble content.
     */
    public static List<String> getPreambleContent() {
        if (((Boolean) ConfigurationController.
                    getInstance().get("execution.preamble.active")) == true) {
            return new ArrayList<String>(
                    Arrays.asList(
                            ((String) ConfigurationController.getInstance().
                                    get("execution.preamble.content")
                            ).split("\n"))
            );
        }
        else {
            return new ArrayList<String>();
        }
    }

}

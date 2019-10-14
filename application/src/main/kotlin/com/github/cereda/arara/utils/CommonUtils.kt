/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
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
package com.github.cereda.arara.utils

import com.github.cereda.arara.Arara
import com.github.cereda.arara.configuration.AraraSpec
import com.github.cereda.arara.localization.LanguageController
import com.github.cereda.arara.localization.Messages
import com.github.cereda.arara.model.AraraException
import com.github.cereda.arara.ruleset.Argument
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import java.util.zip.CRC32
import kotlin.math.ln
import kotlin.math.pow


/**
 * Implements common utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object CommonUtils {
    // the application messages obtained from the
    // language controller
    private val messages = LanguageController

    /**
     * Gets the list of file types, in order.
     *
     * @return A string representation of the list of file types, in order.
     */
    val fileTypesList: String
        get() = "[ " + Arara.config[AraraSpec.Execution.fileTypes]
                .joinToString(" | ") + " ]"

    /**
     * Gets the rule error header, containing the identifier and the path, if
     * any.
     *
     * @return A string representation of the rule error header, containing the
     * identifier and the path, if any.
     */
    val ruleErrorHeader: String
        get() {
            return if (Arara.config[AraraSpec.Execution.InfoSpec.ruleId] != null
                    && Arara.config[AraraSpec.Execution.InfoSpec.rulePath] != null) {
                val id = Arara.config[AraraSpec.Execution.InfoSpec.ruleId]!!
                val path = Arara.config[AraraSpec.Execution.InfoSpec.rulePath]!!
                messages.getMessage(
                        Messages.ERROR_RULE_IDENTIFIER_AND_PATH,
                        id,
                        path
                ) + " "
            } else {
                ""
            }
        }

    /**
     * Gets a list of all rule paths.
     *
     * @return A list of all rule paths.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val allRulePaths: List<String>
        @Throws(AraraException::class)
        get() = Arara.config[AraraSpec.Execution.rulePaths].map {
            val location = File(InterpreterUtils.construct(it, "quack"))
            getParentCanonicalPath(location)
        }

    /**
     * Gets the reference of the current file in execution. Note that this
     * method might return a value different than the main file provided in
     * the command line.
     *
     * @return A reference of the current file in execution. Might be different
     * than the main file provided in the command line.
     */
    // TODO: rename as it is pointing to file and not reference?
    private val currentReference: File
        get() = Arara.config[AraraSpec.Execution.file]

    /**
     * Returns the exit status of the application.
     *
     * @return An integer representing the exit status of the application.
     */
    val exitStatus: Int
        get() = Arara.config[AraraSpec.Execution.status]

    /**
     * Gets the preamble content, converting a single string into a list of
     * strings, based on new lines.
     *
     * @return A list of strings representing the preamble content.
     */
    val preambleContent: List<String>
        get() = if (Arara.config[AraraSpec.Execution.preamblesActive]) {
            Arara.config[AraraSpec.Execution.preamblesContent]
                    .split("\n")
                    .dropLastWhile { it.isEmpty() }
                    .toList()
        } else {
            listOf()
        }

    /**
     * Checks if the input string is equal to a valid boolean value.
     *
     * @param value The input string.
     * @return A boolean value represented by the provided string.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun checkBoolean(value: String): Boolean {
        val yes = listOf("yes", "true", "1", "on")
        val no = listOf("no", "false", "0", "off")
        return if (!yes.union(no).contains(value.toLowerCase())) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_CHECKBOOLEAN_NOT_VALID_BOOLEAN,
                            value
                    )
            )
        } else {
            yes.contains(value.toLowerCase())
        }
    }

    /**
     * Removes the keyword from the beginning of the provided string.
     *
     * @param line A string to be analyzed.
     * @return The provided string without the keyword.
     */
    fun removeKeyword(line: String?): String? {
        return if (line == null) null
        else removeKeywordNotNull(line)
    }

    /**
     * Removes the keyword from the beginning of the provided string.
     *
     * @param line A string to be analyzed.
     * @return The provided string without the keyword.
     */
    fun removeKeywordNotNull(line: String): String {
        var tempLine = line
        val pattern = "^(\\s)*<arara>\\s".toPattern()
        val matcher = pattern.matcher(tempLine)
        if (matcher.find()) {
            tempLine = tempLine.substring(matcher.end())
        }
        return tempLine.trim()
    }

    /**
     * Discovers the file through string reference lookup and sets the
     * configuration accordingly.
     *
     * @param reference The string reference.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun discoverFile(reference: String) {
        lookupFile(reference)
                ?: throw AraraException(
                        messages.getMessage(
                                Messages.ERROR_DISCOVERFILE_FILE_NOT_FOUND,
                                reference,
                                fileTypesList
                        )
                )
    }

    /**
     * Performs a file lookup based on a string reference.
     *
     * @param reference The file reference as a string.
     * @return The file as result of the lookup operation.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun lookupFile(reference: String): File? {
        val types = Arara.config[AraraSpec.Execution.fileTypes]
        val file = Arara.config[AraraSpec.Execution.workingDirectory]
                .resolve(reference).toFile()
        val name = file.name
        val parent = getParentCanonicalFile(file)

        // direct search, so we are considering
        // the reference as a complete name
        val testFile = parent.resolve(name)
        if (testFile.exists() && testFile.isFile) {
            types.firstOrNull {
                testFile.toString().endsWith("." + it.extension)
            }?.let {
                Arara.config[AraraSpec.Execution.filePattern] =
                        it.pattern
                Arara.config[AraraSpec.Execution.reference] = testFile
                return testFile
            }
        }

        // indirect search; in this case, we are considering
        // that the file reference has an implicit extension,
        // so we need to add it and look again
        return types.map { it to parent.resolve("$name.${it.extension}") }
                .firstOrNull { it.second.exists() && it.second.isFile }
                ?.let {
                    Arara.config[AraraSpec.Execution.filePattern] =
                            it.first.pattern
                    Arara.config[AraraSpec.Execution.reference] = it.second
                    file
                }
    }

    /**
     * Gets the parent canonical path of a file.
     *
     * @param file The file.
     * @return The parent canonical path of a file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getParentCanonicalPath(file: File): String {
        return getParentCanonicalFile(file).toString()
    }

    /**
     * Gets the parent canonical file of a file.
     *
     * @param file The file.
     * @return The parent canonical file of a file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getParentCanonicalFile(file: File): File {
        try {
            return file.canonicalFile.parentFile
        } catch (exception: IOException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_GETPARENTCANONICALPATH_IO_EXCEPTION
                    ),
                    exception
            )
        }
    }

    /**
     * Flattens a potential list of lists into a list of objects.
     *
     * @param list The list to be flattened.
     * @return The flattened list.
     */
    // TODO: check nullity
    fun flatten(list: List<*>): List<Any> {
        val result = mutableListOf<Any>()
        list.forEach { item ->
            if (item is List<*>)
                result.addAll(flatten(item))
            else
                result.add(item as Any)
        }
        return result
    }

    /**
     * Gets a set of strings containing unknown keys from a map and a list. It
     * is a set difference from the keys in the map and the entries in the list.
     *
     * @param parameters The map of parameters.
     * @param arguments  The list of arguments.
     * @return A set of strings representing unknown keys from a map and a list.
     */
    fun getUnknownKeys(parameters: Map<String, Any>,
                       arguments: List<Argument>): Set<String> {
        val found = parameters.keys
        val expected = arguments.mapNotNull { it.identifier }
        return found.subtract(expected)
    }

    /**
     * Gets a human readable representation of a size.
     *
     * @param size The byte size to be converted.
     * @return A string representation of the size.
     */
    fun byteSizeToString(size: Long): String {
        val language = Arara.config[AraraSpec.Execution.language]
        val unit = 1000
        if (size < unit) return "$size B"
        val exp = (ln(size.toDouble()) / ln(unit.toDouble())).toInt()
        return "%.1f %sB".format(language.locale, size / unit.toDouble()
                .pow(exp.toDouble()), "kMGTPE"[exp - 1])
    }

    /**
     * Gets the date the provided file was last modified.
     *
     * @param file The file.
     * @return A string representation of the date the provided file was last
     * modified.
     */
    fun getLastModifiedInformation(file: File): String {
        return SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                .format(file.lastModified())
    }

    /**
     * Calculates the CRC32 checksum of the provided file.
     *
     * @param file The file.
     * @return A string containing the CRC32 checksum of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun calculateHash(file: File): String {
        try {
            return String.format("%08x", CRC32().run {
                update(file.readBytes())
                value
            })
        } catch (exception: IOException) {
            throw AraraException(messages.getMessage(Messages
                    .ERROR_CALCULATEHASH_IO_EXCEPTION), exception)
        }

    }

    /**
     * Gets the extension of a file.
     *
     * @param file The file.
     * @return The corresponding file type.
     */
    fun getFileExtension(file: File): String = file.extension

    /**
     * Gets the base name of a file.
     *
     * @param file The file.
     * @return The corresponding base name.
     */
    fun getBasename(file: File): String = file.nameWithoutExtension

    /**
     * Generates a string based on a list of objects, separating each one of
     * them by one space.
     *
     * @param objects A list of objects.
     * @return A string based on the list of objects, separating each one of
     * them by one space. Empty values are not considered.
     */
    fun generateString(vararg objects: Any): String = objects
            .map { it.toString() }.filter { it.isNotEmpty() }
            .joinToString(" ")

    /**
     * Checks if a file exists based on its extension.
     *
     * @param extension The extension.
     * @return A boolean value indicating if the file exists.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun exists(extension: String): Boolean {
        val file = File(getPath(extension))
        return file.exists()
    }

    /**
     * Checks if a file has changed since the last verification.
     *
     * @param file The file.
     * @return A boolean value indicating if the file has changed since the last
     * verification.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun hasChanged(file: File): Boolean {
        val database = DatabaseUtils.load()
        val map = database.map
        val path = getCanonicalPath(file)
        return if (!file.exists()) {
            if (map.containsKey(path)) {
                map.remove(path)
                database.map = map
                DatabaseUtils.save(database)
                true
            } else {
                false
            }
        } else {
            val hash = calculateHash(file)
            if (map.containsKey(path)) {
                val value = map[path]
                if (hash == value) {
                    false
                } else {
                    map[path] = hash
                    database.map = map
                    DatabaseUtils.save(database)
                    true
                }
            } else {
                map[path] = hash
                database.map = map
                DatabaseUtils.save(database)
                true
            }
        }
    }

    /**
     * Checks if the file has changed since the last verification based on the
     * provided extension.
     *
     * @param extension The provided extension.
     * @return A boolean value indicating if the file has changed since the last
     * verification.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun hasChanged(extension: String): Boolean =
            hasChanged(File(getPath(extension)))

    /**
     * Gets the full file path based on the provided extension.
     *
     * @param extension The extension.
     * @return A string containing the full file path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun getPath(extension: String): String {
        val name = currentReference.nameWithoutExtension + ".$extension"
        val path = getParentCanonicalFile(currentReference)
        return path.resolve(name).toString()
    }

    /**
     * Gets the canonical path from the provided file.
     *
     * @param file The file.
     * @return The canonical path from the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getCanonicalPath(file: File): String {
        return getCanonicalFile(file).toString()
    }

    /**
     * Gets the canonical file from the provided file.
     *
     * @param file The file.
     * @return The canonical file from the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getCanonicalFile(file: File): File {
        try {
            return file.canonicalFile
        } catch (exception: IOException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_GETCANONICALFILE_IO_EXCEPTION
                    ),
                    exception
            )
        }

    }

    /**
     * Checks if the file based on the provided extension contains the provided
     * regex.
     *
     * @param extension The file extension.
     * @param regex     The regex.
     * @return A boolean value indicating if the file contains the provided
     * regex.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun checkRegex(extension: String, regex: String): Boolean {
        val file = File(getPath(extension))
        return checkRegex(file, regex)
    }

    /**
     * Checks if the file contains the provided regex.
     *
     * @param file  The file.
     * @param regex The regex.
     * @return A boolean value indicating if the file contains the provided
     * regex.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun checkRegex(file: File, regex: String): Boolean {
        try {
            // TODO: do we call this on files > 2 GB?
            val text = file.readText()
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(text)
            return matcher.find()
        } catch (exception: IOException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_CHECKREGEX_IO_EXCEPTION,
                            file.name
                    ),
                    exception
            )
        }

    }

    /**
     * Replicates a string pattern based on a list of objects, generating a list
     * as result.
     *
     * @param pattern The string pattern.
     * @param values  The list of objects to be merged with the pattern.
     * @return A list containing the string pattern replicated to each object
     * from the list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun replicateList(pattern: String,
                      values: List<Any>): List<Any> {
        return try {
            values.map { String.format(pattern, it) }
        } catch (exception: MissingFormatArgumentException) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_REPLICATELIST_MISSING_FORMAT_ARGUMENTS_EXCEPTION
                    ),
                    exception
            )
        }
    }

    /**
     * Checks if the provided operating system string holds according to the
     * underlying operating system.
     *
     * Supported operating systems:
     *
     *   * Windows
     *   * Linux
     *   * Mac OS X
     *   * Unix (Linux || Mac OS)
     *   * Cygwin
     *
     * @param value A string representing an operating system.
     * @return A boolean value indicating if the provided string refers to the
     * underlying operating system.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun checkOS(value: String): Boolean {
        fun checkOSProperty(key: String): Boolean =
                getSystemPropertyOrNull("os.name")
                        ?.toLowerCase()?.startsWith(key.toLowerCase()) ?: false

        val values = mutableMapOf<String, Boolean>()
        values["windows"] = checkOSProperty("Windows")
        values["linux"] = checkOSProperty("Linux")
        values["mac"] = checkOSProperty("Mac OS X")
        values["unix"] = checkOSProperty("Mac OS X") ||
                checkOSProperty("Linux")
        values["cygwin"] = SystemCallUtils["cygwin"] as Boolean
        if (!values.containsKey(value.toLowerCase())) {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_CHECKOS_INVALID_OPERATING_SYSTEM,
                            value
                    )
            )
        }
        // will never throw, see check above
        return values.getValue(value.toLowerCase())
    }

    /**
     * Gets the system property according to the provided key, or resort to the
     * fallback value if an exception is thrown or if the key is invalid.
     *
     * @param key      The system property key.
     * @param fallback The fallback value.
     * @return A string containing the system property value or the fallback.
     */
    fun getSystemProperty(key: String, fallback: String): String {
        return try {
            val result = System.getProperty(key, fallback)
            if (result == "") fallback else result
        } catch (exception: Exception) {
            fallback
        }
    }

    /**
     * Access a system property.
     *
     * @param key The key of the property.
     * @return The value of the system property or null if there is an
     *   exception.
     */
    fun getSystemPropertyOrNull(key: String): String? {
        return try {
            System.getProperty(key)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Generates a list of filenames from the provided command based on a list
     * of extensions for each underlying operating system.
     *
     * @param command A string representing the command.
     * @return A list of filenames.
     */
    private fun appendExtensions(command: String): List<String> {
        // list of extensions, specific for
        // each operating system (in fact, it
        // is more Windows specific)
        val extensions = if (checkOS("windows")) {
            // the application is running on
            // Windows, so let's look for the
            // following extensions in order

            // this list is actually a sublist from
            // the original Windows PATHEXT environment
            // variable which holds the list of executable
            // extensions that Windows supports
            listOf(".com", ".exe", ".bat", ".cmd")
        } else {
            // no Windows, so the default
            // extension will be just an
            // empty string
            listOf("")
        }

        // return the resulting list holding the
        // filenames generated from the
        // provided command
        return extensions.map { "$command$it" }
    }

    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    fun isOnPath(command: String): Boolean {
        return try {
            // first and foremost, let's build the list
            // of filenames based on the underlying
            // operating system
            val filenames = appendExtensions(command)

            // break the path into several parts
            // based on the path separator symbol
            System.getenv("PATH").split(File.pathSeparator)
                    .asSequence()
                    .mapNotNull { File(it).listFiles() }
                    // if the search does not return an empty
                    // list, one of the filenames got a match,
                    // and the command is available somewhere
                    // in the system path
                    .firstOrNull {
                        it.any { file ->
                            filenames.contains(file.name) && !file.isDirectory
                        }
                    }?.let { true }
            // otherwise it is not in the path
                    ?: false
        } catch (exception: Exception) {
            // an exception was raised, simply
            // return and forget about it
            false
        }
    }

    /**
     * Gets the full base name of a file.
     *
     * @param file The file.
     * @return The corresponding full base name.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getFullBasename(file: File): String =
            if (!file.toString().contains(File.separator)) {
                // if the provided file does not contain a
                // file separator, fallback to the usual
                // base name lookup
                getBasename(file)
            } else {
                // we need to get the parent file, get the
                // canonical path and build the corresponding
                // full base name path
                getCanonicalFile(file.parentFile ?: file)
                        .resolve(getBasename(file)).toString()
            }

    /**
     * Checks whether a directory is under a root directory.
     *
     * @param f1 Directory to be inspected.
     * @param f2 Root directory.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    @Throws(AraraException::class)
    fun isSubDirectory(f1: File, f2: File): Boolean {
        return if (f1.isDirectory) {
            getCanonicalPath(f1).startsWith(
                    getParentCanonicalPath(f2) + File.separator
            )
        } else {
            throw AraraException(
                    messages.getMessage(
                            Messages.ERROR_ISSUBDIRECTORY_NOT_A_DIRECTORY,
                            f1.name
                    )
            )
        }
    }
}

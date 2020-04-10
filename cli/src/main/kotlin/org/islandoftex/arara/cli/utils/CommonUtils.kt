// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.File
import java.io.IOException
import java.util.MissingFormatArgumentException
import java.util.regex.Pattern
import kotlin.math.ln
import kotlin.math.pow
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.filehandling.FileHandlingUtils
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages

/**
 * Implements common utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object CommonUtils {
    /**
     * Gets the list of file types as string, in order.
     *
     * @return A string representation of the list of file types, in order.
     */
    val fileTypesList: String
        get() = "[ " + Arara.config[AraraSpec.executionOptions].fileTypes
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
            return if (Arara.config[AraraSpec.Execution.InfoSpec.ruleId] != null &&
                    Arara.config[AraraSpec.Execution.InfoSpec.rulePath] != null) {
                val id = Arara.config[AraraSpec.Execution.InfoSpec.ruleId]!!
                val path = Arara.config[AraraSpec.Execution.InfoSpec.rulePath]!!
                LanguageController.getMessage(
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
            FileHandlingUtils.getParentCanonicalPath(location)
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
                LanguageController.getMessage(
                    Messages.ERROR_CHECKBOOLEAN_NOT_VALID_BOOLEAN,
                    value
                )
            )
        } else {
            yes.contains(value.toLowerCase())
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
     * @param arguments The list of arguments.
     * @return A set of strings representing unknown keys from a map and a list.
     */
    fun getUnknownKeys(
        parameters: Map<String, Any>,
        arguments: List<RuleArgument<*>>
    ): Set<String> {
        val found = parameters.keys
        val expected = arguments.map { it.identifier }
        return found.subtract(expected)
    }

    /**
     * Gets a human readable representation of a size.
     *
     * @param size The byte size to be converted.
     * @return A string representation of the size.
     */
    @Suppress("MagicNumber")
    fun byteSizeToString(size: Long): String {
        val language = Arara.config[AraraSpec.Execution.language]
        val conversionFactor = 1000.0
        return if (size < conversionFactor) "$size B"
        else
            (ln(size.toDouble()) / ln(conversionFactor)).toInt().let { exp ->
                "%.1f %sB".format(language.locale,
                        size / conversionFactor.pow(exp.toDouble()),
                        "kMGTPE"[exp - 1])
            }
    }

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
     * Checks if the file based on the provided extension contains the provided
     * regex.
     *
     * @param extension The file extension.
     * @param regex The regex.
     * @return A boolean value indicating if the file contains the provided
     * regex.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun checkRegex(extension: String, regex: String): Boolean {
        val file = File(FileHandlingUtils.getPath(extension))
        return checkRegex(file, regex)
    }

    /**
     * Checks if the file contains the provided regex.
     *
     * As we use [File.readText] this should not be called on files > 2GB.
     *
     * @param file The file.
     * @param regex The regex.
     * @return A boolean value indicating if the file contains the provided
     * regex.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun checkRegex(file: File, regex: String): Boolean {
        try {
            val text = file.readText()
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(text)
            return matcher.find()
        } catch (exception: IOException) {
            throw AraraException(
                LanguageController.getMessage(
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
     * @param values The list of objects to be merged with the pattern.
     * @return A list containing the string pattern replicated to each object
     * from the list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun replicateList(
        pattern: String,
        values: List<Any>
    ): List<Any> {
        return try {
            values.map { String.format(pattern, it) }
        } catch (exception: MissingFormatArgumentException) {
            throw AraraException(
                LanguageController.getMessage(
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
                LanguageController.getMessage(
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
     * @param key The system property key.
     * @param fallback The fallback value.
     * @return A string containing the system property value or the fallback.
     */
    fun getSystemProperty(key: String, fallback: String): String =
            System.getProperties().runCatching {
                getOrDefault(key, fallback).toString().takeIf { it != "" }
            }.getOrNull() ?: fallback

    /**
     * Access a system property.
     *
     * @param key The key of the property.
     * @return The value of the system property or null if there is an
     *   exception.
     */
    fun getSystemPropertyOrNull(key: String): String? =
            System.getProperties().runCatching { getValue(key).toString() }
                    .getOrNull()

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
        // first and foremost, let's build the list
        // of filenames based on the underlying
        // operating system
        val filenames = appendExtensions(command)
        return kotlin.runCatching {
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
        }.getOrNull() ?: false
        // otherwise (and in case of an exception) it is not in the path
    }
}

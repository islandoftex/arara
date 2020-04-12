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
     * The rule identifier to display in the rule error header.
     */
    var ruleId: String? = null

    /**
     * The rule path to display in the rule error header.
     */
    var rulePath: String? = null

    /**
     * Gets the rule error header, containing the identifier and the path, if
     * any.
     *
     * @return A string representation of the rule error header, containing the
     * identifier and the path, if any.
     */
    val ruleErrorHeader: String
        get() {
            return ruleId?.let { id ->
                rulePath?.let { path ->
                    LanguageController.getMessage(
                            Messages.ERROR_RULE_IDENTIFIER_AND_PATH,
                            id,
                            path
                    ) + " "
                }
            } ?: ""
        }

    /**
     * Gets the list of file types as string, in order.
     *
     * @return A string representation of the list of file types, in order.
     */
    val fileTypesList: String
        get() = "[ " + Arara.config[AraraSpec.executionOptions].fileTypes
                .joinToString(" | ") + " ]"

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
            FileHandlingUtils.getParentCanonicalFile(location).toString()
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
        val conversionFactor = 1000.0
        return if (size < conversionFactor) "$size B"
        else
            (ln(size.toDouble()) / ln(conversionFactor)).toInt().let { exp ->
                "%.1f %sB".format(
                        Arara.config[AraraSpec.userInterfaceOptions].locale,
                        size / conversionFactor.pow(exp.toDouble()),
                        "kMGTPE"[exp - 1]
                )
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
}

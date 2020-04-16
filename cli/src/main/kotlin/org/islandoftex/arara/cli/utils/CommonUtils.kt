// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.File
import java.io.IOException
import java.util.MissingFormatArgumentException
import java.util.regex.Pattern
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.filehandling.FileHandlingUtils
import org.islandoftex.arara.core.localization.LanguageController

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
                    LanguageController.messages.ERROR_RULE_IDENTIFIER_AND_PATH
                            .format(id, path) + " "
                }
            } ?: ""
        }

    /**
     * Gets the list of file types as string, in order.
     *
     * @return A string representation of the list of file types, in order.
     */
    val fileTypesList: String
        get() = Arara.config[AraraSpec.executionOptions].fileTypes
                .joinToString(" | ", "[ ", " ]")

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
                    LanguageController.messages.ERROR_CHECKREGEX_IO_EXCEPTION
                            .format(file.name),
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
                    LanguageController.messages.ERROR_REPLICATELIST_MISSING_FORMAT_ARGUMENTS_EXCEPTION,
                    exception
            )
        }
    }
}

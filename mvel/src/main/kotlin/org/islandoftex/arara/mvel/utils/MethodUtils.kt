// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import java.io.File
import java.io.IOException
import java.util.MissingFormatArgumentException
import java.util.regex.Pattern
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

object MethodUtils {
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
    @JvmStatic
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
     * Generates a string based on a list of objects, separating each one of
     * them by one space.
     *
     * @param objects A list of objects.
     * @return A string based on the list of objects, separating each one of
     * them by one space. Empty values are not considered.
     */
    @JvmStatic
    fun generateString(vararg objects: Any): String = objects
            .map { it.toString() }.filter { it.isNotEmpty() }
            .joinToString(" ")

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
    @JvmStatic
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

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     * @param file The file.
     * @param text The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    @JvmStatic
    fun writeToFile(file: File, text: String, append: Boolean): Boolean {
        return try {
            // try to write the provided
            // string to the file, with
            // UTF-8 as encoding
            if (append)
                file.appendText(text, Charsets.UTF_8)
            else
                file.writeText(text, Charsets.UTF_8)
            true
        } catch (_: IOException) {
            // if something bad happens,
            // gracefully fallback to
            // reporting the failure
            false
        }
    }
}

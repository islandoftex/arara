// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.MissingFormatArgumentException
import java.util.regex.Pattern
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Executor

object MethodUtils {
    /**
     * Gets the reference of the current file in execution. Note that this
     * method might return a value different than the main file provided in
     * the command line.
     *
     * @return A reference of the current file in execution. Might be different
     * than the main file provided in the command line.
     */
    private val currentFile: Path
        get() = Arara.config[AraraSpec.Execution.reference].path

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
     * Checks if a file exists based on its extension.
     *
     * @param extension The extension.
     * @return A boolean value indicating if the file exists.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun exists(extension: String): Boolean =
            Files.exists(FileHandling.changeExtension(currentFile, extension))

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
     * Gets the base name of a file.
     *
     * @param file The file.
     * @return The corresponding base name.
     */
    fun getBasename(file: File): String = file.nameWithoutExtension

    /**
     * Gets the extension of a file.
     *
     * @param file The file.
     * @return The corresponding file type.
     */
    fun getFileExtension(file: File): String = file.extension

    /**
     * Gets the full file path based on the provided extension.
     *
     * @param extension The extension.
     * @return A string containing the full file path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getPath(extension: String): File =
            FileHandling.changeExtension(currentFile, extension).toFile()

    /**
     * Checks if a file has changed since the last verification.
     *
     * @param file The file.
     * @param project The project the file is part of.
     * @return A boolean value indicating if the file has changed since the last
     * verification.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    @JvmOverloads
    fun hasChanged(
        file: File,
        project: Project = Arara.config[AraraSpec.Execution.currentProject],
        databaseName: Path = Executor.executionOptions.databaseName
    ): Boolean = FileHandling.hasChanged(
            file.toPath(),
            project.workingDirectory.resolve(databaseName)
    )

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

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     * @param file The file.
     * @param text The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
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

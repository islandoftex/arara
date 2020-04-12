// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.core.files.FileHandling

/**
 * Implements file handling utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object FileHandlingUtils {
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

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     * @param file The file.
     * @param lines The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(
        file: File,
        lines: List<String>,
        append: Boolean
    ): Boolean = writeToFile(
            file,
            lines.joinToString(System.lineSeparator()),
            append
    )

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
     * Gets the parent canonical file of a file.
     *
     * @param file The file.
     * @return The parent canonical file of a file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getParentCanonicalFile(file: File): File {
        return file.runCatching {
            this.canonicalFile.parentFile
        }.getOrElse {
            // it is IOException || is is SecurityException
            throw AraraException(
                    LanguageController.getMessage(
                            Messages.ERROR_GETPARENTCANONICALPATH_IO_EXCEPTION
                    ), it
            )
        }
    }

    /**
     * Gets the full file path based on the provided extension.
     *
     * @param extension The extension.
     * @return A string containing the full file path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getPath(extension: String): String =
            FileHandling.changeExtension(currentFile, extension).toString()

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
        databaseName: Path = Arara.config[AraraSpec.executionOptions].databaseName
    ): Boolean = FileHandling.hasChanged(
            file.toPath(),
            project.workingDirectory.resolve(databaseName)
    )
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.zip.CRC32
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.core.files.Database

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
    private val currentFile: File
        get() = Arara.config[AraraSpec.Execution.reference].path.toFile()

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
    ): Boolean =
            try {
                writeToFile(file, lines.joinToString(System.lineSeparator()),
                        append)
            } catch (_: IOException) {
                false
            }

    /**
     * Reads the provided file (UTF-8) into a list of strings.
     * @param file The file.
     * @return A list of strings.
     */
    fun readFromFile(file: File): List<String> {
        return try {
            // returns the contents of
            // the provided file as
            // a list of strings
            file.readLines(Charsets.UTF_8)
        } catch (_: IOException) {
            // if something bad happens,
            // gracefully fallback to
            // an empty file list
            listOf()
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
    fun exists(extension: String): Boolean {
        val file = File(getPath(extension))
        return file.exists()
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
    fun getPath(extension: String): String {
        val name = currentFile.nameWithoutExtension + ".$extension"
        val path = getParentCanonicalFile(currentFile)
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
                    LanguageController.getMessage(
                            Messages.ERROR_GETCANONICALFILE_IO_EXCEPTION
                    ),
                    exception
            )
        }
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
    fun calculateHash(file: File): Long =
            try {
                CRC32().run {
                    update(file.readBytes())
                    value
                }
            } catch (exception: IOException) {
                throw AraraException(
                        LanguageController.getMessage(
                                Messages.ERROR_CALCULATEHASH_IO_EXCEPTION
                        ), exception
                )
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
        project: Project = Arara.config[AraraSpec.Execution.currentProject]
    ): Boolean {
        val dbPath = project.workingDirectory.resolve(Arara
                .config[AraraSpec.executionOptions].databaseName)
        val database = Database.load(dbPath)
        val path = Paths.get(getCanonicalPath(file))
        return if (!file.exists()) {
            if (path in database) {
                database.remove(path)
                database.save(dbPath)
                true
            } else {
                false
            }
        } else {
            val hash = calculateHash(file)
            if (path in database) {
                val value = database[path]
                if (hash == value) {
                    false
                } else {
                    database[path] = hash
                    database.save(dbPath)
                    true
                }
            } else {
                database[path] = hash
                database.save(dbPath)
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
     * Checks whether a directory is under a root directory.
     *
     * @param child Directory to be inspected.
     * @param parent Root directory.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    @Throws(AraraException::class)
    fun isSubDirectory(child: File, parent: File): Boolean {
        return if (child.isDirectory && parent.isDirectory) {
            getCanonicalPath(child).startsWith(
                    getParentCanonicalPath(parent) + File.separator
            )
        } else {
            throw AraraException(
                    LanguageController.getMessage(
                            Messages.ERROR_ISSUBDIRECTORY_NOT_A_DIRECTORY,
                            child.name
                    )
            )
        }
    }
}

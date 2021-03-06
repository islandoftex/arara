// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.io.File
import java.io.FileFilter
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController
import org.slf4j.LoggerFactory

/**
 * Implements file searching auxiliary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object FileSearching {
    // get the logger context from a factory
    private val logger = LoggerFactory.getLogger(FileSearching::class.java)

    /**
     * List all files from the provided directory according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     *
     * @param directory The provided directory.
     * @param extensions The list of extensions.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    @JvmStatic
    fun listFilesByExtensions(
        directory: File,
        extensions: List<String>,
        recursive: Boolean
    ): List<File> = try {
        // return the result of the
        // provided search
        if (recursive)
            directory.walkTopDown().asSequence()
                    .filter { !it.isDirectory }
                    .filter { extensions.contains(it.extension) }
                    .toList()
        else
            directory.listFiles(
                    FileFilter { extensions.contains(it.extension) })!!
                    .toList()
    } catch (_: Exception) {
        // if something bad happens,
        // gracefully fallback to
        // an empty file list
        emptyList()
    }

    /**
     * List all files from the provided directory matching the list of file
     * name patterns. Such list can contain wildcards.
     *
     * @param directory The provided directory.
     * @param patterns The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    @JvmStatic
    fun listFilesByPatterns(
        directory: File,
        patterns: List<String>,
        recursive: Boolean
    ): List<File> = try {
        // return the result of the provided
        // search, with the wildcard filter
        // and a potential recursive search
        val pathMatcher = patterns.map {
            FileSystems.getDefault().getPathMatcher("glob:$it")
        }
        if (recursive)
            directory.walkTopDown().asSequence()
                    .filter { !it.isDirectory }
                    .filter { file ->
                        pathMatcher.any { it.matches(file.toPath().fileName) }
                    }.toList()
        else
            directory.listFiles { file: File ->
                pathMatcher.any { it.matches(file.toPath().fileName) }
            }!!.toList()
    } catch (_: Exception) {
        // if something bad happens,
        // gracefully fallback to
        // an empty file list
        emptyList()
    }

    /**
     * Discovers the file through string reference lookup and sets the
     * configuration accordingly.
     *
     * @param reference The string reference.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun resolveFile(
        reference: String,
        workingDirectory: Path,
        executionOptions: ExecutionOptions
    ): ProjectFile =
            lookupFile(reference, workingDirectory, executionOptions)
                    ?: throw AraraException(
                            LanguageController.messages.ERROR_DISCOVERFILE_FILE_NOT_FOUND
                                    .format(
                                            reference,
                                            executionOptions.fileTypes
                                                    .joinToString(" | ", "[ ", " ]")
                                    )
                    )

    /**
     * Performs a file lookup based on a string reference.
     *
     * @param reference The file reference as a string.
     * @return The file as result of the lookup operation.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun lookupFile(
        reference: String,
        workingDirectory: Path,
        executionOptions: ExecutionOptions
    ): ProjectFile? {
        val types = executionOptions.fileTypes

        // direct search, so we are considering
        // the reference as a complete name
        val testFile = FileHandling.normalize(workingDirectory / reference)
        return when {
            testFile.exists() && testFile.isRegularFile() -> {
                types.firstOrNull {
                    reference.endsWith("." + it.extension)
                }?.let {
                    val extension = reference.substringAfterLast('.')
                    ProjectFile(
                            path = testFile,
                            fileType = types.firstOrNull { extension == it.extension }
                                    ?: FileType.UNKNOWN_TYPE
                    )
                }
            }
            // when in safe mode we do not perform indirect search
            executionOptions.executionMode == ExecutionMode.SAFE_RUN -> {
                logger.info("Skipping indirect file search with extension " +
                        "completion due to safe mode restrictions.")
                null
            }
            // indirect search; in this case, we are considering
            // that the file reference has an implicit extension,
            // so we need to add it and look again
            else -> {
                val name = testFile.fileName.toString()
                types.map { testFile.parent / "$name.${it.extension}" }
                        .union(types.map {
                            testFile.parent /
                                    "${name.removeSuffix(".").trim()}.${it.extension}"
                        })
                        .firstOrNull { it.exists() && it.isRegularFile() }
                        ?.let { found ->
                            val extension = found.toString().substringAfterLast('.')
                            ProjectFile(
                                    found,
                                    types.firstOrNull { extension == it.extension }
                                            ?: FileType.UNKNOWN_TYPE
                            )
                        }
            }
        }
    }
}

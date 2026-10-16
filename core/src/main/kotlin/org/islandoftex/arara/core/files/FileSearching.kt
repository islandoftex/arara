// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.globToRegex

/**
 * Implements file searching auxiliary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object FileSearching {
    // get the logger context from a factory
    private val logger = KotlinLogging.logger { }

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
    fun listFilesByExtensions(
        directory: MPPPath,
        extensions: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        searchFiles(directory, recursive).filter {
            extensions.contains(it.fileName.substringAfterLast('.', ""))
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
    fun listFilesByPatterns(
        directory: MPPPath,
        patterns: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        patterns.map { it.globToRegex() }.let { regexes ->
            searchFiles(directory, recursive).filter { file -> regexes.any { it.matches(file.fileName) } }
        }

    private fun searchFiles(directory: MPPPath, recursive: Boolean): List<MPPPath> = try {
        directory.listFiles(recursive)
    } catch (_: AraraIOException) {
        emptyList()
    }

    /**
     * Discovers the file through string reference lookup and sets the
     * configuration accordingly.
     *
     * @param reference The string reference.
     * @throw AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun resolveFile(
        reference: String,
        workingDirectory: MPPPath,
        executionOptions: ExecutionOptions
    ): ProjectFile =
        lookupFile(reference, workingDirectory, executionOptions)
            ?: throw fileResolutionError(reference, workingDirectory, executionOptions)

    private fun fileResolutionError(
        reference: String,
        workingDirectory: MPPPath,
        executionOptions: ExecutionOptions
    ): AraraException {
        val messages = LanguageController.messages
        val extensions = executionOptions.fileTypes.map { it.extension }.distinct().joinToString(", ")
        val path = workingDirectory / reference
        val message = when {
            path.isRegularFile -> if (reference.endsWith(".lua")) {
                messages.errorDiscoverFileUnknownLuaExtension(reference, extensions)
            } else {
                messages.errorDiscoverFileUnknownExtension(reference, extensions)
            }
            executionOptions.executionMode == ExecutionMode.SAFE_RUN ->
                messages.errorDiscoverFileSafeModeNoCompletion(reference)
            else -> messages.errorDiscoverFileFileNotFound(reference, extensions)
        }
        return AraraException(message)
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
    internal fun lookupFile(
        reference: String,
        workingDirectory: MPPPath,
        executionOptions: ExecutionOptions
    ): ProjectFile? {
        val types = executionOptions.fileTypes

        // direct search, so we are considering
        // the reference as a complete name
        val testFile = (workingDirectory / reference).normalize()
        return when {
            testFile.exists && testFile.isRegularFile -> {
                types.firstOrNull {
                    reference.endsWith("." + it.extension)
                }?.let { matched ->
                    ProjectFile(path = testFile, fileType = matched)
                }
            }
            // when in safe mode we do not perform indirect search
            executionOptions.executionMode == ExecutionMode.SAFE_RUN -> {
                logger.info {
                    "Skipping indirect file search with extension " +
                        "completion due to safe mode restrictions."
                }
                null
            }
            // indirect search; in this case, we are considering
            // that the file reference has an implicit extension,
            // so we need to add it and look again
            else -> {
                val name = testFile.fileName
                types.map { (testFile.parent / "$name.${it.extension}") to it }
                    .union(
                        types.map {
                            (testFile.parent /
                                "${name.removeSuffix(".").trim()}.${it.extension}") to it
                        }
                    )
                    .firstOrNull { (file, _) -> file.exists && file.isRegularFile }
                    ?.let { (file, type) -> ProjectFile(file, type) }
            }
        }
    }
}

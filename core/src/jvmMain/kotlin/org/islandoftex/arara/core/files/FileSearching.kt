// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.formatString
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
        // ---------- KLPN ----------
        // Replaced local VFS by MPPPath -> File
        runCatching {
            // ---------- KLPN ----------
            // Normalization + File
            directory.normalize().toJVMFile().let {
                // return the result of the
                // provided search
                if (recursive)
                    // ---------- KLPN ----------
                    // Filter file here and not later
                    it.walk().filter { entry -> entry.isFile }
                else
                    // ---------- KLPN ----------
                    // 1. Filter file here and not later
                    // 2. maxDepth = 1 limits search to the
                    //    current directory
                    it.walk().maxDepth(1).filter { entry -> entry.isFile }
            }
        }.getOrDefault(
            // if something bad happens,
            // gracefully fallback to
            // an empty file list

            // ---------- KLPN ----------
            // Result is now a sequence, not a flow
            emptySequence()
        ).filter {
                // ---------- KLPN ----------
                // No need to check if it's a file, it's already
                // been done in the walk(...) step
                extensions.contains(it.extension)
        }.map {
            MPPPath(it.absolutePath).normalize()
        }.toList()

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
        // return the result of the provided
        // search, with the wildcard filter
        // and a potential recursive search
        patterns.map { it.globToRegex() }.let { regexes ->

        runCatching {
            // ---------- KLPN ----------
            // Replaced local VFS by MPPPath -> File
            directory.normalize().toJVMFile().let {

                // return the result of the
                // provided search
                if (recursive)
                    // ---------- KLPN ----------
                    // Filter file here and not later
                    it.walk().filter { entry -> entry.isFile }
                else
                    // ---------- KLPN ----------
                    // 1. Filter file here and not later
                    // 2. maxDepth = 1 limits search to the
                    //    current directory
                    it.walk().maxDepth(1).filter { entry -> entry.isFile }
            }
        }.getOrDefault(
            // if something bad happens,
            // gracefully fallback to
            // an empty file list

            // ---------- KLPN ----------
            // Result is now a sequence, not a flow
            emptySequence()
        ).filter { file ->
                // ---------- KLPN ----------
                // No need to check if it's a file, it's already
                // been done in the walk(...) step
                regexes.any { it.matches(file.name) }
        }.map {
            MPPPath(it.absolutePath).normalize()
        }.toList()
    }

    /**
     * Discovers the file through string reference lookup and sets the
     * configuration accordingly.
     *
     * @param reference The string reference.
     * @throws org.islandoftex.arara.api.AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun resolveFile(
        reference: String,
        workingDirectory: MPPPath,
        executionOptions: ExecutionOptions
    ): org.islandoftex.arara.api.files.ProjectFile =
        lookupFile(reference, workingDirectory, executionOptions)
            ?: throw AraraException(
                LanguageController.messages.ERROR_DISCOVERFILE_FILE_NOT_FOUND
                    .formatString(
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
        workingDirectory: MPPPath,
        executionOptions: ExecutionOptions
    ): org.islandoftex.arara.api.files.ProjectFile? {
        val types = executionOptions.fileTypes

        // direct search, so we are considering
        // the reference as a complete name
        val testFile = (workingDirectory / reference).normalize()
        return when {
            testFile.exists && testFile.isRegularFile -> {
                types.firstOrNull {
                    reference.endsWith("." + it.extension)
                }?.let {
                    val extension = reference.substringAfterLast('.')
                    ProjectFile(
                        path = testFile,
                        fileType = types.firstOrNull { extension == it.extension }
                            ?: FileType.Companion.UNKNOWN_TYPE
                    )
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
                types.map { testFile.parent / "$name.${it.extension}" }
                    .union(
                        types.map {
                            testFile.parent /
                                "${name.removeSuffix(".").trim()}.${it.extension}"
                        }
                    )
                    .firstOrNull { it.exists && it.isRegularFile }
                    ?.let { found ->
                        val extension = found.toString().substringAfterLast('.')
                        ProjectFile(
                            found,
                            types.firstOrNull { extension == it.extension }
                                ?: FileType.Companion.UNKNOWN_TYPE
                        )
                    }
            }
        }
    }
}

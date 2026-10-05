// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.github.oshai.kotlinlogging.KotlinLogging
import okio.FileSystem
import okio.Path.Companion.toPath
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.globToRegex

/**
 * Implements the file lookups, from reference resolution to the file
 * type discovery the interpreter dispatches on.
 *
 * @author Island of TeX
 */
object FileSearching {
    /**
     * The logger of the file searching.
     */
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
        // return the result of the provided
        // search, with the wildcard filter
        // and a potential recursive search
        patterns.map { it.globToRegex() }.let { regexes ->
            searchFiles(directory, recursive).filter { file ->
                regexes.any { it.matches(file.fileName) }
            }
        }

    // the traversal returns the search target itself when it is a
    // regular file — the platform walk included such a target in its
    // own results — and otherwise lists the directory tree, following
    // links as the platform walk did; the recursive listing is lazy,
    // so it is materialized inside the block for traversal failures
    // to hit the fallback
    /**
     * Lists the regular files of the provided directory, its whole
     * tree when the recursive flag is set, falling back to an
     * empty list when the traversal fails.
     *
     * @param directory The directory to search.
     * @param recursive Whether the search follows the whole tree.
     * @return The regular files the traversal found.
     */
    private fun searchFiles(
        directory: MPPPath,
        recursive: Boolean
    ): List<MPPPath> {
        val target = directory.normalize()
        if (target.isRegularFile) return listOf(target)
        return runCatching {
            val path = target.toString().toPath()
            val entries = if (recursive)
                FileSystem.SYSTEM.listRecursively(path, followSymlinks = true)
            else
                FileSystem.SYSTEM.listOrNull(path).orEmpty().asSequence()
            entries.map { MPPPath(it.toString()) }.toList()
        }.getOrDefault(
            // if something bad happens,
            // gracefully fallback to
            // an empty file list
            emptyList()
        ).filter {
            it.isRegularFile
        }
    }

    /**
     * Discovers the file through string reference lookup, failing
     * with a localized error when no file matches.
     *
     * @param reference The string reference.
     * @param workingDirectory The working directory to resolve the
     *   reference against.
     * @param executionOptions The execution options holding the known
     *   file types.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun resolveFile(
        reference: String,
        workingDirectory: MPPPath,
        executionOptions: ExecutionOptions
    ): ProjectFile =
        lookupFile(reference, workingDirectory, executionOptions)
            ?: throw fileResolutionError(
                reference, workingDirectory, executionOptions
            )

    /**
     * Builds the error for a reference no file of the run
     * resolves: the reserved project suffix is refused when its
     * file exists, an existing file whose extension no type
     * carries is told so instead of being reported missing, and
     * everything else keeps the not-found verdict with the
     * extensions the run would have completed the name with.
     */
    private fun fileResolutionError(
        reference: String,
        workingDirectory: MPPPath,
        executionOptions: ExecutionOptions
    ): AraraException {
        val target = (workingDirectory / reference).normalize()
        return when {
            // the project suffix belongs to the project world
            // alone; a missing specification keeps the ordinary
            // verdict below, as the file is simply not there
            reference.trim().endsWith(".proj.lua") && target.exists ->
                AraraException(
                    LanguageController.messages
                        .errorDiscoverFileIsAProjectSpecification(
                            reference
                        )
                )
            target.exists && target.isRegularFile ->
                AraraException(
                    LanguageController.messages
                        .errorDiscoverFileUnknownExtension(reference)
                )
            else ->
                AraraException(
                    LanguageController.messages
                        .errorDiscoverFileFileNotFound(
                            reference,
                            // the extensions speak as a plain
                            // enumeration in the priority order of
                            // the effective list; a type the
                            // configuration redeclares shows once
                            executionOptions.fileTypes
                                .distinctBy { it.extension }
                                .joinToString(", ")
                        )
                )
        }
    }

    /**
     * Looks up the file type an extension maps to, in the priority
     * order of the effective file types: the first type carrying
     * the extension wins, so a type the configuration redeclares
     * is the one its files resolve to.
     *
     * @param extension The extension to look up, without the
     *   leading dot.
     * @param executionOptions The execution options holding the
     *   known file types.
     * @return The file type of the extension, or null when no
     *   effective type carries it.
     */
    fun lookupFileType(
        extension: String,
        executionOptions: ExecutionOptions
    ): FileType? =
        executionOptions.fileTypes.firstOrNull { it.extension == extension }

    /**
     * Performs a file lookup based on a string reference.
     *
     * @param reference The file reference as a string.
     * @param workingDirectory The working directory to resolve the
     *   reference against.
     * @param executionOptions The execution options holding the known
     *   file types.
     * @return The file as result of the lookup operation, or null if no
     *   match was found.
     */
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
                                ?: FileType.UNKNOWN_TYPE
                        )
                    }
            }
        }
    }
}

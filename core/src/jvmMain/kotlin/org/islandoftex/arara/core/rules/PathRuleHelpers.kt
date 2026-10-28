// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor

/** Path names, directory searches and directory operations for rule interpreters. */
object PathRuleHelpers {
    /**
     * Gets the basename.
     *
     * @param file The file.
     * @return The basename of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getBasename(file: MPPPath): String =
        if (file.isRegularFile) {
            file.fileName.substringBeforeLast(".")
        } else {
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .errorBasenameNotAFile(file.fileName)
            )
        }

    /**
     * Gets the basename.
     *
     * @param filename The string.
     * @return The basename.
     */
    fun getBasename(filename: String): String = getBasename(MPPPath(filename))

    /**
     * Gets the file type.
     *
     * @param file The provided file.
     * @return The file type.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getFiletype(file: MPPPath): String =
        if (file.isRegularFile) {
            file.fileName.substringAfterLast(".")
        } else {
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .errorFiletypeNotAFile(file.fileName)
            )
        }

    /**
     * Gets the file type.
     *
     * @param filename The provided string.
     * @return The file type.
     */
    fun getFiletype(filename: String): String = getFiletype(MPPPath(filename))

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
        FileSearching.listFilesByExtensions(
            directory,
            extensions,
            recursive
        )

    /**
     * List all files from the provided string path according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     *
     * @param path The provided path as plain string.
     * @param extensions The list of extensions.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByExtensions(
        path: String,
        extensions: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        listFilesByExtensions(MPPPath(path), extensions, recursive)

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
        FileSearching.listFilesByPatterns(
            directory,
            patterns,
            recursive
        )

    /**
     * List all files from the provided path matching the list of file
     * name patterns. Such list can contain wildcards.
     *
     * @param path The provided path as plain string.
     * @param patterns The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByPatterns(
        path: String,
        patterns: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        listFilesByPatterns(MPPPath(path), patterns, recursive)

    /**
     * Checks whether a directory is under the project directory.
     *
     * @param directory The directory to be inspected.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    @Throws(AraraException::class)
    fun isSubdirectory(directory: MPPPath): Boolean =
        FileHandling.isSubDirectory(
            directory,
            LinearExecutor.currentProject!!.executionDirectory
        )
}

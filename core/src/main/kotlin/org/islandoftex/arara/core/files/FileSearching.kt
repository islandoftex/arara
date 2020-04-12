// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.io.File
import java.io.FileFilter
import java.nio.file.FileSystems

/**
 * Implements file searching auxiliary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object FileSearching {
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
        listOf()
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
        listOf()
    }
}

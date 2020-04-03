// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.filehandling

import java.io.File
import java.io.FileFilter
import java.nio.file.FileSystems
import org.islandoftex.arara.Arara
import org.islandoftex.arara.AraraException
import org.islandoftex.arara.configuration.AraraSpec
import org.islandoftex.arara.files.FileType
import org.islandoftex.arara.files.ProjectFile
import org.islandoftex.arara.localization.LanguageController
import org.islandoftex.arara.localization.Messages
import org.islandoftex.arara.model.UNKNOWN_TYPE
import org.islandoftex.arara.utils.CommonUtils

/**
 * Implements file searching utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object FileSearchingUtils {
    /**
     * List all files from the provided directory according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     * @param directory The provided directory.
     * @param extensions The list of extensions.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByExtensions(
        directory: File,
        extensions: List<String>,
        recursive: Boolean
    ):
            List<File> = try {
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
     * @param directory The provided directory.
     * @param patterns The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByPatterns(
        directory: File,
        patterns: List<String>,
        recursive: Boolean
    ):
            List<File> = try {
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

    /**
     * Discovers the file through string reference lookup and sets the
     * configuration accordingly.
     *
     * @param reference The string reference.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun resolveFile(
        reference: String,
        workingDirectory: File = Arara.config[AraraSpec.Execution.workingDirectory].toFile()
    ): ProjectFile =
            lookupFile(reference, workingDirectory)
                    ?: throw AraraException(
                            LanguageController.getMessage(
                                    Messages.ERROR_DISCOVERFILE_FILE_NOT_FOUND,
                                    reference,
                                    CommonUtils.fileTypesList
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
    private fun lookupFile(reference: String, workingDirectory: File): ProjectFile? {
        val types = Arara.config[AraraSpec.Execution.fileTypes]
        val file = workingDirectory.resolve(reference)
        val name = file.name
        val parent = FileHandlingUtils.getParentCanonicalFile(file)

        // direct search, so we are considering
        // the reference as a complete name
        val testFile = parent.resolve(name)
        if (testFile.exists() && testFile.isFile) {
            types.firstOrNull {
                testFile.toString().endsWith("." + it.extension)
            }?.let {
                return ProjectFile(
                        testFile.toPath(),
                        types.firstOrNull { testFile.extension == it.extension }
                                ?: FileType.UNKNOWN_TYPE
                )
            }
        }

        // indirect search; in this case, we are considering
        // that the file reference has an implicit extension,
        // so we need to add it and look again
        // TODO: disable this step in safe mode
        return types.map { parent.resolve("$name.${it.extension}") }
                .union(types.map {
                    parent.resolve("${name.removeSuffix(".").trim()}.${it.extension}")
                })
                .firstOrNull { it.exists() && it.isFile }
                ?.let { found ->
                    ProjectFile(
                            found.toPath(),
                            types.firstOrNull { found.extension == it.extension }
                                    ?: FileType.UNKNOWN_TYPE
                    )
                }
    }

    fun registerFileAttributes(file: ProjectFile) {
        Arara.config[AraraSpec.Execution.filePattern] = file.fileType.pattern
        Arara.config[AraraSpec.Execution.reference] = file.toFile()
    }
}

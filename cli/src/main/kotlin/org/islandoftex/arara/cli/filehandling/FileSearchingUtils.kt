// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import java.io.File
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Implements file searching utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object FileSearchingUtils {
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
        workingDirectory: File,
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
     * Gets the parent canonical file of a file.
     *
     * @param file The file.
     * @return The parent canonical file of a file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun getParentCanonicalFile(file: File): File {
        return file.runCatching {
            this.canonicalFile.parentFile
        }.getOrElse {
            // it is IOException || is is SecurityException
            throw AraraException(
                    LanguageController.messages.ERROR_GETPARENTCANONICALPATH_IO_EXCEPTION,
                    it
            )
        }
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
        workingDirectory: File,
        executionOptions: ExecutionOptions
    ): ProjectFile? {
        val types = executionOptions.fileTypes
        val file = workingDirectory.resolve(reference)
        val name = file.name
        val parent = getParentCanonicalFile(file)

        // direct search, so we are considering
        // the reference as a complete name
        val testFile = parent.resolve(name)
        if (testFile.exists() && testFile.isFile) {
            types.firstOrNull {
                testFile.toString().endsWith("." + it.extension)
            }?.let {
                return org.islandoftex.arara.cli.model.ProjectFile(
                        path = testFile.toPath(),
                        fileType = types.firstOrNull { testFile.extension == it.extension }
                                ?: FileType.UNKNOWN_TYPE
                )
            }
        }

        // indirect search; in this case, we are considering
        // that the file reference has an implicit extension,
        // so we need to add it and look again
        if (executionOptions.executionMode == ExecutionMode.SAFE_RUN)
            return null

        return types.map { parent.resolve("$name.${it.extension}") }
                .union(types.map {
                    parent.resolve("${name.removeSuffix(".").trim()}.${it.extension}")
                })
                .firstOrNull { it.exists() && it.isFile }
                ?.let { found ->
                    org.islandoftex.arara.cli.model.ProjectFile(
                            found.toPath(),
                            types.firstOrNull { found.extension == it.extension }
                                    ?: FileType.UNKNOWN_TYPE
                    )
                }
    }
}

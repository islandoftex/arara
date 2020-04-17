// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import java.nio.file.Files
import java.nio.file.Path
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.files.FileHandling
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
        val testFile = FileHandling.normalize(workingDirectory.resolve(reference))
        if (Files.exists(testFile) && Files.isRegularFile(testFile)) {
            types.firstOrNull {
                reference.endsWith("." + it.extension)
            }?.let {
                val extension = reference.substringAfterLast('.')
                return org.islandoftex.arara.cli.model.ProjectFile(
                        path = testFile,
                        fileType = types.firstOrNull { extension == it.extension }
                                ?: FileType.UNKNOWN_TYPE
                )
            }
        }

        // indirect search; in this case, we are considering
        // that the file reference has an implicit extension,
        // so we need to add it and look again
        if (executionOptions.executionMode == ExecutionMode.SAFE_RUN)
            return null

        val name = testFile.fileName.toString()
        return types.map { testFile.parent.resolve("$name.${it.extension}") }
                .union(types.map {
                    testFile.parent.resolve(
                            "${name.removeSuffix(".").trim()}.${it.extension}")
                })
                .firstOrNull { Files.exists(it) && Files.isRegularFile(it) }
                ?.let { found ->
                    val extension = found.toString().substringAfterLast('.')
                    org.islandoftex.arara.cli.model.ProjectFile(
                            found,
                            types.firstOrNull { extension == it.extension }
                                    ?: FileType.UNKNOWN_TYPE
                    )
                }
    }
}

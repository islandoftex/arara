// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor

/** File existence, content and change predicates for directive conditionals. */
object ConditionalFileHelpers {
    /**
     * Checks if the file exists according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun exists(extension: String): Boolean = FileHandling.changeExtension(
        LinearExecutor.currentFile!!.path, extension
    ).exists

    /**
     * Checks if the file is missing according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun missing(extension: String): Boolean = !exists(extension)

    /**
     * Checks if the file has changed, according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun changed(extension: String): Boolean = changed(
        FileHandling.changeExtension(
            LinearExecutor.currentFile!!.path, extension
        )
    )

    /**
     * Checks if the file is unchanged according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun unchanged(extension: String): Boolean = !changed(extension)

    /**
     * Checks if the file exists.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    fun exists(filename: MPPPath): Boolean = filename.exists

    /**
     * Checks if the file is missing.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    fun missing(filename: MPPPath): Boolean = !exists(filename)

    /**
     * Checks if the file has changed.
     *
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun changed(filename: MPPPath): Boolean = FileHandling.hasChanged(
        filename,
        LinearExecutor.currentProject!!.executionDirectory /
            LinearExecutor.executionOptions.databaseName
    )

    /**
     * Checks if the file is unchanged.
     *
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun unchanged(filename: MPPPath): Boolean = !changed(filename)

    /**
     * Checks if the file contains the regex, based on its extension.
     *
     * @param extension The extension.
     * @param regex The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun found(extension: String, regex: String): Boolean = found(
        FileHandling.changeExtension(
            LinearExecutor.currentFile!!.path, extension
        ),
        regex
    )

    /**
     * Checks if the file contains the provided regex.
     *
     * @param file The file.
     * @param regex The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun found(file: MPPPath, regex: String): Boolean = try {
        val text = file.readText()
        regex.toRegex().containsMatchIn(text)
    } catch (exception: AraraIOException) {
        throw AraraException(
            LanguageController.messages.errorCheckRegexIoException(file.fileName),
            exception
        )
    }
}

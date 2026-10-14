// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.ClassLoading
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.ui.TerminalDialogs

/**
 * Implements the helper methods available to directive conditionals in
 * both dialects, such as file existence and change checks, dialogs and
 * class loading. The MVEL registry and the Lua conditional environment
 * bind the very same helpers. The registry binds them as reflective
 * method pointers invoked without an instance, so the public members
 * need to stay true statics - removing the annotations fails only at
 * the first conditional evaluation inside a rule.
 */
@Suppress("TooManyFunctions")
object ConditionalMethods {

    /**
     * Checks if the file exists according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
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
    @JvmStatic
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
    @JvmStatic
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
    @JvmStatic
    @Throws(AraraException::class)
    fun unchanged(extension: String): Boolean = !changed(extension)

    /**
     * Checks if the file exists.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    @JvmStatic
    fun exists(filename: MPPPath): Boolean = filename.exists

    /**
     * Checks if the file is missing.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    @JvmStatic
    fun missing(filename: MPPPath): Boolean = !exists(filename)

    /**
     * Checks if the file has changed.
     *
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun changed(filename: MPPPath): Boolean = FileHandling.hasChanged(
        filename,
        LinearExecutor.currentProject!!.workingDirectory /
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
    @JvmStatic
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
    @JvmStatic
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
    @JvmStatic
    @Throws(AraraException::class)
    fun found(file: MPPPath, regex: String): Boolean =
        checkRegex(file, regex)

    /**
     * Returns a file object based on the provided name.
     *
     * @param name The file name.
     * @return A file object.
     */
    @JvmStatic
    fun toFile(name: String): MPPPath = MPPPath(name)

    /**
     * Shows a message with options presented as an array of buttons.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    @JvmStatic
    fun showOptions(
        type: Int,
        title: String,
        text: String,
        vararg buttons: Any
    ): Int = TerminalDialogs.showOptions(type, title, text, buttons)

    /**
     * Shows a message with a text input.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    @JvmStatic
    fun showInput(
        type: Int,
        title: String,
        text: String
    ): String = TerminalDialogs.showInput(type, title, text)

    /**
     * Shows a message with options presented as a list of elements.
     *
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    @JvmStatic
    fun showList(
        type: Int,
        title: String,
        text: String,
        vararg elements: Any
    ): Int = TerminalDialogs.showList(type, title, text, elements)

    /**
     * Gets the file reference for the current directive. It is important to
     * observe that version 4.0 of arara replicates the directive when 'files'
     * is detected amongst the parameters, so each instance will have a
     * different reference.
     *
     * @return A file reference for the current directive.
     */
    @JvmStatic
    fun currentFile(): MPPPath = LinearExecutor.currentFile!!.path

    /**
     * Loads a class from the provided file, potentially a Java archive.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    @JvmStatic
    fun loadClass(file: MPPPath, name: String): Pair<ClassLoadingStatus, Class<*>> =
        if (LinearExecutor.executionOptions.executionMode == ExecutionMode.SAFE_RUN)
            throw SafeRunViolationException(
                LanguageController.messages.errorSafeModeClassLoading
            )
        else
            ClassLoading.loadClass(file, name)

    /**
     * Loads a class from the provided string reference, representing a file.
     *
     * @param ref String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    @JvmStatic
    fun loadClass(ref: String, name: String): Pair<ClassLoadingStatus, Class<*>> =
        loadClass(MPPPath(ref), name)

    /**
     * Loads a class from the provided file, instantiating it.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    @JvmStatic
    fun loadObject(file: MPPPath, name: String): Pair<ClassLoadingStatus, Any> =
        if (LinearExecutor.executionOptions.executionMode == ExecutionMode.SAFE_RUN)
            throw SafeRunViolationException(
                LanguageController.messages.errorSafeModeClassLoading
            )
        else
            ClassLoading.loadObject(file, name)

    /**
     * Loads a class from the provided string reference, instantiating it.
     *
     * @param ref String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    @JvmStatic
    fun loadObject(ref: String, name: String): Pair<ClassLoadingStatus, Any> =
        loadObject(MPPPath(ref), name)

    /**
     * Checks if the file contains the provided regex.
     *
     * As this reads the whole file, it should not be called on files
     * larger than two gigabytes.
     *
     * @param file The file.
     * @param regex The regex.
     * @return A boolean value indicating if the file contains the
     * provided regex.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun checkRegex(file: MPPPath, regex: String): Boolean {
        try {
            val text = file.readText()
            val pattern = regex.toRegex()
            return pattern.containsMatchIn(text)
        } catch (exception: AraraIOException) {
            throw AraraException(
                LanguageController.messages
                    .errorCheckRegexIoException(file.fileName),
                exception
            )
        }
    }
}

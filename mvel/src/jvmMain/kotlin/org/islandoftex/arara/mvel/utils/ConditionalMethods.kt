// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.session.ClassLoading
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.GUIDialogs

@Suppress("unused", "TooManyFunctions")
object ConditionalMethods {
    // the GUI generator
    private val dialogs = GUIDialogs(Session.userInterfaceOptions)

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
            MethodUtils.checkRegex(file, regex)

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
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    @JvmStatic
    @JvmOverloads
    fun showOptions(
        width: Int = dialogs.defaultWidth,
        type: Int,
        title: String,
        text: String,
        vararg buttons: Any
    ): Int = dialogs.showOptions(width, type, title, text, buttons)

    /**
     * Shows a message with a text input.
     *
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @return The string representing the input text.
     */
    @JvmStatic
    @JvmOverloads
    fun showInput(
        width: Int = dialogs.defaultWidth,
        type: Int,
        title: String,
        text: String
    ): String = dialogs.showInput(width, type, title, text)

    /**
     * Shows a message with options presented as a dropdown list of elements.
     *
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    @JvmStatic
    @JvmOverloads
    fun showDropdown(
        width: Int = dialogs.defaultWidth,
        type: Int,
        title: String,
        text: String,
        vararg elements: Any
    ): Int = dialogs.showDropdown(width, type, title, text, elements)

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
}

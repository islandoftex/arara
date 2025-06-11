// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.files.FileSearching
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.ui.GUIDialogs
import org.islandoftex.arara.core.ui.InputHandling
import org.islandoftex.arara.mvel.interpreter.AraraExceptionWithHeader
import kotlin.time.ExperimentalTime

@Suppress("unused", "TooManyFunctions")
object RuleMethods {
    // the GUI generator
    private val dialogs = GUIDialogs(Session.userInterfaceOptions)

    /**
     * Exit the application with status code.
     *
     * @param status The exit value, defaults to status OK, i.e. 0
     */
    @JvmStatic
    @JvmOverloads
    fun halt(status: Int = 0) {
        Session.put("arara:$originalFile:halt", status)
    }

    /**
     * Gets the original file.
     *
     * @return The original file.
     */
    @JvmStatic
    val originalFile: String
        get() = originalReference.fileName

    /**
     * Gets the original reference, i.e. the file arara has been called on or
     * the file arara extracted the directives from respectively.
     *
     * The reference is the file with the lowest priority as the main file will
     * always be compiled after all dependencies have been satisfied.
     *
     * @return The original reference.
     */
    @JvmStatic
    val originalReference: MPPPath
        get() =
            LinearExecutor.currentProject!!
                .files
                .minByOrNull { it.priority }!!
                .path

    /**
     * Trim spaces from the string.
     *
     * @param string The string.
     * @return A trimmed string.
     */
    @JvmStatic
    fun trimSpaces(string: String): String = string.trim()

    /**
     * Gets the basename.
     *
     * @param file The file.
     * @return The basename of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun getBasename(file: MPPPath): String =
        if (file.isRegularFile) {
            file.fileName.substringBeforeLast(".")
        } else {
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .ERROR_BASENAME_NOT_A_FILE
                    .format(file.fileName),
            )
        }

    /**
     * Gets the basename.
     *
     * @param filename The string.
     * @return The basename.
     */
    @JvmStatic
    fun getBasename(filename: String): String = getBasename(MPPPath(filename))

    /**
     * Gets the file type.
     *
     * @param file The provided file.
     * @return The file type.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun getFiletype(file: MPPPath): String =
        if (file.isRegularFile) {
            file.fileName.substringAfterLast(".")
        } else {
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .ERROR_FILETYPE_NOT_A_FILE
                    .format(file.fileName),
            )
        }

    /**
     * Gets the file type.
     *
     * @param filename The provided string.
     * @return The file type.
     */
    @JvmStatic
    fun getFiletype(filename: String): String = getFiletype(MPPPath(filename))

    /**
     * Attempts to retrieve an index-based element from a list of strings.
     *
     * @param list The list.
     * @param index The index.
     * @return The element in the position defined by the provided index or a
     * null value if the index is out of bounds.
     *
     */
    @JvmStatic
    @JvmOverloads
    fun getOrNull(
        list: List<String>,
        index: Int = 0,
    ) = list.getOrNull(index)

    /**
     * Replicates the pattern to each element of a list.
     *
     * @param pattern The pattern.
     * @param values The list.
     * @return A list of strings containing the pattern applied to the list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun replicatePattern(
        pattern: String,
        values: List<Any>,
    ): List<Any> = MethodUtils.replicateList(pattern, values)

    /**
     * Throws an exception.
     *
     * @param text The text to be thrown as the exception message.
     * @throws AraraException The exception to be thrown by this method.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun throwError(text: String): Unit = throw AraraExceptionWithHeader(text)

    /**
     * Gets the session.
     *
     * @return The session.
     */
    @JvmStatic
    fun getSession(): Session = Session

    /**
     * Build a string based on an array of objects.
     *
     * @param objects Array of objects.
     * @return A string built from the array.
     */
    @JvmStatic
    fun buildString(vararg objects: Any?): String =
        MethodUtils.generateString(objects.filterNotNull().toTypedArray())

    /**
     * Gets the command based on a list of strings.
     *
     * @param elements The list of strings.
     * @return A command.
     */
    @JvmStatic
    fun getCommand(elements: List<String>): Command =
        org.islandoftex.arara.core.session.Command(
            elements,
            LinearExecutor.currentProject!!.workingDirectory,
        )

    /**
     * Gets the command based on an array of objects.
     *
     * @param elements Array of objects.
     * @return A command.
     */
    @JvmStatic
    fun getCommand(vararg elements: Any?): Command =
        getCommand(
            InputHandling
                .flatten(elements.filterNotNull())
                .map { it.toString() }
                .filter { it.isNotEmpty() },
        )

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as string.
     *
     * @param path String path representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        path: String,
        vararg elements: Any?,
    ): Command =
        org.islandoftex.arara.core.session.Command(
            InputHandling
                .flatten(elements.filterNotNull())
                .map { it.toString() }
                .filter { it.isNotEmpty() },
            MPPPath(path),
        )

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as file.
     *
     * @param file File representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        file: MPPPath,
        vararg elements: Any?,
    ): Command =
        org.islandoftex.arara.core.session.Command(
            InputHandling
                .flatten(elements.filterNotNull())
                .map { it.toString() }
                .filter { it.isNotEmpty() },
            file,
        )

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as string.
     *
     * @param path String path representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        path: String,
        elements: List<String>,
    ): Command =
        org.islandoftex.arara.core.session.Command(
            elements,
            MPPPath(path),
        )

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as file.
     *
     * @param file File representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        file: MPPPath,
        elements: List<String>,
    ): Command =
        org.islandoftex.arara.core.session
            .Command(elements, file)

    /**
     * Checks if the execution is in verbose mode.
     *
     * @return A boolean value indicating if the execution is in verbose mode.
     */
    @OptIn(ExperimentalTime::class)
    @JvmStatic
    fun isVerboseMode(): Boolean = LinearExecutor.executionOptions.verbose

    /**
     * Shows the message.
     *
     * @param width Integer value, in pixels.
     * @param type Type of message.
     * @param title Title of the message.
     * @param text Text of the message.
     */
    @JvmStatic
    @JvmOverloads
    fun showMessage(
        width: Int = dialogs.defaultWidth,
        type: Int,
        title: String,
        text: String,
    ) = dialogs.showMessage(width, type, title, text)

    /**
     * Unsafely executes a system command from the underlying operating system
     * and returns a pair containing the exit status and the command output as a
     * string.
     *
     * @param command The system command to be executed.
     * @return A pair containing the exit status and the system command output
     * as a string.
     */
    @OptIn(ExperimentalTime::class)
    @JvmStatic
    fun unsafelyExecuteSystemCommand(command: Command): Pair<Int, String> =
        if (LinearExecutor.executionOptions.executionMode ==
            ExecutionMode.SAFE_RUN
        ) {
            throw SafeRunViolationException(
                "In safe mode, rules are not allowed to execute " +
                    "arbitrary system commands within a step of execution.",
            )
        } else {
            Environment.executeSystemCommand(command)
        }

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
    @JvmStatic
    fun listFilesByExtensions(
        directory: MPPPath,
        extensions: List<String>,
        recursive: Boolean,
    ): List<MPPPath> =
        FileSearching.listFilesByExtensions(
            directory,
            extensions,
            recursive,
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
    @JvmStatic
    fun listFilesByExtensions(
        path: String,
        extensions: List<String>,
        recursive: Boolean,
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
    @JvmStatic
    fun listFilesByPatterns(
        directory: MPPPath,
        patterns: List<String>,
        recursive: Boolean,
    ): List<MPPPath> =
        FileSearching.listFilesByPatterns(
            directory,
            patterns,
            recursive,
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
    @JvmStatic
    fun listFilesByPatterns(
        path: String,
        patterns: List<String>,
        recursive: Boolean,
    ): List<MPPPath> = listFilesByPatterns(MPPPath(path), patterns, recursive)

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param file The file.
     * @param text The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    @JvmStatic
    fun writeToFile(
        file: MPPPath,
        text: String,
        append: Boolean,
    ): Boolean = MethodUtils.writeToFile(file, text, append)

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param path The path.
     * @param text The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    @JvmStatic
    fun writeToFile(
        path: String,
        text: String,
        append: Boolean,
    ): Boolean = MethodUtils.writeToFile(MPPPath(path), text, append)

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param file The file.
     * @param lines The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    @JvmStatic
    fun writeToFile(
        file: MPPPath,
        lines: List<String>,
        append: Boolean,
    ): Boolean =
        MethodUtils.writeToFile(
            file,
            lines.joinToString(System.lineSeparator()),
            append,
        )

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param path The path.
     * @param lines The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    @JvmStatic
    fun writeToFile(
        path: String,
        lines: List<String>,
        append: Boolean,
    ): Boolean =
        MethodUtils.writeToFile(
            MPPPath(path),
            lines.joinToString(System.lineSeparator()),
            append,
        )

    /**
     * Reads the provided file into a list of strings.
     *
     * @param file The file.
     * @return A list of strings.
     */
    @JvmStatic
    fun readFromFile(file: MPPPath): List<String> =
        try {
            file.readLines()
        } catch (_: AraraIOException) {
            emptyList()
        }

    /**
     * Reads the provided file into a list of strings.
     *
     * @param path The path.
     * @return A list of strings.
     */
    @JvmStatic
    fun readFromFile(path: String): List<String> = readFromFile(MPPPath(path))

    /**
     * Checks whether a directory is under the project directory.
     *
     * @param directory The directory to be inspected.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isSubdirectory(directory: MPPPath): Boolean =
        FileHandling.isSubDirectory(
            directory,
            LinearExecutor.currentProject!!.workingDirectory,
        )

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    @JvmStatic
    fun isEmpty(string: String): Boolean = string.isEmpty()

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    @JvmStatic
    fun isNotEmpty(string: String): Boolean = !isEmpty(string)

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    @JvmStatic
    @JvmOverloads
    fun isEmpty(
        string: String,
        yes: Any?,
        no: Any? = "",
    ): Any? = if (isEmpty(string)) yes else no

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    @JvmStatic
    @JvmOverloads
    fun isNotEmpty(
        string: String,
        yes: Any?,
        no: Any? = "",
    ): Any? = if (isNotEmpty(string)) yes else no

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isTrue(string: String): Boolean =
        !isEmpty(string) && InputHandling.checkBoolean(string)

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isFalse(string: String): Boolean =
        !isEmpty(string) && !InputHandling.checkBoolean(string)

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(AraraException::class)
    fun isTrue(
        string: String,
        yes: Any?,
        no: Any? = "",
    ): Any? = if (isTrue(string)) yes else no

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(AraraException::class)
    fun isFalse(
        string: String,
        yes: Any?,
        no: Any? = "",
    ): Any? = if (isFalse(string)) yes else no

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isTrue(
        string: String,
        yes: Any?,
        no: Any?,
        fallback: Any?,
    ): Any? =
        if (isEmpty(string)) {
            fallback
        } else if (isTrue(string)) {
            yes
        } else {
            no
        }

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isFalse(
        string: String,
        yes: Any?,
        no: Any?,
        fallback: Any?,
    ): Any? =
        if (isEmpty(string)) {
            fallback
        } else if (isFalse(string)) {
            yes
        } else {
            no
        }

    /**
     * Checks if the expression resolves to true.
     *
     * @param value The expression.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    @JvmStatic
    @JvmOverloads
    fun isTrue(
        value: Boolean,
        yes: Any?,
        no: Any? = "",
    ): Any? = if (value) yes else no

    /**
     * Checks if the expression resolves to false.
     *
     * @param value The expression.
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     */
    @JvmStatic
    @JvmOverloads
    fun isFalse(
        value: Boolean,
        yes: Any?,
        no: Any? = "",
    ): Any? = if (!value) yes else no

    /**
     * Checks if the object is an instance of the provided class.
     *
     * @param clazz The class.
     * @param obj The object.
     * @return A boolean value.
     */
    @JvmStatic
    fun checkClass(
        clazz: Class<*>,
        obj: Any?,
    ): Boolean = clazz.isInstance(obj)

    /**
     * Checks if the object is a string.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    @JvmStatic
    fun isString(obj: Any?): Boolean =
        checkClass(java.lang.String::class.java, obj)

    /**
     * Checks if the object is a list.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    @JvmStatic
    fun isList(obj: Any?): Boolean = checkClass(java.util.List::class.java, obj)

    /**
     * Checks if the object is a map.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    @JvmStatic
    fun isMap(obj: Any?): Boolean = checkClass(java.util.Map::class.java, obj)

    /**
     * Checks if the object is a boolean.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    @JvmStatic
    fun isBoolean(obj: Any?): Boolean =
        checkClass(java.lang.Boolean::class.java, obj)

    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    @JvmStatic
    fun isOnPath(command: String): Boolean = Environment.isOnPath(command)

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isWindows(): Boolean =
        Environment.checkOS(Environment.SupportedOS.WINDOWS)

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isCygwin(): Boolean =
        Environment.checkOS(Environment.SupportedOS.CYGWIN)

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isLinux(): Boolean = Environment.checkOS(Environment.SupportedOS.LINUX)

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isMac(): Boolean = Environment.checkOS(Environment.SupportedOS.MACOS)

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isUnix(): Boolean = Environment.checkOS(Environment.SupportedOS.UNIX)

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isWindows(
        yes: Any?,
        no: Any?,
    ): Any? =
        if (Environment.checkOS(Environment.SupportedOS.WINDOWS)) yes else no

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isCygwin(
        yes: Any?,
        no: Any?,
    ): Any? =
        if (Environment.checkOS(Environment.SupportedOS.CYGWIN)) yes else no

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isLinux(
        yes: Any?,
        no: Any?,
    ): Any? =
        if (Environment.checkOS(Environment.SupportedOS.LINUX)) yes else no

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isMac(
        yes: Any?,
        no: Any?,
    ): Any? =
        if (Environment.checkOS(Environment.SupportedOS.MACOS)) yes else no

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isUnix(
        yes: Any?,
        no: Any?,
    ): Any? = if (Environment.checkOS(Environment.SupportedOS.UNIX)) yes else no
}

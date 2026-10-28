// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.session.Session

/**
 * Compatibility-only JVM static entry points for existing rules and reflective MVEL bindings.
 * Implementations live in the focused helper domains linked by each method.
 */
// The flat static ABI is intentional; this facade owns no execution state or business logic.
@Suppress("TooManyFunctions")
object RuleMethods {
    /** Compatibility entry point for [SessionRuleHelpers.halt]. */
    @JvmStatic
    @JvmOverloads
    fun halt(status: Int = 0) =
        SessionRuleHelpers.halt(status)

    /** Compatibility entry point for [SessionRuleHelpers.originalFile]. */
    @JvmStatic
    val originalFile: String
        get() = SessionRuleHelpers.originalFile

    /** Compatibility entry point for [SessionRuleHelpers.originalReference]. */
    @JvmStatic
    val originalReference: MPPPath
        get() = SessionRuleHelpers.originalReference

    /** Compatibility entry point for [StringRuleHelpers.trimSpaces]. */
    @JvmStatic
    fun trimSpaces(string: String): String =
        StringRuleHelpers.trimSpaces(string)

    /** Compatibility entry point for [PathRuleHelpers.getBasename]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun getBasename(file: MPPPath): String =
        PathRuleHelpers.getBasename(file)

    /** Compatibility entry point for [PathRuleHelpers.getBasename]. */
    @JvmStatic
    fun getBasename(filename: String): String =
        PathRuleHelpers.getBasename(filename)

    /** Compatibility entry point for [PathRuleHelpers.getFiletype]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun getFiletype(file: MPPPath): String =
        PathRuleHelpers.getFiletype(file)

    /** Compatibility entry point for [PathRuleHelpers.getFiletype]. */
    @JvmStatic
    fun getFiletype(filename: String): String =
        PathRuleHelpers.getFiletype(filename)

    /** Compatibility entry point for [StringRuleHelpers.getOrNull]. */
    @JvmStatic
    @JvmOverloads
    fun getOrNull(list: List<String>, index: Int = 0) =
        StringRuleHelpers.getOrNull(list, index)

    /** Compatibility entry point for [StringRuleHelpers.replicatePattern]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun replicatePattern(pattern: String, values: List<Any>): List<Any> =
        StringRuleHelpers.replicatePattern(pattern, values)

    /** Compatibility entry point for [SessionRuleHelpers.throwError]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun throwError(text: String) =
        SessionRuleHelpers.throwError(text)

    /** Compatibility entry point for [SessionRuleHelpers.getSession]. */
    @JvmStatic
    fun getSession(): Session =
        SessionRuleHelpers.getSession()

    /** Compatibility entry point for [StringRuleHelpers.buildString]. */
    @JvmStatic
    fun buildString(vararg objects: Any?): String =
        StringRuleHelpers.buildString(objects.toList())

    /** Compatibility entry point for [CommandRuleHelpers.getCommand]. */
    @JvmStatic
    fun getCommand(elements: List<String>): Command =
        CommandRuleHelpers.getCommand(elements)

    /** Compatibility entry point for [CommandRuleHelpers.commandFromValues]. */
    @JvmStatic
    fun getCommand(vararg elements: Any?): Command =
        CommandRuleHelpers.commandFromValues(elements.toList())

    /** Compatibility entry point for [CommandRuleHelpers.commandFromValues]. */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        path: String,
        vararg elements: Any?
    ): Command =
        CommandRuleHelpers.commandFromValues(path, elements.toList())

    /** Compatibility entry point for [CommandRuleHelpers.commandFromValues]. */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        file: MPPPath,
        vararg elements: Any?
    ): Command =
        CommandRuleHelpers.commandFromValues(file, elements.toList())

    /** Compatibility entry point for [CommandRuleHelpers.getCommandWithWorkingDirectory]. */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        path: String,
        elements: List<String>
    ): Command =
        CommandRuleHelpers.getCommandWithWorkingDirectory(path, elements)

    /** Compatibility entry point for [CommandRuleHelpers.getCommandWithWorkingDirectory]. */
    @JvmStatic
    fun getCommandWithWorkingDirectory(
        file: MPPPath,
        elements: List<String>
    ): Command =
        CommandRuleHelpers.getCommandWithWorkingDirectory(file, elements)

    /** Compatibility entry point for [SessionRuleHelpers.isVerboseMode]. */
    @JvmStatic
    fun isVerboseMode(): Boolean =
        SessionRuleHelpers.isVerboseMode()

    /** Compatibility entry point for [DialogRuleHelpers.showMessage]. */
    @JvmStatic
    fun showMessage(
        type: Int,
        title: String,
        text: String
    ) =
        DialogRuleHelpers.showMessage(type, title, text)

    /** Compatibility entry point for [CommandRuleHelpers.unsafelyExecuteSystemCommand]. */
    @JvmStatic
    fun unsafelyExecuteSystemCommand(command: Command): Pair<Int, String> =
        CommandRuleHelpers.unsafelyExecuteSystemCommand(command)

    /** Compatibility entry point for [PathRuleHelpers.listFilesByExtensions]. */
    @JvmStatic
    fun listFilesByExtensions(
        directory: MPPPath,
        extensions: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        PathRuleHelpers.listFilesByExtensions(directory, extensions, recursive)

    /** Compatibility entry point for [PathRuleHelpers.listFilesByExtensions]. */
    @JvmStatic
    fun listFilesByExtensions(
        path: String,
        extensions: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        PathRuleHelpers.listFilesByExtensions(path, extensions, recursive)

    /** Compatibility entry point for [PathRuleHelpers.listFilesByPatterns]. */
    @JvmStatic
    fun listFilesByPatterns(
        directory: MPPPath,
        patterns: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        PathRuleHelpers.listFilesByPatterns(directory, patterns, recursive)

    /** Compatibility entry point for [PathRuleHelpers.listFilesByPatterns]. */
    @JvmStatic
    fun listFilesByPatterns(
        path: String,
        patterns: List<String>,
        recursive: Boolean
    ): List<MPPPath> =
        PathRuleHelpers.listFilesByPatterns(path, patterns, recursive)

    /** Compatibility entry point for [FileRuleHelpers.writeToFile]. */
    @JvmStatic
    fun writeToFile(file: MPPPath, text: String, append: Boolean): Boolean =
        FileRuleHelpers.writeToFile(file, text, append)

    /** Compatibility entry point for [FileRuleHelpers.writeToFile]. */
    @JvmStatic
    fun writeToFile(path: String, text: String, append: Boolean): Boolean =
        FileRuleHelpers.writeToFile(path, text, append)

    /** Compatibility entry point for [FileRuleHelpers.writeToFile]. */
    @JvmStatic
    fun writeToFile(file: MPPPath, lines: List<String>, append: Boolean): Boolean =
        FileRuleHelpers.writeToFile(file, lines, append)

    /** Compatibility entry point for [FileRuleHelpers.writeToFile]. */
    @JvmStatic
    fun writeToFile(path: String, lines: List<String>, append: Boolean): Boolean =
        FileRuleHelpers.writeToFile(path, lines, append)

    /** Compatibility entry point for [FileRuleHelpers.readFromFile]. */
    @JvmStatic
    fun readFromFile(file: MPPPath): List<String> =
        FileRuleHelpers.readFromFile(file)

    /** Compatibility entry point for [FileRuleHelpers.readFromFile]. */
    @JvmStatic
    fun readFromFile(path: String): List<String> =
        FileRuleHelpers.readFromFile(path)

    /** Compatibility entry point for [PathRuleHelpers.isSubdirectory]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isSubdirectory(directory: MPPPath): Boolean =
        PathRuleHelpers.isSubdirectory(directory)

    /** Compatibility entry point for [EmptyRuleHelpers.isEmpty]. */
    @JvmStatic
    fun isEmpty(string: String): Boolean =
        EmptyRuleHelpers.isEmpty(string)

    /** Compatibility entry point for [EmptyRuleHelpers.isNotEmpty]. */
    @JvmStatic
    fun isNotEmpty(string: String): Boolean =
        EmptyRuleHelpers.isNotEmpty(string)

    /** Compatibility entry point for [EmptyRuleHelpers.isEmpty]. */
    @JvmStatic
    @JvmOverloads
    fun isEmpty(string: String, yes: Any?, no: Any? = ""): Any? =
        EmptyRuleHelpers.isEmpty(string, yes, no)

    /** Compatibility entry point for [EmptyRuleHelpers.isNotEmpty]. */
    @JvmStatic
    @JvmOverloads
    fun isNotEmpty(string: String, yes: Any?, no: Any? = ""): Any? =
        EmptyRuleHelpers.isNotEmpty(string, yes, no)

    /** Compatibility entry point for [BooleanRuleHelpers.isTrue]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isTrue(string: String): Boolean =
        BooleanRuleHelpers.isTrue(string)

    /** Compatibility entry point for [BooleanRuleHelpers.isFalse]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isFalse(string: String): Boolean =
        BooleanRuleHelpers.isFalse(string)

    /** Compatibility entry point for [BooleanRuleHelpers.isTrue]. */
    @JvmStatic
    @JvmOverloads
    @Throws(AraraException::class)
    fun isTrue(string: String, yes: Any?, no: Any? = ""): Any? =
        BooleanRuleHelpers.isTrue(string, yes, no)

    /** Compatibility entry point for [BooleanRuleHelpers.isFalse]. */
    @JvmStatic
    @JvmOverloads
    @Throws(AraraException::class)
    fun isFalse(string: String, yes: Any?, no: Any? = ""): Any? =
        BooleanRuleHelpers.isFalse(string, yes, no)

    /** Compatibility entry point for [BooleanRuleHelpers.isTrue]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isTrue(string: String, yes: Any?, no: Any?, fallback: Any?): Any? =
        BooleanRuleHelpers.isTrue(string, yes, no, fallback)

    /** Compatibility entry point for [BooleanRuleHelpers.isFalse]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isFalse(string: String, yes: Any?, no: Any?, fallback: Any?): Any? =
        BooleanRuleHelpers.isFalse(string, yes, no, fallback)

    /** Compatibility entry point for [BooleanRuleHelpers.isTrue]. */
    @JvmStatic
    @JvmOverloads
    fun isTrue(value: Boolean, yes: Any?, no: Any? = ""): Any? =
        BooleanRuleHelpers.isTrue(value, yes, no)

    /** Compatibility entry point for [BooleanRuleHelpers.isFalse]. */
    @JvmStatic
    @JvmOverloads
    fun isFalse(value: Boolean, yes: Any?, no: Any? = ""): Any? =
        BooleanRuleHelpers.isFalse(value, yes, no)

    /** Compatibility entry point for [TypeRuleHelpers.checkClass]. */
    @JvmStatic
    fun checkClass(clazz: Class<*>, obj: Any?): Boolean =
        TypeRuleHelpers.checkClass(clazz, obj)

    /** Compatibility entry point for [TypeRuleHelpers.isString]. */
    @JvmStatic
    fun isString(obj: Any?): Boolean =
        TypeRuleHelpers.isString(obj)

    /** Compatibility entry point for [TypeRuleHelpers.isList]. */
    @JvmStatic
    fun isList(obj: Any?): Boolean =
        TypeRuleHelpers.isList(obj)

    /** Compatibility entry point for [TypeRuleHelpers.isMap]. */
    @JvmStatic
    fun isMap(obj: Any?): Boolean =
        TypeRuleHelpers.isMap(obj)

    /** Compatibility entry point for [TypeRuleHelpers.isBoolean]. */
    @JvmStatic
    fun isBoolean(obj: Any?): Boolean =
        TypeRuleHelpers.isBoolean(obj)

    /** Compatibility entry point for [CommandRuleHelpers.isOnPath]. */
    @JvmStatic
    fun isOnPath(command: String): Boolean =
        CommandRuleHelpers.isOnPath(command)

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isWindows]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isWindows(): Boolean =
        OperatingSystemRuleHelpers.isWindows()

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isCygwin]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isCygwin(): Boolean =
        OperatingSystemRuleHelpers.isCygwin()

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isLinux]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isLinux(): Boolean =
        OperatingSystemRuleHelpers.isLinux()

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isMac]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isMac(): Boolean =
        OperatingSystemRuleHelpers.isMac()

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isUnix]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isUnix(): Boolean =
        OperatingSystemRuleHelpers.isUnix()

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isWindows]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isWindows(yes: Any?, no: Any?): Any? =
        OperatingSystemRuleHelpers.isWindows(yes, no)

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isCygwin]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isCygwin(yes: Any?, no: Any?): Any? =
        OperatingSystemRuleHelpers.isCygwin(yes, no)

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isLinux]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isLinux(yes: Any?, no: Any?): Any? =
        OperatingSystemRuleHelpers.isLinux(yes, no)

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isMac]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isMac(yes: Any?, no: Any?): Any? =
        OperatingSystemRuleHelpers.isMac(yes, no)

    /** Compatibility entry point for [OperatingSystemRuleHelpers.isUnix]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun isUnix(yes: Any?, no: Any?): Any? =
        OperatingSystemRuleHelpers.isUnix(yes, no)
}

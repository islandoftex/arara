// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus

/**
 * Compatibility-only JVM static entry points for existing rules and reflective MVEL bindings.
 * Implementations live in the focused helper domains linked by each method.
 */
// The flat static ABI is intentional; this facade owns no execution state or business logic.
@Suppress("TooManyFunctions")
object ConditionalMethods {
    /** Compatibility entry point for [ConditionalFileHelpers.exists]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun exists(extension: String): Boolean =
        ConditionalFileHelpers.exists(extension)

    /** Compatibility entry point for [ConditionalFileHelpers.missing]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun missing(extension: String): Boolean =
        ConditionalFileHelpers.missing(extension)

    /** Compatibility entry point for [ConditionalFileHelpers.changed]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun changed(extension: String): Boolean =
        ConditionalFileHelpers.changed(extension)

    /** Compatibility entry point for [ConditionalFileHelpers.unchanged]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun unchanged(extension: String): Boolean =
        ConditionalFileHelpers.unchanged(extension)

    /** Compatibility entry point for [ConditionalFileHelpers.exists]. */
    @JvmStatic
    fun exists(filename: MPPPath): Boolean =
        ConditionalFileHelpers.exists(filename)

    /** Compatibility entry point for [ConditionalFileHelpers.missing]. */
    @JvmStatic
    fun missing(filename: MPPPath): Boolean =
        ConditionalFileHelpers.missing(filename)

    /** Compatibility entry point for [ConditionalFileHelpers.changed]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun changed(filename: MPPPath): Boolean =
        ConditionalFileHelpers.changed(filename)

    /** Compatibility entry point for [ConditionalFileHelpers.unchanged]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun unchanged(filename: MPPPath): Boolean =
        ConditionalFileHelpers.unchanged(filename)

    /** Compatibility entry point for [ConditionalFileHelpers.found]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun found(extension: String, regex: String): Boolean =
        ConditionalFileHelpers.found(extension, regex)

    /** Compatibility entry point for [ConditionalFileHelpers.found]. */
    @JvmStatic
    @Throws(AraraException::class)
    fun found(file: MPPPath, regex: String): Boolean =
        ConditionalFileHelpers.found(file, regex)

    /** Compatibility entry point for [ConditionalReferenceHelpers.toFile]. */
    @JvmStatic
    fun toFile(name: String): MPPPath =
        ConditionalReferenceHelpers.toFile(name)

    /** Compatibility entry point for [ConditionalDialogHelpers.showOptions]. */
    @JvmStatic
    fun showOptions(
        type: Int,
        title: String,
        text: String,
        vararg buttons: Any
    ): Int =
        ConditionalDialogHelpers.showOptions(type, title, text, buttons)

    /** Compatibility entry point for [ConditionalDialogHelpers.showInput]. */
    @JvmStatic
    fun showInput(
        type: Int,
        title: String,
        text: String
    ): String =
        ConditionalDialogHelpers.showInput(type, title, text)

    /** Compatibility entry point for [ConditionalDialogHelpers.showList]. */
    @JvmStatic
    fun showList(
        type: Int,
        title: String,
        text: String,
        vararg elements: Any
    ): Int =
        ConditionalDialogHelpers.showList(type, title, text, elements)

    /** Compatibility entry point for [ConditionalReferenceHelpers.currentFile]. */
    @JvmStatic
    fun currentFile(): MPPPath =
        ConditionalReferenceHelpers.currentFile()

    /** Compatibility entry point for [ConditionalLoadingHelpers.loadClass]. */
    @JvmStatic
    fun loadClass(file: MPPPath, name: String): Pair<ClassLoadingStatus, Class<*>> =
        ConditionalLoadingHelpers.loadClass(file, name)

    /** Compatibility entry point for [ConditionalLoadingHelpers.loadClass]. */
    @JvmStatic
    fun loadClass(ref: String, name: String): Pair<ClassLoadingStatus, Class<*>> =
        ConditionalLoadingHelpers.loadClass(ref, name)

    /** Compatibility entry point for [ConditionalLoadingHelpers.loadObject]. */
    @JvmStatic
    fun loadObject(file: MPPPath, name: String): Pair<ClassLoadingStatus, Any> =
        ConditionalLoadingHelpers.loadObject(file, name)

    /** Compatibility entry point for [ConditionalLoadingHelpers.loadObject]. */
    @JvmStatic
    fun loadObject(ref: String, name: String): Pair<ClassLoadingStatus, Any> =
        ConditionalLoadingHelpers.loadObject(ref, name)
}

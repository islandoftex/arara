// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.ClassLoading
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.core.session.LinearExecutor

/** Guarded class and object loading for directive conditionals. */
object ConditionalLoadingHelpers {
    /**
     * Loads a class from the provided file, potentially a Java archive.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    fun loadClass(file: MPPPath, name: String): Pair<ClassLoadingStatus, Class<*>> {
        ensureClassLoadingAllowed()
        return ClassLoading.loadClass(file, name)
    }

    /**
     * Loads a class from the provided string reference, representing a file.
     *
     * @param ref String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    fun loadClass(ref: String, name: String): Pair<ClassLoadingStatus, Class<*>> =
        loadClass(MPPPath(ref), name)

    /**
     * Loads a class from the provided file, instantiating it.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    fun loadObject(file: MPPPath, name: String): Pair<ClassLoadingStatus, Any> {
        ensureClassLoadingAllowed()
        return ClassLoading.loadObject(file, name)
    }

    /**
     * Loads a class from the provided string reference, instantiating it.
     *
     * @param ref String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    fun loadObject(ref: String, name: String): Pair<ClassLoadingStatus, Any> =
        loadObject(MPPPath(ref), name)

    private fun ensureClassLoadingAllowed() {
        if (LinearExecutor.executionOptions.executionMode == ExecutionMode.SAFE_RUN) {
            throw SafeRunViolationException(LanguageController.messages.errorSafeModeClassLoading)
        }
    }
}

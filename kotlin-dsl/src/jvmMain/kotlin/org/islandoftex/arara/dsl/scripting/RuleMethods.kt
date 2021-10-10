// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.scripting

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

@Suppress("unused", "TooManyFunctions")
object RuleMethods {
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
        get() = LinearExecutor.currentProject!!.files
            .maxByOrNull { it.priority }!!
            .path
}

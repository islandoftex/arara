// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/** Session state, current references and explicit execution control for rules. */
object SessionRuleHelpers {
    /**
     * Exit the application with status code.
     *
     * @param status The exit value, defaults to status OK, i.e. 0
     */
    fun halt(status: Int = 0) {
        HaltRequest.request(status)
    }

    /**
     * Gets the original file.
     *
     * @return The original file.
     */
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
    val originalReference: MPPPath
        get() = LinearExecutor.currentProject!!.files
            .minByOrNull { it.priority }!!
            .path

    /**
     * Throws an exception.
     *
     * @param text The text to be thrown as the exception message.
     * @throws AraraException The exception to be thrown by this method.
     */
    @Throws(AraraException::class)
    fun throwError(text: String) {
        throw AraraExceptionWithHeader(text)
    }

    /**
     * Gets the session.
     *
     * @return The session.
     */
    fun getSession(): Session = Session

    /**
     * Checks if the execution is in verbose mode.
     *
     * @return A boolean value indicating if the execution is in verbose mode.
     */
    fun isVerboseMode(): Boolean = LinearExecutor.executionOptions.verbose
}

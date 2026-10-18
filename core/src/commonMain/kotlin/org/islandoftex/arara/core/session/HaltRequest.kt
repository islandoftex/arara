// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

/** A halt belongs to the current file, independently of its name or project. */
object HaltRequest {
    /** Requested status, including zero for a successful halt. */
    var exitCode: Int? = null
        private set

    /** Ask the executor to finish the current file with [code]. */
    fun request(code: Int) { exitCode = code }

    /** Release the request when entering or leaving a file. */
    fun clear() { exitCode = null }
}

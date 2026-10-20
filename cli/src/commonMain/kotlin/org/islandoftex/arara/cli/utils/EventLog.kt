// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

/** Platform logging preserves human-readable messages and typed event fields. */
internal expect object EventLog {
    val isInfoEnabled: Boolean
    val isErrorEnabled: Boolean

    fun info(event: String, message: String, fields: Map<String, Any>)
    fun error(event: String, message: String, fields: Map<String, Any>, cause: Throwable? = null)
}

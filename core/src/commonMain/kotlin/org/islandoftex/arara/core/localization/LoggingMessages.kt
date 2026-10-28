// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for logging messages. */
interface LoggingMessages {
    /** Unknown diagnostic log format; argument: supplied format. */
    fun errorLogFormatUnknown(value: String): String

    /** Unknown diagnostic severity; argument: supplied level. */
    fun errorLogLevelUnknown(value: String): String

    /** Unknown log retention policy; argument: supplied policy. */
    fun errorLogRetentionUnknown(value: String): String

    /** Failure to discard successful diagnostic output; argument: filesystem error. */
    fun errorLogCleanupFailed(reason: String): String
}

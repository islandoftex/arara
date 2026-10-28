// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for logging messages. */
public interface LoggingMessages {
    /** Unknown diagnostic log format; argument: supplied format. */
    public fun errorLogFormatUnknown(value: String): String

    /** Unknown diagnostic severity; argument: supplied level. */
    public fun errorLogLevelUnknown(value: String): String

    /** Unknown log retention policy; argument: supplied policy. */
    public fun errorLogRetentionUnknown(value: String): String

    /** Failure to discard successful diagnostic output; argument: filesystem error. */
    public fun errorLogCleanupFailed(reason: String): String
}

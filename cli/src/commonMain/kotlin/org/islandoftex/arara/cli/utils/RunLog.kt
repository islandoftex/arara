// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.logging.CommandExecutionLog
import org.islandoftex.arara.api.logging.RuleExecutionLog
import org.islandoftex.arara.api.logging.RunLifecycleLog
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.session.Session

/** Stable entry points composed from focused operation domains. */
object RunLog : RunLifecycleLog by runLifecycleLog,
    RuleExecutionLog by ruleExecutionLog,
    CommandExecutionLog by commandExecutionLog {

    internal val applicationPath: String
        get() = try {
            ConfigurationUtils.applicationPath.toString()
        } catch (_: AraraException) {
            "[unknown application path]"
        }

    internal fun separator(): String = "-".repeat(Session.userInterfaceOptions.terminalOutputWidth)

    internal fun outputSeparator(message: String): String =
        " $message ".center(Session.userInterfaceOptions.terminalOutputWidth, '-')
}

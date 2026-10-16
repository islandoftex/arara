// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.configuration.PreambleState
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/** Configuration inherited by each project and returned to an embedding host after a CLI run. */
internal data class ConfigurationSnapshot(
    val execution: ExecutionOptions,
    val logging: LoggingOptions,
    val ui: UserInterfaceOptions,
    val preambles: Map<String, String>,
    val defaultPreamble: String?,
    val prepend: Boolean
) {
    fun restore(configureLogging: Boolean = true) {
        LinearExecutor.executionOptions = execution
        Session.loggingOptions = logging
        Session.userInterfaceOptions = ui
        PreambleState.preambles.clear()
        PreambleState.preambles.putAll(preambles)
        PreambleState.defaultPreamble = defaultPreamble
        PreambleState.prependPreambleIfDirectivesGiven = prepend
        LanguageController.loadMessagesFor(ui.locale)
        if (configureLogging) LoggingUtils.setupLogging(logging)
    }

    companion object {
        fun capture(): ConfigurationSnapshot = ConfigurationSnapshot(
            LinearExecutor.executionOptions, Session.loggingOptions, Session.userInterfaceOptions,
            PreambleState.preambles.toMap(), PreambleState.defaultPreamble, PreambleState.prependPreambleIfDirectivesGiven
        )
    }
}

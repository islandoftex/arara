// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.plain

import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/** State shared only by the components of one plain renderer. */
internal class PlainRenderState {
    /**
     * The length of the longest result match as integer.
     */
    val longestMatch: Int
        get() = maxOf(
            LanguageController.messages.infoLabelOnSuccess.length,
            LanguageController.messages.infoLabelOnFailure.length,
            LanguageController.messages.infoLabelOnError.length
        )

    val shortenedLongestMatch = SHORT_LABEL_WIDTH

    var displayLine = false
    var displayResult = false
    var displayRolling = false
    var displayException = false
    var displaySummary = false

    /**
     * Checks if the execution is in dry-run mode.
     */
    val isDryRunMode: Boolean
        get() = LinearExecutor.executionOptions.executionMode ==
            ExecutionMode.DRY_RUN

    /**
     * Checks if the execution is in verbose mode.
     */
    val isVerboseMode: Boolean
        get() = LinearExecutor.executionOptions.verbose

    /**
     * The number of columns available for output.
     */
    val outputWidth: Int
        get() = Session.userInterfaceOptions.terminalOutputWidth

    companion object { const val SHORT_LABEL_WIDTH = 10 }
}

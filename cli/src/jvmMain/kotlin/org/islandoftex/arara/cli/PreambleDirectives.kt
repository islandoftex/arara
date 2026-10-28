// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.configuration.PreambleState
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.Directives

/**
 * Prepend potential preamble lines to the given directives.
 *
 * If there is no preamble given on command-line, returns the [directives],
 * otherwise checks existence of the preamble and returns the processed
 * preamble for a file of [fileType] prepended to [directives].
 */
internal fun prependPreambleDirectives(
    selectedPreamble: String?,
    fileType: FileType,
    directives: List<Directive>
): List<Directive> {
    val resolvedPreamble = selectedPreamble ?: PreambleState.defaultPreamble
    if (resolvedPreamble != null && resolvedPreamble !in PreambleState.preambles) {
        throw AraraException(
            LanguageController.messages
                .errorParserInvalidPreamble(resolvedPreamble)
        )
    }
    val allDirectives = resolvedPreamble
        ?.takeIf { PreambleState.preambles.containsKey(it) }
        ?.let { preambleName ->
            Directives.extractDirectives(
                PreambleState.preambles.getValue(preambleName)
                    .lines()
                    .filterNot { it.isEmpty() },
                true,
                fileType
            ).plus(directives)
        } ?: directives
    if (allDirectives.isEmpty())
        throw AraraException(
            LanguageController
                .messages.errorValidateNoDirectivesFound
        )
    return allDirectives
}

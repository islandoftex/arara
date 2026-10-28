// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.localization.BrazilianPortugueseMessages
import org.islandoftex.arara.core.localization.DutchMessages
import org.islandoftex.arara.core.localization.EnglishMessages
import org.islandoftex.arara.core.localization.FrenchMessages
import org.islandoftex.arara.core.localization.GermanMessages
import org.islandoftex.arara.core.localization.ItalianMessages
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.localization.NorfolkMessages

class ProjectDiagnosticsTest {
    @Test
    fun graphErrorsUseTheSelectedLanguageAndLiteralProjectNames() {
        val previous = LanguageController.messages
        val firstName = "A-${'$'}1-%s-\\"
        val first = Project(firstName, MPPPath("."), emptySet(), setOf("B"))
        val second = Project("B", MPPPath("."), emptySet(), setOf(firstName))
        val downstream = Project("Downstream", MPPPath("."), emptySet(), setOf("B"))
        try {
            for (messages in listOf(EnglishMessages, GermanMessages, ItalianMessages, DutchMessages,
                BrazilianPortugueseMessages, FrenchMessages, NorfolkMessages)) {
                LanguageController.messages = messages
                val cycle = assertFailsWith<AraraException> {
                    ProjectValidation.executionOrder(listOf(downstream, first, second))
                }
                assertEquals(messages.errorProjectCycle("$firstName, B"), cycle.message)
                val duplicate = assertFailsWith<AraraException> {
                    ProjectValidation.executionOrder(listOf(first, first))
                }
                assertEquals(messages.errorProjectDuplicateNames(firstName), duplicate.message)
                val missing = assertFailsWith<AraraException> { ProjectValidation.executionOrder(listOf(first)) }
                assertEquals(messages.errorProjectUnknownDependency(firstName, "B"), missing.message)
            }
        } finally {
            LanguageController.messages = previous
        }
    }
}

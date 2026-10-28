// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController

class RuleFilesTest {
    @Test
    fun unsupportedRuleFormatsReportAnAraraException() {
        val file = MPPPath("probe.kts")
        val failure = assertFailsWith<AraraException> { RuleFiles.parseRule(file, "probe") }
        assertEquals(LanguageController.messages.errorParseRuleUnsupportedFormat("probe", file.toString()), failure.message)
    }
}

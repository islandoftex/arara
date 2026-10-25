// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue
import org.islandoftex.arara.api.configuration.LogFormat
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Session

class RunLogLanguageTest {
    @Test
    fun logLabelsAndDecimalFormattingDoNotFollowTheConsoleLocale() {
        val snapshot = ConfigurationSnapshot.capture()
        val root = Files.createTempDirectory("arara log languages ").toMPPPath()
        try {
            for (language in listOf("en", "de", "it", "nl", "pt-BR", "en-QN", "fr")) {
                val locale = MPPLocale(language)
                Session.userInterfaceOptions = UserInterfaceOptions(locale)
                LanguageController.loadMessagesFor(locale)
                val log = root.resolve("$language.log")
                LoggingUtils.init()
                LoggingUtils.setupLogging(LoggingOptions(LogFormat.TEXT, false, log))
                RunLog.entry("literal %s", "script message")
                RunLog.taskResult(false)
                RunLog.executionTime(1.25)
                RunLog.exception("literal exception %s", "literal cause")
                LoggingUtils.init()
                val text = log.readText()
                assertTrue("Interpreting task \"script message\" from rule \"literal %s\"" in text)
                assertTrue("Task result: FAILURE" in text)
                assertTrue("Elapsed time: 1.250 seconds" in text)
                assertTrue("literal exception %s" in text)
                assertTrue("literal cause" in text)
            }
        } finally {
            LoggingUtils.init()
            root.removeDirectory()
            snapshot.restore()
        }
    }
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import java.util.Properties
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested

/**
 * Verifies the UTF-8 properties loading used by the command line
 * application, using the duck fixture with its sixteen translations.
 */
class PropertiesLoadingTest {
    /**
     * The UTF-8 properties loading across the sixteen
     * translations of the duck fixture.
     */
    @Nested
    inner class `UTF8 support for properties files` {
        private val path = Path("src/test/resources/executiontests/properties/duck.properties")
        private val charset = Charsets.UTF_8
        private val translations: Properties = Properties().also { ConfigurationFiles.loadProperties(it, path, charset) }

        @Test
        fun `is not empty`() {
            assertTrue(
                translations.isNotEmpty(),
                "the properties file holds at least one entry"
            )
        }

        @Test
        fun `gets the correct string for english`() = assertTranslation("english", "duck")
        @Test
        fun `gets the correct string for spanish`() = assertTranslation("spanish", "patito")
        @Test
        fun `gets the correct string for french`() = assertTranslation("french", "canard")
        @Test
        fun `gets the correct string for german`() = assertTranslation("german", "ente")
        @Test
        fun `gets the correct string for portuguese`() = assertTranslation("portuguese", "pato")
        @Test
        fun `gets the correct string for italian`() = assertTranslation("italian", "anatra")
        @Test
        fun `gets the correct string for russian`() = assertTranslation("russian", "\u0443\u0442\u043A\u0430")
        @Test
        fun `gets the correct string for chinese`() = assertTranslation("chinese", "\u9E2D\u5B50")
        @Test
        fun `gets the correct string for japanese`() = assertTranslation("japanese", "\u30A2\u30D2\u30EB")
        @Test
        fun `gets the correct string for korean`() = assertTranslation("korean", "\uC624\uB9AC")
        @Test
        fun `gets the correct string for arabic`() = assertTranslation("arabic", "\u0628\u0637\u0647")
        @Test
        fun `gets the correct string for hindi`() = assertTranslation("hindi", "\u092C\u0924\u094D\u0924\u0916")
        @Test
        fun `gets the correct string for turkish`() = assertTranslation("turkish", "\u00F6\u0072\u0064\u0065\u006B")
        @Test
        fun `gets the correct string for swedish`() = assertTranslation("swedish", "anka")
        @Test
        fun `gets the correct string for dutch`() = assertTranslation("dutch", "eend")
        @Test
        fun `gets the correct string for norwegian`() = assertTranslation("norwegian", "and")

        private fun assertTranslation(language: String, expected: String) {
            assertEquals(
                expected,
                translations[language],
                "translation for $language"
            )
        }
    }
}

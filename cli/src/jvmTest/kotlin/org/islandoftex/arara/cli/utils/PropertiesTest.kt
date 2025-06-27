// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.util.Properties
import kotlin.io.path.Path

class PropertiesTest : ShouldSpec({
    context("duck translations (UTF-8 support)") {
        val path = Path("src/jvmTest/resources/executiontests/properties/duck.properties")
        val charset = Charsets.UTF_8
        val translations: Properties = Properties().apply { load(path, charset) }
        val expected = mapOf(
                "english" to "duck",
                "spanish" to "patito",
                "french" to "canard",
                "german" to "ente",
                "portuguese" to "pato",
                "italian" to "anatra",
                "russian" to "\u0443\u0442\u043A\u0430",
                "chinese" to "\u9E2D\u5B50",
                "japanese" to "\u30A2\u30D2\u30EB",
                "korean" to "\uC624\uB9AC",
                "arabic" to "\u0628\u0637\u0647",
                "hindi" to "\u092C\u0924\u094D\u0924\u0916",
                "turkish" to "\u00F6\u0072\u0064\u0065\u006B",
                "swedish" to "anka",
                "dutch" to "eend",
                "norwegian" to "and"
        )

        expected.forEach { (language, translation) ->
            should("translate $language to $translation") {
                translations[language] shouldBe translation
            }
        }
    }
})

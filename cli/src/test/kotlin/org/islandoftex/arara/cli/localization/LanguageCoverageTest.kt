// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.localization

import io.kotest.core.spec.style.ShouldSpec

class LanguageCoverageTest : ShouldSpec({
    /*"locale definitions" {
        /**
         * Helper method, checks the provided locale (all keys set?).
         *
         * @param locale The locale.
         * @return The size of the error list.
         */
        fun check(locale: Locale): Int {
            val errors = MessageKeyVerifier(Messages::class.java).verify(locale)
            errors.forEach(System.err::println)
            return errors.size
        }

        should("succeed instantiating known locale") {
            Locale("en").language shouldBe "en"
        }

        should("not error on known localizations") {
            listOf("en", "de", "nl", "it").forAll {
                check(Locale(it)) shouldBe 0
            }
            check(Locale("en", "QN")) shouldBe 0
            check(Locale("pt", "BR")) shouldBe 0
            check(Locale.forLanguageTag("en-QN")) shouldBe 0
            check(Locale.forLanguageTag("pt-BR")) shouldBe 0
        }
    }*/
})

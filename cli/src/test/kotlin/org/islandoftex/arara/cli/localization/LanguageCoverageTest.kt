// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.localization

import ch.qos.cal10n.verifier.MessageKeyVerifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale
import java.util.stream.Collectors
import org.islandoftex.arara.api.AraraException

class LanguageCoverageTest : ShouldSpec({
    "locale definitions" {
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
            Language("en").locale.language shouldBe "en"
        }
        should("throw on unknown locale instantiation") {
            shouldThrow<AraraException> {
                Language("quack")
            }
        }

        should("not error on known localizations") {
            listOf("en", "de", "nl", "it").forAll {
                check(Locale(it)) shouldBe 0
            }
            check(Locale("en", "QN")) shouldBe 0
            check(Locale("pt", "BR")) shouldBe 0
        }
    }
    "language coverage" {
        /*
         * Tests the localized messages, checking if all messages are properly
         * quoted (but not necessarily whether they are loadable).
         */
        should("get all strings from every language") {
            // get all files
            val files = Files.list(
                    Paths.get("src/main/resources/org/islandoftex/arara/cli/localization"))
                    .map { p: Path ->
                        val f = p.toFile()
                        if (f.name.endsWith("properties") && !f.isDirectory) f
                        else null
                    }
                    .collect(Collectors.toList())
                    .toList()
                    .filterNotNull()
            files.shouldNotBeEmpty()

            // for each report, print
            // the corresponding entry
            files.map { file: File ->
                try {
                    LanguageReport.analyze(file)
                } catch (exception: IOException) {
                    throw AssertionError(
                            "Fatal exception: an error was raised while " +
                                    "trying to read one of the languages. Please " +
                                    "make sure all languages in the provided " +
                                    "directory have read permission.")
                }
            }.forEach { report ->
                // debug output
                println(report.reference.name +
                        "\t" + String.format(" %2.2f%%", report.coverage))

                // if there are problematic lines,
                // add the current language report
                if (report.lines.isNotEmpty()) {
                    // legend: S = Simple message, single quotes should not be doubled
                    //         P = Parametrized message, single quotes must be doubled

                    // build the beginning of the line
                    println(report.reference.name)
                    // print error lines
                    println(report.lines)
                }

                report.coverage shouldBe 100.0f
            }
        }
    }
})

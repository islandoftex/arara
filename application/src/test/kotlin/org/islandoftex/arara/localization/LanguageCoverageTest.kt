/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.islandoftex.arara.localization

import ch.qos.cal10n.verifier.MessageKeyVerifier
import org.islandoftex.arara.model.AraraException
import io.kotlintest.inspectors.forAll
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

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
                    Paths.get("src/main/resources/org/islandoftex/arara/localization"))
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
                            "Fatal exception: an error was raised while "
                                    + "trying to read one of the languages. Please "
                                    + "make sure all languages in the provided "
                                    + "directory have read permission.")
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

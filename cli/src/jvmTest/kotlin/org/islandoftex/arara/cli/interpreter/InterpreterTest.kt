// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.spec.style.AnnotationSpec.Ignore
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.Directives
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InterpreterTest :
    ShouldSpec({
        val texFile = FileType("tex", "^\\s*%\\s+")

        DirectiveUtils.initializeDirectiveCore()

        xshould("return zero exit code for ordinary halt") {
//        should("throw exception for return zero exit code for ordinary halt") {
//            shouldThrow<NotImplementedError> {
                val rulePath = MPPPath("../rules").normalize()
                val filePath =
                        MPPPath("src/jvmTest/resources/executiontests/halt")
                                .normalize() / "halt.tex"
                   val haltDirective =
                        Directives
                                .extractDirectives(
                                        listOf("% arara: halt"),
                                        false,
                                        texFile,
                                ).single()
                                .run {
                                    DirectiveImpl(
                                            identifier,
                                            parameters.plus("reference" to filePath.toString()),
                                            conditional,
                                            lineNumbers,
                                    )
                                }

                Interpreter(
                        ExecutionOptions(rulePaths = setOf(rulePath)),
                        ProjectFile(filePath, texFile),
                        filePath.parent,
                ).execute(haltDirective)
                        .exitCode shouldBe 0
            }
//        }

        xshould("return non-zero exit code for error halt") {
//        should("throw exception for return non-zero exit code for error halt") {
//            shouldThrow<NotImplementedError> {
                val rulePath =
                        MPPPath("src/jvmTest/resources/executiontests/halt-error")
                                .normalize()
                val filePath = MPPPath(rulePath / "halt-error.tex")
                val haltDirective =
                        Directives
                                .extractDirectives(
                                        listOf("% arara: halt"),
                                        false,
                                        texFile,
                                ).single()
                                .run {
                                    DirectiveImpl(
                                            identifier,
                                            parameters.plus("reference" to filePath.toString()),
                                            conditional,
                                            lineNumbers,
                                    )
                                }

                Interpreter(
                        ExecutionOptions(rulePaths = setOf(rulePath)),
                        ProjectFile(filePath, texFile),
                        rulePath,
                ).execute(haltDirective)
                        .exitCode shouldNotBe 0
            }
//        }
    })

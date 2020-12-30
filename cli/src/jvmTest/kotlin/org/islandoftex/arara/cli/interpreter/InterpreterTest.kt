// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.Directives

class InterpreterTest : ShouldSpec({
    val texFile = FileType("tex", "^\\s*%\\s+")

    should("return zero exit code for ordinary halt") {
        val rulePath = MPPPath("../rules").normalize()
        val filePath = MPPPath("src/jvmTest/resources/executiontests/halt")
                .normalize() / "halt.tex"
        val haltDirective = Directives.extractDirectives(listOf("% arara: halt"),
                false, texFile).single().run {
            DirectiveImpl(identifier, parameters.plus("reference" to filePath.toString()),
                    conditional, lineNumbers)
        }

        Interpreter(
            ExecutionOptions(rulePaths = setOf(rulePath)),
                ProjectFile(filePath, texFile), filePath.parent)
                .execute(haltDirective).exitCode shouldBe 0
    }

    should("return non-zero exit code for error halt") {
        val rulePath = MPPPath("src/jvmTest/resources/executiontests/halt-error")
                .normalize()
        val filePath = MPPPath(rulePath / "halt-error.tex")
        val haltDirective = Directives.extractDirectives(listOf("% arara: halt"),
                false, texFile).single().run {
            DirectiveImpl(identifier, parameters.plus("reference" to filePath.toString()),
                    conditional, lineNumbers)
        }

        Interpreter(
            ExecutionOptions(rulePaths = setOf(rulePath)),
                ProjectFile(filePath, texFile), rulePath)
                .execute(haltDirective).exitCode shouldNotBe 0
    }
})

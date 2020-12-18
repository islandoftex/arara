// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths
import kotlin.io.path.div
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.Directives

class InterpreterTest : ShouldSpec({
    val texFile = FileType("tex", "^\\s*%\\s+")

    should("return zero exit code for ordinary halt") {
        val rulePath = MPPPath(FileHandling.normalize(Paths.get("../rules")))
        val filePath = MPPPath(FileHandling.normalize(
                Paths.get("src/test/resources/executiontests/halt")
        )) / "halt.tex"
        val haltDirective = Directives.extractDirectives(listOf("% arara: halt"),
                false, texFile).single().run {
            DirectiveImpl(identifier, parameters.plus("reference" to filePath.toString()),
                    conditional, lineNumbers)
        }

        Interpreter(ExecutionOptions(rulePaths = setOf(rulePath.toJVMPath())),
                ProjectFile(filePath, texFile), filePath.parent.toJVMPath())
                .execute(haltDirective).exitCode shouldBe 0
    }

    should("return non-zero exit code for error halt") {
        val rulePath = MPPPath(FileHandling.normalize(
                Paths.get("src/test/resources/executiontests/halt-error")))
        val filePath = MPPPath(rulePath / "halt-error.tex")
        val haltDirective = Directives.extractDirectives(listOf("% arara: halt"),
                false, texFile).single().run {
            DirectiveImpl(identifier, parameters.plus("reference" to filePath.toString()),
                    conditional, lineNumbers)
        }

        Interpreter(ExecutionOptions(rulePaths = setOf(rulePath.toJVMPath())),
                ProjectFile(filePath, texFile), rulePath.toJVMPath())
                .execute(haltDirective).exitCode shouldNotBe 0
    }
})

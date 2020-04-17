// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Paths
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.configuration.ExecutionOptions

class FileSearchingUtilsTest : ShouldSpec({
    should("fail looking up inexistent file") {
        FileSearchingUtils.lookupFile("QUACK", Paths.get("."),
                ExecutionOptions()) shouldBe null
    }

    should("fail on existing directory") {
        FileSearchingUtils.lookupFile("../buildSrc", Paths.get("."),
                ExecutionOptions()) shouldBe null
    }

    should("succeed finding tex file with extension") {
        val pathToTest = Paths.get("src/test/resources/executiontests/changes/changes.tex")
        val projectFile = FileSearchingUtils.lookupFile(
                pathToTest.toString(),
                Paths.get("."),
                ExecutionOptions()
        ) as ProjectFile
        projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                Paths.get(".").resolve(pathToTest).toFile().canonicalFile.absolutePath
        projectFile.fileType.extension shouldBe "tex"
        projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
    }

    should("succeed finding tex file without extension") {
        val projectFile = FileSearchingUtils.lookupFile(
                Paths.get("src/test/resources/executiontests/changes/changes").toString(),
                Paths.get("."),
                ExecutionOptions()
        ) as ProjectFile
        projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                File("./src/test/resources/executiontests/changes/changes.tex").canonicalFile.absolutePath
        projectFile.fileType.extension shouldBe "tex"
        projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
    }

    should("succeed finding tex file with extension in safe mode") {
        val pathToTest = Paths.get("src/test/resources/executiontests/changes/changes.tex")
        val projectFile = FileSearchingUtils.lookupFile(
                pathToTest.toString(),
                Paths.get("."),
                ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
        ) as ProjectFile
        projectFile.path.toFile().canonicalFile.absolutePath shouldBe
                pathToTest.toFile().canonicalFile.absolutePath
        projectFile.fileType.extension shouldBe "tex"
        projectFile.fileType.pattern shouldBe "^\\s*%\\s+"
    }

    should("not succeed finding tex file without extension in safe mode") {
        FileSearchingUtils.lookupFile(
                Paths.get("src/test/resources/executiontests/changes/changes").toString(),
                Paths.get("."),
                ExecutionOptions().copy(executionMode = ExecutionMode.SAFE_RUN)
        ) shouldBe null
    }
})

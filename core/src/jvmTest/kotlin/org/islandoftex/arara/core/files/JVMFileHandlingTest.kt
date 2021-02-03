// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath

class JVMFileHandlingTest : ShouldSpec({
    should("detect changes on file") {
        withContext(Dispatchers.IO) {
            val file = tempfile().toMPPPath()
            val databaseFile = tempfile().toMPPPath()
            databaseFile.toJVMPath().deleteIfExists()
            FileHandling.hasChanged(file, databaseFile) shouldBe true
            FileHandling.hasChanged(file, databaseFile) shouldBe false
            file.writeText("QUACK")
            FileHandling.hasChanged(file, databaseFile) shouldBe true
            FileHandling.hasChanged(file, databaseFile) shouldBe false
            file.writeText("QUACK2")
            FileHandling.hasChanged(file, databaseFile) shouldBe true
            file.toJVMPath().deleteExisting()
            FileHandling.hasChanged(file, databaseFile) shouldBe true
            FileHandling.hasChanged(file, databaseFile) shouldBe false
        }
    }
})

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import org.islandoftex.arara.core.rules.RuleMethods
import org.islandoftex.arara.core.rules.ConditionalMethods

import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HelperRegressionTest {
    @Test
    fun stringBuilderReceivesTheObjectsRatherThanAnArrayIdentity() {
        assertEquals("one two", RuleMethods.buildString("one", null, "", "two"))
    }

    @Test
    fun writeHelperReportsFailuresAndPreservesAppendAndOverwriteBehavior() {
        val directory = Files.createTempDirectory("arara-write").toMPPPath()
        try {
            assertFalse(RuleMethods.writeToFile(directory, "text", false))
            val target = directory.resolve("output.txt")
            assertTrue(RuleMethods.writeToFile(target.toString(), "first", false))
            assertTrue(RuleMethods.writeToFile(target.toString(), " second", true))
            assertEquals("first second", target.readText())
            assertTrue(RuleMethods.writeToFile(target.toString(), "replacement", false))
            assertEquals("replacement", target.readText())
        } finally {
            directory.removeDirectory()
        }
    }

    @Test
    fun safeModeRejectsAllClassLoadingOverloadsBeforeAccessingAnArchive() {
        val previous = LinearExecutor.executionOptions
        try {
            LinearExecutor.executionOptions = ExecutionOptions(executionMode = ExecutionMode.SAFE_RUN)
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadClass("missing.jar", "Plugin") }
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadClass(MPPPath("missing.jar"), "Plugin") }
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadObject("missing.jar", "Plugin") }
            assertFailsWith<SafeRunViolationException> { ConditionalMethods.loadObject(MPPPath("missing.jar"), "Plugin") }
        } finally {
            LinearExecutor.executionOptions = previous
        }
    }
}

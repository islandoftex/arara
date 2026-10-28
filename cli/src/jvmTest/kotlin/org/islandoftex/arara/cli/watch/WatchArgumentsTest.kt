// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.parse
import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.islandoftex.arara.cli.CLI

class WatchArgumentsTest {

    @Test
    fun watchRejectsLooseInputsCleaningAndSubcommandsBeforeExecuting() {
        listOf(listOf("run.tex"), listOf("project.proj.lua", "run.tex"),
            listOf("--clean-build", "project.proj.lua"), listOf("rules")).forEach { arguments ->
            assertFailsWith<UsageError> { CLI().parse(listOf("--watch") + arguments) }
        }
    }
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectWatchContractTest {

    @Test
    fun existingProjectImplementationsReceiveAnEmptyWatchSelection() {
        val project = object : Project {
            override val name = "legacy implementation"
            override val workingDirectory = MPPPath(".").normalize()
            override val files = emptySet<ProjectFile>()
            override val dependencies = emptySet<String>()
        }
        assertTrue(project.watch.paths.isEmpty())
        assertTrue(project.watch.include.isEmpty())
        assertTrue(project.watch.exclude.isEmpty())
        assertEquals(project.workingDirectory, project.executionDirectory)
    }

    @Test
    fun watchSelectionsRetainTypedPathsAndIndependentIncludeExcludeLists() {
        val path = MPPPath("../shared").normalize()
        val options = WatchOptions(listOf(path), listOf("figures/**/*.pdf"), listOf("draft/**"))
        assertEquals(listOf(path), options.paths)
        assertEquals(listOf("figures/**/*.pdf"), options.include)
        assertEquals(listOf("draft/**"), options.exclude)
        assertEquals(options, options.copy())
        assertEquals(emptyList(), options.copy(paths = emptyList()).paths)
    }
}

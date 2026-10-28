// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectParseException

class ProjectWatchFieldsTest {

    @Test
    fun watchFieldsSurviveFileTypeResolution() {
        val root = MPPPath(".").normalize()
        val project = LuaInterpreter(root).parseProjectsFromLua("""
            return { files = { ['main.tex'] = {} }, watch = {
                paths = { '../shared' }, include = { '**/*.pdf' }, exclude = { 'draft/**' }
            } }
        """.trimIndent()).single()
        assertEquals(listOf(root.resolve("../shared").normalize()), project.watch.paths)
        assertEquals(listOf("**/*.pdf"), project.watch.include)
        assertEquals(listOf("draft/**"), project.watch.exclude)
        assertEquals(project.watch, LuaInterpreter.resolvePendingFileTypes(project,
            org.islandoftex.arara.core.configuration.ExecutionOptions()).watch)
    }

    @Test
    fun rejectsMalformedWatchFields() {
        listOf("false", "{ other = {} }", "{ paths = { 1 } }", "{ include = { '' } }",
            "{ exclude = { [2] = 'a' } }").forEach { value ->
            assertFailsWith<ProjectParseException> {
                LuaInterpreter(MPPPath(".")).parseProjectsFromLua(
                    "return { files = { ['main.tex'] = {} }, watch = $value }")
            }
        }
    }
}

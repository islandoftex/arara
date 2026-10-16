// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.islandoftex.arara.api.files.BuildSelection
import org.islandoftex.arara.api.files.BuildSymlinks
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions

class ProjectBuildFieldsTest {
    private val root = MPPPath(".").normalize()

    @Test
    fun optionalBuildFieldsSurviveFileTypeResolution() {
        val interpreter = LuaInterpreter(root)
        assertNull(interpreter.parseProjectsFromLua("return {files={['book.tex']={}}}").single().buildDirectory)
        val project = interpreter.parseProjectsFromLua("""
            return { files={['book.tex']={}}, build={directory='build', symlinks='copy',
              inputs={'chapters/**', 'figures', {source='../shared', destination='styles'}}} }
        """.trimIndent()).single()
        val options = checkNotNull(LuaInterpreter.resolvePendingFileTypes(project, ExecutionOptions()).buildDirectory)
        assertEquals(root.resolve("build"), options.directory)
        assertEquals(BuildSymlinks.COPY, options.symlinks)
        assertEquals(listOf(BuildSelection.Glob("chapters/**"), BuildSelection.Path(MPPPath("figures"))), options.inputs)
        assertEquals(root.parent.resolve("shared"), options.mappings.single().source)
        assertEquals("styles", options.mappings.single().destination)
        val defaults = interpreter.parseProjectsFromLua(
            "return {files={['book.tex']={}}, build={directory='build'}}").single().buildDirectory!!
        assertNull(defaults.inputs)
        assertEquals(BuildSymlinks.PRESERVE, defaults.symlinks)
        val mappedTree = interpreter.parseProjectsFromLua("""
            return {files={['book.tex']={}},build={directory='build',
              mappings={{source='../shared',destination='vendor'}}}}
        """.trimIndent()).single().buildDirectory!!
        assertNull(mappedTree.inputs)
        assertEquals("vendor", mappedTree.mappings.single().destination)
    }

    @Test
    fun malformedBuildFieldsFailAtTheLuaBoundary() {
        for (build in listOf("false", "{}", "{directory=''}", "{directory='build', typo=true}",
            "{directory='build',symlinks='follow'}", "{directory='build',symlinks=true}",
            "{directory='build',inputs='book.tex'}", "{directory='build',inputs={[2]='book.tex'}}",
            "{directory='build',inputs={''}}", "{directory='build',inputs={false}}",
            "{directory='build',inputs={{source='a'}}}",
            "{directory='build',inputs={{source='a',destination='b',typo=true}}}",
            "{directory='build',mappings={'not a table'}}", "{directory='build',mappings=false}")) {
            assertFailsWith<ProjectParseException>(build) {
                LuaInterpreter(root).parseProjectsFromLua("return {files={['book.tex']={}},build=$build}")
            }
        }
    }
}

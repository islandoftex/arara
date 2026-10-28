// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.luaj.vm2.LuaValue

class LuaPathTest {
    @Test
    fun suppliedSpellingIsExplicitAndReadOnlyInBothEnvironments() {
        listOf(LuaRuleEnvironment.newGlobals(), LuaConditionalEnvironment.newGlobals()).forEach { globals ->
            globals.load("""
                local path = to_file('./nested//../chapter.tex')
                assert(path.supplied_spelling == './nested//../chapter.tex')
                assert(path.suppliedSpelling == path.supplied_spelling)
                assert(tostring(path) == tostring(path:normalize()))
                assert(path:normalize().supplied_spelling == tostring(path))
                assert(path == path:normalize())
                local ok = pcall(function() path.supplied_spelling = 'changed' end)
                assert(not ok)
                assert(path.supplied_spelling == './nested//../chapter.tex')
            """.trimIndent()).call()
        }
    }

    private fun inDirectory(test: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara Lua paths ").toMPPPath()
        try { test(root) } finally { root.removeDirectory() }
    }

    @Test
    fun propertiesResolutionAndIdentityPreserveNativePaths() = inDirectory { root ->
        val globals = LuaRuleEnvironment.newGlobals()
        globals.set("root", LuaConversion.toLua(root))
        globals.set("same", LuaConversion.toLua(root))
        globals.load("""
            assert(type(root) == 'userdata' and root == same)
            assert(root.is_absolute and root.isAbsolute)
            assert(root.exists and root.is_directory and root.isDirectory)
            assert(not root.is_regular_file and not root.isRegularFile)
            assert(root.file_name == root.fileName)
            assert(root:normalize() == root)
            local child = root:resolve('child.txt')
            assert(child.parent == root)
            assert(child.file_name == 'child.txt')
            assert(child:starts_with(root) and child:startsWith(root))
            assert(child:resolve_sibling('other.txt') == root:resolve('other.txt'))
            assert(child:resolveSibling('other.txt') == root:resolve('other.txt'))
            assert(root.getClass == nil and root.toJVMPath == nil and root.remove_directory == nil)
        """.trimIndent()).call()
        globals.set("relative", LuaConversion.toLua(MPPPath("relative.txt")))
        val resolved = globals.load("return root:resolve(relative)").call()
        assertEquals(root.resolve("relative.txt"), LuaConversion.fromLua(resolved))
        assertEquals(root.resolveSibling("relative.txt"), LuaConversion.fromLua(
            globals.load("return root:resolve_sibling(relative)").call()))
        assertEquals(root, LuaConversion.fromLua(globals.get("root")))
        assertEquals(root.toString(), globals.load("return tostring(root)").call().tojstring())
        assertFailsWith<Exception> { globals.load("root.file_name = 'changed'").call() }
    }

    @Test
    fun textAndListingMethodsDelegateToStrictPathOperations() = inDirectory { root ->
        val globals = LuaRuleEnvironment.newGlobals()
        globals.set("root", LuaConversion.toLua(root))
        globals.load("""
            local directory = root:resolve('subdir')
            directory:create_directories()
            directory:createDirectories()
            local file = directory:resolve('text.txt')
            assert(file:write_text('first\n') == nil)
            file:writeText('second', true)
            assert(file:read_text() == 'first\nsecond')
            assert(file:readText() == 'first\nsecond')
            assert(file.file_size == 12 and file.fileSize == 12)
            assert(file.is_regular_file and file.isRegularFile)
            assert(#file:read_lines() == 2 and file:readLines()[2] == 'second')
            assert(#root:list_files() == 0 and #root:listFiles() == 0)
            assert(root:list_entries()[1] == directory and root:listEntries()[1] == directory)
            assert(root:list_files(true)[1] == file and root:listFiles(true)[1] == file)
        """.trimIndent()).call()
        assertEquals("first\nsecond", root.resolve("subdir/text.txt").readText())
        val path = LuaConversion.toLua(root.resolve("missing.txt"))
        assertFailsWith<AraraIOException> { path.get("read_text").call(path) }
        assertFailsWith<AraraIOException> { path.get("file_size") }
        val directory = globals.get("root")
        assertFailsWith<AraraIOException> {
            directory.get("write_text").call(directory, LuaValue.valueOf("text"))
        }
    }

    @Test
    fun methodsRejectInvalidArgumentsAndPreserveTheCalledSpelling() = inDirectory { root ->
        val globals = LuaRuleEnvironment.newGlobals()
        globals.set("root", LuaConversion.toLua(root))
        for ((expression, name) in listOf(
            "root:resolve(true)" to "resolve",
            "root:resolveSibling(42)" to "resolveSibling",
            "root:starts_with('text')" to "starts_with",
            "root:write_text(true)" to "write_text",
            "root:writeText('text', 'yes')" to "writeText",
            "root:list_files(1)" to "list_files",
            "root.read_text('text')" to "read_text"
        )) {
            val error = assertFailsWith<Exception> { globals.load(expression).call() }
            assertTrue(error.toString().contains("path.$name"), error.toString())
        }
    }

    @Test
    fun helperAndSessionBoundariesReturnPathsWithMethodsInBothEnvironments() = inDirectory { root ->
        root.resolve("input.txt").writeText("content")
        for (globals in listOf(LuaRuleEnvironment.newGlobals(), LuaConditionalEnvironment.newGlobals())) {
            globals.set("root", LuaConversion.toLua(root))
            globals.load("""
                local file = to_file(tostring(root:resolve('input.txt')))
                assert(file:read_text() == 'content')
                assert(exists(file) and found(file, 'content'))
                local session = get_session()
                session.put('path-methods-test', {file})
                local restored = session.get('path-methods-test')[1]
                session.remove('path-methods-test')
                assert(restored == file and restored:read_text() == 'content')
            """.trimIndent()).call()
        }
    }
}

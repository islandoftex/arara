// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import io.github.taoguan.luaj.LuaValue
import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the conditional helper bindings of the Lua environment:
 * the file existence and change checks in both argument shapes, the
 * current file and original reference queries, and the class
 * loading tables. The extension-based helpers are exercised inside
 * the lifecycle window in which real conditionals evaluate; the
 * interactive dialog surface is deliberately out of scope here, as
 * its fate is under review.
 */
class LuaConditionalEnvironmentTest {
    private val globals = LuaConditionalEnvironment.newGlobals()

    private fun binding(name: String): LuaValue = globals.get(name)

    private fun evaluate(snippet: String): LuaValue =
        globals.load("return $snippet").call()

    private fun call(name: String, vararg arguments: LuaValue): LuaValue =
        binding(name).invoke(LuaValue.varargsOf(arguments)).arg1()

    @Test
    fun `binds file objects from names`() {
        val file = evaluate("toFile('quack.tex')")
        assertTrue(file.isuserdata())
        assertTrue(file.touserdata() is MPPPath)
    }

    @Test
    fun `the file helpers accept file objects and names`() {
        val file = Files.createTempFile("arara-lua-conditional-", ".log")
        Files.writeString(file, "error in line 3")
        val name = file.fileName.toString().substringBeforeLast('.')
        val fileObject = call("toFile", LuaValue.valueOf(file.toString()))
        assertTrue(call("exists", fileObject).toboolean())
        assertFalse(call("missing", fileObject).toboolean())
        assertTrue(call("missing", call("toFile", LuaValue.valueOf("$file.gone"))).toboolean())
        assertTrue(call("found", fileObject, LuaValue.valueOf("error")).toboolean())
        assertFalse(call("found", fileObject, LuaValue.valueOf("warning")).toboolean())
        assertEquals(name, call("getBasename", fileObject).tojstring())
        assertEquals(name, call("getBasename", LuaValue.valueOf(file.toString())).tojstring())
        // non-file arguments are rejected with the bad argument error
        assertFailsWith<AraraException> {
            call("exists", LuaValue.valueOf(42))
        }
    }

    @Test
    fun `the extension helpers resolve against the file under run`() {
        executeWithinProject(listOf("main.tex" to 0)) { directory, _ ->
            Files.writeString(directory.resolve("main.log"), "error in line 3")
            assertTrue(evaluate("exists('log')").toboolean())
            assertFalse(evaluate("exists('aux')").toboolean())
            assertTrue(evaluate("missing('aux')").toboolean())
            assertFalse(evaluate("missing('log')").toboolean())
            assertTrue(evaluate("found('log', 'error in')").toboolean())
            assertFalse(evaluate("found('log', 'warning')").toboolean())
            // the change checks consult the file database
            Files.writeString(directory.resolve("main.aux"), "auxiliary")
            assertTrue(evaluate("changed('aux')").toboolean())
            assertFalse(evaluate("changed('aux')").toboolean())
            assertTrue(evaluate("unchanged('aux')").toboolean())
            // the file-based change checks consult the same database
            val watched = directory.resolve("watch.log")
            Files.writeString(watched, "watched")
            val watchedObject = call("toFile", LuaValue.valueOf(watched.toString()))
            assertTrue(call("changed", watchedObject).toboolean())
            assertFalse(call("changed", watchedObject).toboolean())
            assertTrue(call("unchanged", watchedObject).toboolean())
            // the current file and the original reference are file objects
            val current = evaluate("currentFile()")
            assertTrue(current.touserdata() is MPPPath)
            val original = evaluate("getOriginalReference()")
            assertTrue(original.touserdata() is MPPPath)
        }
    }

    @Test
    fun `the class loading helpers report through status tables`() {
        val jar = classArchive(
            "kotlin/Unit.class",
            "org/islandoftex/arara/lua/rules/LuaConditionalEnvironmentTest.class",
        )
        val jarObject = call("toFile", LuaValue.valueOf(jar.toString()))
        val loaded = call(
            "loadClass", LuaValue.valueOf(jar.toString()), LuaValue.valueOf("kotlin.Unit")
        )
        assertEquals("SUCCESS", loaded.get("status").tojstring())
        val byFile = call("loadClass", jarObject, LuaValue.valueOf("kotlin.Unit"))
        assertEquals("SUCCESS", byFile.get("status").tojstring())
        val missing = call(
            "loadClass", LuaValue.valueOf("arara-not-there.jar"),
            LuaValue.valueOf("kotlin.Unit")
        )
        assertEquals("FILE_NOT_FOUND", missing.get("status").tojstring())
        val notConstructible = call(
            "loadObject", LuaValue.valueOf(jar.toString()), LuaValue.valueOf("kotlin.Unit")
        )
        assertEquals("ILLEGAL_ACCESS", notConstructible.get("status").tojstring())
        val instantiated = call(
            "loadObject", jarObject,
            LuaValue.valueOf(LuaConditionalEnvironmentTest::class.java.name)
        )
        assertEquals("SUCCESS", instantiated.get("status").tojstring())
        assertTrue(instantiated.get("value").touserdata() is LuaConditionalEnvironmentTest)
    }

    @Test
    fun `the class loading helpers are rejected in safe mode`() {
        val original = LinearExecutor.executionOptions
        try {
            LinearExecutor.executionOptions =
                ExecutionOptions.from(original).copy(executionMode = ExecutionMode.SAFE_RUN)
            assertFailsWith<AraraException> {
                call(
                    "loadClass", LuaValue.valueOf("arara-not-there.jar"),
                    LuaValue.valueOf("kotlin.Unit")
                )
            }
            assertFailsWith<AraraException> {
                call(
                    "loadObject", LuaValue.valueOf("arara-not-there.jar"),
                    LuaValue.valueOf("kotlin.Unit")
                )
            }
        } finally {
            LinearExecutor.executionOptions = original
        }
    }

    /**
     * Builds a throwaway archive from class resources already on the
     * test classpath, so the loading helpers can resolve real classes
     * from a file of their own, without depending on any external
     * artifact.
     */
    private fun classArchive(vararg resources: String): MPPPath {
        val jar = Files.createTempFile("arara-lua-classload-", ".jar")
        JarOutputStream(Files.newOutputStream(jar)).use { stream ->
            for (resource in resources) {
                val bytes = javaClass.classLoader.getResourceAsStream(resource)?.readBytes()
                    ?: error("the class resource $resource is not available")
                stream.putNextEntry(JarEntry(resource))
                stream.write(bytes)
                stream.closeEntry()
            }
        }
        return MPPPath(jar.toString())
    }
}

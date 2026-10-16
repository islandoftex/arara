// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ForeignPathTest {
    @Test
    fun foreignWindowsPathsNeverDependOnTheHost() {
        val drive = ForeignPath.parse("C:/Book/../chapter.tex", PathSyntax.WINDOWS)
        assertTrue(drive.isAbsolute)
        assertEquals("C:\\chapter.tex", drive.normalize().toString())
        assertEquals(listOf("Book", "..", "chapter.tex"), drive.segments)
        assertEquals("chapter.tex", drive.fileName)
        assertNotEquals(drive.normalize(), ForeignPath.parse("c:/chapter.tex", PathSyntax.WINDOWS))
        val unc = ForeignPath.parse("//server/share/folder/file.tex", PathSyntax.WINDOWS)
        assertEquals("\\\\server\\share\\", unc.root)
        assertEquals("\\\\server\\share\\folder", unc.parent.toString())
        assertEquals("\\\\server\\share\\other.tex", unc.resolve("\\other.tex").toString())
        assertTrue(unc.startsWith(unc.parent))
    }

    @Test
    fun posixPathsKeepBackslashesAndRequireExplicitNormalization() {
        val relative = ForeignPath.parse("a/./b/../file", PathSyntax.POSIX)
        assertFalse(relative.isAbsolute)
        assertEquals("a/./b/../file", relative.toString())
        assertEquals("a/file", relative.normalize().toString())
        val backslash = ForeignPath.parse("C:\\file.tex", PathSyntax.POSIX)
        assertFalse(backslash.isAbsolute)
        assertEquals(listOf("C:\\file.tex"), backslash.segments)
        assertEquals("/child", ForeignPath.parse("/base", PathSyntax.POSIX).resolve("/child").toString())
        assertEquals("/", ForeignPath.parse("/../..", PathSyntax.POSIX).normalize().toString())
        assertEquals("../../file", ForeignPath.parse("../../file", PathSyntax.POSIX).normalize().toString())
    }

    @Test
    fun ambiguousDriveContextsAndMixedSyntaxAreExplicitErrors() {
        val base = ForeignPath.parse("C:\\base", PathSyntax.WINDOWS)
        assertFalse(ForeignPath.parse("C:file", PathSyntax.WINDOWS).isAbsolute)
        assertFailsWith<IllegalArgumentException> { base.resolve("C:file") }
        assertFailsWith<IllegalArgumentException> { base.resolve(ForeignPath.parse("x", PathSyntax.POSIX)) }
        assertFailsWith<IllegalArgumentException> { ForeignPath.parse("\\\\server", PathSyntax.WINDOWS) }
        assertFailsWith<IllegalArgumentException> { ForeignPath.parse("bad\u0000path", PathSyntax.POSIX) }
    }
}

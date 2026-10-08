// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException

/**
 * Verifies the glob-to-regex conversion the file listings and rule
 * lookups rely on: the wildcards become their regex counterparts,
 * escapes and character classes keep their meaning, braces become
 * groups, and a malformed glob surfaces as the arara error naming
 * the pattern.
 *
 * Code by [Neil Traft](https://stackoverflow.com/users/213525/neil-traft)
 * (see https://stackoverflow.com/a/17369948, released into public domain as
 * per [his comment](https://stackoverflow.com/questions/1247772/is-there-an-
 * equivalent-of-java-util-regex-for-glob-type-patterns#comment108405664_17369948).
 */
class StringExtensionsTest {
    @Test
    fun `converts a star to dot star`() {
        assertEquals(
            "gl.*b", "gl*b".globToRegex().pattern,
            "regex conversion of the star glob"
        )
    }

    @Test
    fun `keeps an escaped star unchanged`() {
        assertEquals(
            "gl\\*b", "gl\\*b".globToRegex().pattern,
            "an escaped star keeps its backslash"
        )
    }

    @Test
    fun `converts a question mark to a dot`() {
        assertEquals(
            "gl.b", "gl?b".globToRegex().pattern,
            "regex conversion of the question mark glob"
        )
    }

    @Test
    fun `keeps an escaped question mark unchanged`() {
        assertEquals(
            "gl\\?b", "gl\\?b".globToRegex().pattern,
            "an escaped question mark keeps its backslash"
        )
    }

    @Test
    fun `keeps character classes unchanged`() {
        assertEquals(
            "gl[-o]b", "gl[-o]b".globToRegex().pattern,
            "a character class passes through unchanged"
        )
    }

    @Test
    fun `keeps escaped class brackets unchanged`() {
        assertEquals(
            "gl\\[-o\\]b", "gl\\[-o\\]b".globToRegex().pattern,
            "escaped class brackets keep their backslashes"
        )
    }

    @Test
    fun `converts negation inside character classes`() {
        assertEquals(
            "gl[^a-n!p-z]b", "gl[!a-n!p-z]b".globToRegex().pattern,
            "an exclamation mark inside a class becomes a negation"
        )
    }

    @Test
    fun `converts negation inside nested character classes`() {
        assertEquals(
            "gl[[^a-n]!p-z]b", "gl[[!a-n]!p-z]b".globToRegex().pattern,
            "a negation in a nested class also converts"
        )
    }

    @Test
    fun `escapes a leading caret inside a character class`() {
        assertEquals(
            "gl[\\^o]b", "gl[^o]b".globToRegex().pattern,
            "a leading caret inside a class is escaped"
        )
    }

    @Test
    fun `escapes regular expression metacharacters`() {
        assertEquals(
            "gl..*\\.\\(\\)\\+\\|\\^\\$\\@\\%b", "gl?*.()+|^$@%b".globToRegex().pattern,
            "regular expression metacharacters are escaped"
        )
    }

    @Test
    fun `keeps metacharacters inside character classes unchanged`() {
        assertEquals(
            "gl[?*.()+|^$@%]b", "gl[?*.()+|^$@%]b".globToRegex().pattern,
            "metacharacters inside a class pass through unchanged"
        )
    }

    @Test
    fun `keeps an escaped backslash unchanged`() {
        assertEquals(
            "gl\\\\b", "gl\\\\b".globToRegex().pattern,
            "an escaped backslash keeps its duplication"
        )
    }

    @Test
    fun `escapes the quoting expressions`() {
        assertEquals(
            "\\\\Qglob\\\\E", "\\Qglob\\E".globToRegex().pattern,
            "the quoting expressions are escaped"
        )
    }

    @Test
    fun `turns braces into groups`() {
        assertEquals(
            "(glob|regex)", "{glob,regex}".globToRegex().pattern,
            "braces become groups with alternatives"
        )
    }

    @Test
    fun `keeps escaped braces unchanged`() {
        assertEquals(
            "\\{glob\\}", "\\{glob\\}".globToRegex().pattern,
            "escaped braces keep their backslashes"
        )
    }

    @Test
    fun `does not escape commas outside groups`() {
        assertEquals(
            "(glob,regex),", "{glob\\,regex},".globToRegex().pattern,
            "commas outside groups pass through unchanged"
        )
    }

    @Test
    fun `produces a friendly error for malformed globs`() {
        // an unbalanced brace becomes an unbalanced group in the
        // conversion, which has to surface as an arara error naming the
        // pattern instead of a raw pattern syntax exception
        val exception = assertFailsWith<AraraException>(
            "a malformed glob must be rejected"
        ) {
            "gl{ob".globToRegex()
        }
        assertTrue(
            exception.message!!.contains("gl{ob"),
            "the rejection names the malformed pattern"
        )
    }
}

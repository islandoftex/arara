// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for glob to regex conversion.
 *
 * Code by [Neil Traft](https://stackoverflow.com/users/213525/neil-traft)
 * (see https://stackoverflow.com/a/17369948, released into public domain as
 * per [his comment](https://stackoverflow.com/questions/1247772/is-there-an-
 * equivalent-of-java-util-regex-for-glob-type-patterns#comment108405664_17369948).
 */
class StringExtensionsTest {
    @Test
    fun star_becomes_dot_star() {
        assertEquals(
            "gl.*b", "gl*b".globToRegex().pattern,
            "regex conversion of the star glob"
        )
    }

    @Test
    fun escaped_star_is_unchanged() {
        assertEquals(
            "gl\\*b", "gl\\*b".globToRegex().pattern,
            "an escaped star keeps its backslash"
        )
    }

    @Test
    fun question_mark_becomes_dot() {
        assertEquals(
            "gl.b", "gl?b".globToRegex().pattern,
            "regex conversion of the question mark glob"
        )
    }

    @Test
    fun escaped_question_mark_is_unchanged() {
        assertEquals(
            "gl\\?b", "gl\\?b".globToRegex().pattern,
            "an escaped question mark keeps its backslash"
        )
    }

    @Test
    fun character_classes_dont_need_conversion() {
        assertEquals(
            "gl[-o]b", "gl[-o]b".globToRegex().pattern,
            "a character class passes through unchanged"
        )
    }

    @Test
    fun escaped_classes_are_unchanged() {
        assertEquals(
            "gl\\[-o\\]b", "gl\\[-o\\]b".globToRegex().pattern,
            "escaped class brackets keep their backslashes"
        )
    }

    @Test
    fun negation_in_character_classes() {
        assertEquals(
            "gl[^a-n!p-z]b", "gl[!a-n!p-z]b".globToRegex().pattern,
            "an exclamation mark inside a class becomes a negation"
        )
    }

    @Test
    fun nested_negation_in_character_classes() {
        assertEquals(
            "gl[[^a-n]!p-z]b", "gl[[!a-n]!p-z]b".globToRegex().pattern,
            "a negation in a nested class also converts"
        )
    }

    @Test
    fun escape_caret_if_it_is_the_first_char_in_a_character_class() {
        assertEquals(
            "gl[\\^o]b", "gl[^o]b".globToRegex().pattern,
            "a leading caret inside a class is escaped"
        )
    }

    @Test
    fun metachars_are_escaped() {
        assertEquals(
            "gl..*\\.\\(\\)\\+\\|\\^\\$\\@\\%b", "gl?*.()+|^$@%b".globToRegex().pattern,
            "regular expression metacharacters are escaped"
        )
    }

    @Test
    fun metachars_in_character_classes_dont_need_escaping() {
        assertEquals(
            "gl[?*.()+|^$@%]b", "gl[?*.()+|^$@%]b".globToRegex().pattern,
            "metacharacters inside a class pass through unchanged"
        )
    }

    @Test
    fun escaped_backslash_is_unchanged() {
        assertEquals(
            "gl\\\\b", "gl\\\\b".globToRegex().pattern,
            "an escaped backslash keeps its duplication"
        )
    }

    @Test
    fun slashQ_and_slashE_are_escaped() {
        assertEquals(
            "\\\\Qglob\\\\E", "\\Qglob\\E".globToRegex().pattern,
            "the quoting expressions are escaped"
        )
    }

    @Test
    fun braces_are_turned_into_groups() {
        assertEquals(
            "(glob|regex)", "{glob,regex}".globToRegex().pattern,
            "braces become groups with alternatives"
        )
    }

    @Test
    fun escaped_braces_are_unchanged() {
        assertEquals(
            "\\{glob\\}", "\\{glob\\}".globToRegex().pattern,
            "escaped braces keep their backslashes"
        )
    }

    @Test
    fun commas_dont_need_escaping() {
        assertEquals(
            "(glob,regex),", "{glob\\,regex},".globToRegex().pattern,
            "commas outside groups pass through unchanged"
        )
    }

    @Test
    fun malformed_globs_produce_a_friendly_error() {
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

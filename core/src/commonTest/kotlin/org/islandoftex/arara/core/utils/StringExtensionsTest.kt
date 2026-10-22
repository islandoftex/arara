// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for glob to regex conversion.
 *
 * Code by [Neil Traft](https://stackoverflow.com/users/213525/neil-traft)
 * (see https://stackoverflow.com/a/17369948, released into public domain as
 * per [his comment](https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns#comment108405664_17369948).
 */
class StringExtensionsTest {
    @Test
    fun star_becomes_dot_star() {
        assertEquals("gl.*b", "gl*b".globToRegex().pattern)
    }

    @Test
    fun escaped_star_is_unchanged() {
        assertEquals("gl\\*b", "gl\\*b".globToRegex().pattern)
    }

    @Test
    fun question_mark_becomes_dot() {
        assertEquals("gl.b", "gl?b".globToRegex().pattern)
    }

    @Test
    fun escaped_question_mark_is_unchanged() {
        assertEquals("gl\\?b", "gl\\?b".globToRegex().pattern)
    }

    @Test
    fun character_classes_dont_need_conversion() {
        assertEquals("gl[-o]b", "gl[-o]b".globToRegex().pattern)
    }

    @Test
    fun escaped_classes_are_unchanged() {
        assertEquals("gl\\[-o\\]b", "gl\\[-o\\]b".globToRegex().pattern)
    }

    @Test
    fun negation_in_character_classes() {
        assertEquals("gl[^a-n!p-z]b", "gl[!a-n!p-z]b".globToRegex().pattern)
    }

    @Test
    fun nested_negation_in_character_classes() {
        assertEquals("gl[[^a-n]!p-z]b", "gl[[!a-n]!p-z]b".globToRegex().pattern)
    }

    @Test
    fun escape_caret_if_it_is_the_first_char_in_a_character_class() {
        assertEquals("gl[\\^o]b", "gl[^o]b".globToRegex().pattern)
    }

    @Test
    fun metachars_are_escaped() {
        assertEquals("gl..*\\.\\(\\)\\+\\|\\^\\$\\@\\%b", "gl?*.()+|^$@%b".globToRegex().pattern)
    }

    @Test
    fun metachars_in_character_classes_dont_need_escaping() {
        assertEquals("gl[?*.()+|^$@%]b", "gl[?*.()+|^$@%]b".globToRegex().pattern)
    }

    @Test
    fun escaped_backslash_is_unchanged() {
        assertEquals("gl\\\\b", "gl\\\\b".globToRegex().pattern)
    }

    @Test
    fun slashQ_and_slashE_are_escaped() {
        assertEquals("\\\\Qglob\\\\E", "\\Qglob\\E".globToRegex().pattern)
    }

    @Test
    fun braces_are_turned_into_groups() {
        assertEquals("(glob|regex)", "{glob,regex}".globToRegex().pattern)
    }

    @Test
    fun escaped_braces_are_unchanged() {
        assertEquals("\\{glob\\}", "\\{glob\\}".globToRegex().pattern)
    }

    @Test
    fun commas_dont_need_escaping() {
        assertEquals("(glob,regex),", "{glob\\,regex},".globToRegex().pattern)
    }
}

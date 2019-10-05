package com.github.cereda.arara.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExtensionTest {
    @Test
    fun checkStringAbbreviation() {
        assertEquals("Quack…", "Quack quack".abbreviate(6))
        assertEquals("Quack Quack", "Quack Quack".abbreviate(80))
        assertThrows<IllegalArgumentException> { "Quack".abbreviate(1) }
    }

    @Test
    fun checkStringCentering() {
        assertEquals("Quack", "Quack".center(3, '-'))
        assertEquals("--Quack--", "Quack".center(9, '-'))
    }

    @Test
    fun checkStringWrap() {
        assertEquals("This text\nshould be\nwrapped",
                "This text should be wrapped".wrap(10))
    }
}
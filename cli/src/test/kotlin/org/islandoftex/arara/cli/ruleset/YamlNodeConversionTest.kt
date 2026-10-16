// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.charleskorn.kaml.Yaml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Verifies the conversion of parsed YAML nodes into the plain Kotlin
 * values the interpreter expects: maps, lists and scalar resolution per
 * the YAML 1.2 core schema, with quoting always yielding strings and
 * null values being rejected.
 */
class YamlNodeConversionTest {
    @Test
    fun `converts a flat parameter map`() {
        val map = YamlNodeConversion.toParameterMap(
            Yaml.default.parseToYamlNode("{ shell: yes, draft: true }")
        )
        assertEquals("yes", map["shell"], "yes stays a string under YAML 1.2")
        assertEquals(true, map["draft"], "a plain boolean resolves to Boolean")
    }

    @Test
    fun `converts integers and floats`() {
        val map = YamlNodeConversion.toParameterMap(
            Yaml.default.parseToYamlNode("{ loops: 5, timeout: 1.5 }")
        )
        assertEquals(5, map["loops"], "a plain integer resolves to Int")
        assertEquals(1.5, map["timeout"], "a plain float resolves to Double")
    }

    @Test
    fun `keeps quoted scalars as strings`() {
        val map = YamlNodeConversion.toParameterMap(
            Yaml.default.parseToYamlNode("{ a: '123', b: \"true\", c: '1.5' }")
        )
        assertEquals("123", map["a"], "a quoted integer stays a string")
        assertEquals("true", map["b"], "a quoted boolean stays a string")
        assertEquals("1.5", map["c"], "a quoted float stays a string")
    }

    @Test
    fun `converts sequences and nested maps`() {
        val map = YamlNodeConversion.toParameterMap(
            Yaml.default.parseToYamlNode(
                "{ options: [ one, two ], nested: { key: value } }"
            )
        )
        assertEquals(
            listOf("one", "two"), map["options"],
            "a sequence converts to a list of values"
        )
        assertEquals(
            mapOf("key" to "value"), map["nested"],
            "a nested map converts to a map of values"
        )
    }

    @Test
    fun `rejects null values`() {
        val exception = assertFailsWith<IllegalArgumentException>(
            "a null parameter value must be rejected at parse time"
        ) {
            YamlNodeConversion.toParameterMap(
                Yaml.default.parseToYamlNode("{ opt: null }")
            )
        }
        assertTrue(
            exception.message?.contains("null") == true,
            "the rejection names the null problem"
        )
    }

    @Test
    fun `rejects a non-map root`() {
        assertFailsWith<IllegalArgumentException>(
            "a non-map root must be rejected as a parameter map"
        ) {
            YamlNodeConversion.toParameterMap(
                Yaml.default.parseToYamlNode("[ one, two ]")
            )
        }
    }

    @Test
    fun `keeps on and off scalars as strings`() {
        val map = YamlNodeConversion.toParameterMap(
            Yaml.default.parseToYamlNode("{ on: on, off: off }")
        )
        assertEquals("on", map["on"], "on stays a string, not a boolean")
        assertEquals("off", map["off"], "off stays a string, not a boolean")
    }

    @Test
    fun `resolves large integers as longs`() {
        val map = YamlNodeConversion.toParameterMap(
            Yaml.default.parseToYamlNode("{ big: 12345678901 }")
        )
        assertEquals(
            12345678901L, map["big"],
            "an integer beyond the Int range resolves to Long"
        )
    }
}

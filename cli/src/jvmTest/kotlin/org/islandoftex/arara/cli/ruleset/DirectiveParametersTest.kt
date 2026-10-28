// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.rules.Directives

class DirectiveParametersTest {
    @BeforeTest
    fun prepareTest() { DirectiveUtils.initializeDirectiveCore() }

    @AfterTest
    fun cleanTest() { DirectiveUtils.initializeDirectiveCore() }

    private fun parameters(text: String): Map<String, Any> = Directives.extractDirectives(
        listOf("% arara: probe: $text"), false, FileType("tex", "^\\s*%\\s+")
    ).single().parameters

    @Test
    fun `preserve strings and nested directive parameter collections`() {
        val values = parameters("{ label: 'with spaces', values: [one, two], nested: { key: value } }")
        assertEquals("with spaces", values["label"])
        assertEquals(listOf("one", "two"), values["values"])
        assertEquals(mapOf("key" to "value"), values["nested"])
    }

    @Test
    fun `decode ordinary numeric and boolean directive parameters`() {
        val values = parameters("{ count: 42, ratio: 1.5, enabled: true, disabled: false }")
        assertEquals(42, values["count"])
        assertEquals(1.5, values["ratio"])
        assertEquals(true, values["enabled"])
        assertEquals(false, values["disabled"])
    }

    @Test
    fun `keep quoted booleans and numeric strings literal`() {
        assertEquals(
            mapOf("enabled" to "true", "count" to "001", "empty" to ""),
            parameters("{ enabled: 'true', count: '001', empty: '' }")
        )
    }

    @Test
    fun `wrap malformed parameter YAML as an application error`() {
        assertFailsWith<AraraException> { parameters("{ values: [one }") }
    }

    @Test
    fun `retain traditional yes and no rule argument strings`() {
        assertEquals(mapOf("synctex" to "yes", "draft" to "no"), parameters("{ synctex: yes, draft: no }"))
    }
}

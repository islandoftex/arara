// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.rules.Directives

class DirectiveParametersTest : ShouldSpec({
    beforeTest { DirectiveUtils.initializeDirectiveCore() }
    afterTest { DirectiveUtils.initializeDirectiveCore() }

    fun parameters(text: String): Map<String, Any> = Directives.extractDirectives(
        listOf("% arara: probe: $text"), false, FileType("tex", "^\\s*%\\s+")
    ).single().parameters

    should("preserve strings and nested directive parameter collections") {
        val values = parameters("{ label: 'with spaces', values: [one, two], nested: { key: value } }")
        values["label"] shouldBe "with spaces"
        values["values"] shouldBe listOf("one", "two")
        values["nested"] shouldBe mapOf("key" to "value")
    }

    should("decode ordinary numeric and boolean directive parameters") {
        val values = parameters("{ count: 42, ratio: 1.5, enabled: true, disabled: false }")
        values["count"] shouldBe 42
        values["ratio"] shouldBe 1.5
        values["enabled"] shouldBe true
        values["disabled"] shouldBe false
    }

    should("keep quoted booleans and numeric strings literal") {
        parameters("{ enabled: 'true', count: '001', empty: '' }") shouldBe
            mapOf("enabled" to "true", "count" to "001", "empty" to "")
    }

    should("wrap malformed parameter YAML as an application error") {
        shouldThrow<AraraException> { parameters("{ values: [one }") }
    }

    should("retain traditional yes and no rule argument strings") {
        parameters("{ synctex: yes, draft: no }") shouldBe mapOf("synctex" to "yes", "draft" to "no")
    }
})

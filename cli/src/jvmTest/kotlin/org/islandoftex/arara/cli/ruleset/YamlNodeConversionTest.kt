// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.charleskorn.kaml.Yaml
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.rules.Directives

class YamlNodeConversionTest : ShouldSpec({
    fun convert(text: String) = YamlNodeConversion.toParameterMap(Yaml.default.parseToYamlNode(text))

    afterTest { DirectiveUtils.initializeDirectiveCore() }

    should("preserve quoted scalars at every nesting level") {
        convert("{ values: ['false', '001', { number: \"1.5\", empty: '' }] }") shouldBe
            mapOf("values" to listOf("false", "001", mapOf("number" to "1.5", "empty" to "")))
    }

    should("resolve decimal scalars without truncating integers") {
        convert("{ small: 42, large: 2147483648, maximum: 9223372036854775807, " +
            "minimum: -9223372036854775808, overflow: 9223372036854775808, fraction: .5, exponent: 1e3 }") shouldBe
            mapOf("small" to 42, "large" to 2147483648L, "maximum" to Long.MAX_VALUE,
                "minimum" to Long.MIN_VALUE, "overflow" to "9223372036854775808", "fraction" to 0.5,
                "exponent" to 1000.0)
    }

    should("keep non decimal forms and yes no switches as strings") {
        convert("{ yes: yes, no: no, on: on, off: off, hex: 0xff, infinity: .inf, nan: .nan }") shouldBe
            mapOf("yes" to "yes", "no" to "no", "on" to "on", "off" to "off", "hex" to "0xff",
                "infinity" to ".inf", "nan" to ".nan")
        convert("{ upper: TRUE, title: False }") shouldBe mapOf("upper" to true, "title" to false)
    }

    should("reject missing values and non map roots") {
        listOf("[]", "scalar", "{ value: null }", "{ value: ~ }", "{ value: }",
            "{ nested: [one, null] }").forEach { text ->
            shouldThrow<IllegalArgumentException> { convert(text) }
        }
    }

    should("wrap parser and conversion failures with directive line information in both engines") {
        RuleEngine.entries.forEach { engine ->
            DirectiveUtils.initializeDirectiveCore(engine)
            listOf("{ value: null }", "{ value: [unterminated }").forEach { text ->
                val failure = shouldThrow<AraraException> {
                    Directives.extractDirectives(listOf("", "% arara: probe: $text"), false,
                        FileType("tex", "^\\s*%\\s+"))
                }
                check("(2)" in failure.message.orEmpty()) { failure.message.orEmpty() }
                check(failure.exception != null) { "Original YAML failure was discarded" }
            }
        }
    }
})

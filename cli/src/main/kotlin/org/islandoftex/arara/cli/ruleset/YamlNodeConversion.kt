// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar

/** Converts directive YAML to interpreter values; quoted scalars remain strings. */
internal object YamlNodeConversion {
    fun toParameterMap(node: YamlNode): Map<String, Any> = when (node) {
        is YamlMap -> node.entries
            .mapKeys { (key, _) -> key.content }
            .mapValues { (_, value) -> toPlainKotlinValue(value) }
        else -> throw IllegalArgumentException(
            "Directive parameters have to form a map."
        )
    }

    private fun toPlainKotlinValue(node: YamlNode): Any = when (node) {
        is YamlMap -> node.entries
            .mapKeys { (key, _) -> key.content }
            .mapValues { (_, value) -> toPlainKotlinValue(value) }
        is YamlList -> node.items.map { toPlainKotlinValue(it) }
        is YamlScalar -> toScalarValue(node)
        is YamlNull -> throw IllegalArgumentException(
            "Directive parameters must not carry null values."
        )
        else -> throw IllegalArgumentException(
            "Unsupported YAML node in a directive parameter map."
        )
    }

    private fun toScalarValue(scalar: YamlScalar): Any {
        val content = scalar.content
        if (!scalar.plain) return content
        return when {
            booleanRegex.matches(content) ->
                content.equals("true", ignoreCase = true)
            integerRegex.matches(content) ->
                content.toIntOrNull() ?: content.toLongOrNull() ?: content
            floatRegex.matches(content) -> content.toDouble()
            else -> content
        }
    }

    private val booleanRegex = Regex("true|True|TRUE|false|False|FALSE")
    private val integerRegex = Regex("[-+]?[0-9]+")
    private val floatRegex =
        Regex("[-+]?(\\.[0-9]+|[0-9]+(\\.[0-9]*)?)([eE][-+]?[0-9]+)?")
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlNode

/**
 * Converts a parsed YAML node tree of a directive parameter map into the
 * plain Kotlin values the interpreter expects (mirroring the untyped
 * decoding the former directive parser performed).
 *
 * Maps become `Map<String, Any>`, sequences become `List<Any>` and
 * scalars resolve according to the YAML 1.2 core schema — but only when
 * written plainly: a quoted scalar is always a string, preserving the
 * author's intent (for example, `shell: 'yes'` and `shell: yes` are
 * both the string `yes`, as `yes` is not a YAML 1.2 boolean).
 *
 * Null values are rejected: directive parameters must carry a value.
 */
internal object YamlNodeConversion {
    /**
     * Convert the root node of a directive parameter map, requiring it to
     * hold a map.
     *
     * @param node The parsed YAML root node.
     * @return The plain Kotlin map of directive parameters.
     * @throws IllegalArgumentException The root is not a map.
     */
    fun toParameterMap(node: YamlNode): Map<String, Any> = when (node) {
        is YamlMap -> node.entries
            .mapKeys { (key, _) -> key.content }
            .mapValues { (_, value) -> toPlainKotlinValue(value) }
        else -> throw IllegalArgumentException(
            "Directive parameters have to form a map."
        )
    }

    /**
     * Convert the provided node into a plain Kotlin value.
     *
     * @param node The parsed YAML node.
     * @return The plain Kotlin value: a map, a list or a scalar value.
     * @throws IllegalArgumentException The node holds a null value.
     */
    fun toPlainKotlinValue(node: YamlNode): Any = when (node) {
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

    /**
     * Resolve a scalar according to the YAML 1.2 core schema, honoring
     * the quoting style: quoted scalars are always strings. The forms
     * accepted are deliberately conservative: booleans (`true`, `false`
     * and their case variants), decimal integers, and decimal floats
     * with optional exponent - anything else is a string.
     */
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

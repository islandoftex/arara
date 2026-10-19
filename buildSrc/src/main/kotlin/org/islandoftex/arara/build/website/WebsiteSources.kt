// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build.website

import java.io.File

/** Stages unchanged Markdown for Zola's component syntax. */
object WebsiteSources {
    private const val FRONT_MATTER = "+++"
    private const val README_HEADER_LINES = 7
    private val token = Regex(
        """\{\{\s*(?<inline>tex|latex|get_url)\((?<inlineArgs>.*?)\)\s*}}|""" +
            """\{%\s*(?<block>hint|nicetoknow|messagebox)\((?<blockArgs>.*?)\)\s*%}|\{%\s*(?<end>end)\s*%}""",
        RegexOption.DOT_MATCHES_ALL
    )
    private val argument = Regex(
        """\s*(?<key>[a-zA-Z_]\w*)\s*=\s*(?<quote>['"])(?<value>(?:\\.|(?!\k<quote>).)*)\k<quote>\s*(,|$)"""
    )

    private fun arguments(source: String): Map<String, String> {
        val values = linkedMapOf<String, String>()
        var position = 0
        while (source.substring(position).isNotBlank()) {
            val match = argument.find(source, position)
            require(match != null && match.range.first == position) {
                "Expected named string shortcode arguments: $source"
            }
            val key = match.groups["key"]!!.value
            require(key !in values) { "Duplicate shortcode argument: $key" }
            val value = match.groups["value"]!!.value.replace(Regex("""\\(.)""")) {
                when (it.groupValues[1]) {
                    "n" -> "\n"
                    "r" -> "\r"
                    "t" -> "\t"
                    "\\", "'", "\"" -> it.groupValues[1]
                    else -> it.value
                }
            }
            values[key] = value
            position = match.range.last + 1
        }
        return values
    }

    private fun literal(value: String): String = "\"" + value
        .replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\""

    private fun raw(value: String): String {
        require("{% endraw %}" !in value) { "Unexpected endraw in legacy content" }
        return if (value.isEmpty()) "" else "{% raw %}$value{% endraw %}"
    }

    fun adapt(source: String): String {
        require(source.startsWith(FRONT_MATTER)) { "Missing TOML front matter" }
        val frontEnd = source.indexOf(FRONT_MATTER, FRONT_MATTER.length)
        require(frontEnd >= 0) { "Unclosed front matter" }
        val body = source.substring(frontEnd + FRONT_MATTER.length)
        val tokens = token.findAll(body).toList()
        val stack = mutableListOf<String>()
        val output = StringBuilder(source.substring(0, frontEnd + FRONT_MATTER.length) + "\n")
        var end = 0
        tokens.forEachIndexed { index, match ->
            output.append(raw(body.substring(end, match.range.first)))
            val inline = match.groups["inline"]?.value.orEmpty()
            val block = match.groups["block"]?.value.orEmpty()
            when {
                inline.isNotEmpty() -> {
                    val args = arguments(match.groups["inlineArgs"]!!.value)
                    if (inline == "get_url") {
                        require(args.keys == setOf("url")) { "get_url requires url" }
                        output.append("{{< arara.url url={${literal(args.getValue("url"))}} />}}")
                    } else {
                        require(args.isEmpty()) { "Logo shortcodes do not accept arguments" }
                        output.append(raw(if (inline == "tex") "<span class=\"tex\">T<sub>e</sub>X</span>"
                        else "<span class=\"latex\">L<sup>a</sup>T<sub>e</sub>X</span>"))
                    }
                }
                block.isNotEmpty() -> {
                    val args = arguments(match.groups["blockArgs"]!!.value)
                    require(args.keys.all { it in setOf("title", "reference") }) { "Unknown callout arguments" }
                    var depth = 1
                    val closing = tokens.drop(index + 1).firstOrNull {
                        if (it.groups["block"] != null) depth++
                        if (it.groups["end"] != null) depth--
                        depth == 0
                    } ?: error("Unclosed legacy shortcode")
                    val before = body.substring(0, match.range.first).substringAfterLast('\n')
                    val indent = before.takeIf { it.isBlank() }.orEmpty()
                    val prefixes = body.substring(match.range.last + 1, closing.range.first).lineSequence()
                        .filter { it.isNotBlank() }.map { it.takeWhile { c -> c == ' ' || c == '\t' } }.toList()
                    val dedent = prefixes.reduceOrNull { a, b -> a.commonPrefixWith(b) }.orEmpty()
                    val attrs = mapOf("indent" to indent, "dedent" to dedent) + args
                    output.append("{% <arara.$block")
                    attrs.forEach { (key, value) -> output.append(" $key={${literal(value)}}") }
                    output.append("> %}")
                    stack.add(block)
                }
                else -> {
                    require(stack.isNotEmpty()) { "Unexpected shortcode end" }
                    output.append("{% </arara.${stack.removeAt(stack.lastIndex)}> %}")
                }
            }
            end = match.range.last + 1
        }
        require(stack.isEmpty()) { "Unclosed legacy shortcode" }
        return output.append(raw(body.substring(end))).toString()
    }

    fun prepare(repo: File, destination: File, theme: File) {
        require(!destination.exists()) { "Destination already exists: $destination" }
        require(theme.resolve("theme.toml").isFile) { "Initialize the DevLab theme submodule: $theme" }
        copyTree(repo.resolve("website"), destination, setOf("themes", "public", ".git"))
        copyTree(theme, destination.resolve("themes/devlab-theme"), setOf(".git"))
        val includes = listOf(
            Triple("README.md", "_index.md", README_HEADER_LINES), Triple("CHANGELOG.md", "CHANGELOG.md", 2)
        )
        for ((source, target, skip) in includes) {
            val text = repo.resolve(source).readLines().drop(skip).joinToString("\n", postfix = "\n")
            destination.resolve("content/$target")
                .appendText(if (source == "CHANGELOG.md") text.replace("## ", "# ") else text)
        }
        destination.resolve("content").walkTopDown().filter { it.isFile && it.extension == "md" }
            .forEach { it.writeText(adapt(it.readText())) }
    }

    private fun copyTree(source: File, target: File, exclude: Set<String>) {
        source.walkTopDown().onEnter { it.name !in exclude }.filter { it.name !in exclude }.forEach { file ->
            val output = target.resolve(file.relativeTo(source).path)
            if (file.isDirectory) output.mkdirs() else {
                file.copyTo(output, overwrite = true)
                check(output.setWritable(true)) { "Cannot make staged file writable: $output" }
            }
        }
    }
}

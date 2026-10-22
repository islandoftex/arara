// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build.website

import java.io.File
import java.net.URI

internal data class HtmlTag(val name: String, val attributes: Map<String, String>, val closing: Boolean,
                            val empty: Boolean, val start: Int, val end: Int)

/** Scans tags without reserializing code examples, entities, or whitespace. */
internal fun htmlTags(source: String): Sequence<HtmlTag> = sequence {
    val token = Regex(
        """<!--.*?-->|<![^>]*>|<(?<closing>/?)(?<name>[A-Za-z][\w:-]*)""" +
            """(?<attributes>(?:"[^"]*"|'[^']*'|[^'">])*)>""", RegexOption.DOT_MATCHES_ALL
    )
    val attribute = Regex(
        """(?<name>[\w:-]+)(?:\s*=\s*(?:"(?<double>[^"]*)"|'(?<single>[^']*)'|(?<bare>[^\s"'=<>`]+)))?"""
    )
    val voids = setOf(
        "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr"
    )
    var rawTag: String? = null
    for (match in token.findAll(source)) {
        val name = match.groups["name"]?.value.orEmpty().lowercase()
        val closing = match.groups["closing"]?.value == "/"
        val insideRawBody = rawTag != null && !(closing && name == rawTag)
        if (name.isEmpty() || insideRawBody) continue
        val attrs = attribute.findAll(match.groups["attributes"]!!.value).associate {
            val value = listOf("double", "single", "bare").firstNotNullOfOrNull { key -> it.groups[key]?.value }
            it.groups["name"]!!.value.lowercase() to value.orEmpty()
        }
        val empty = name in voids || match.value.endsWith("/>")
        yield(HtmlTag(name, attrs, closing, empty, match.range.first, match.range.last + 1))
        if (closing && name == rawTag) rawTag = null
        else if (!closing && !empty && name in setOf("script", "style")) rawTag = name
    }
}

private class DocumentRegions {
    val stack = mutableListOf<HtmlTag>()
    val active = mutableMapOf<String, HtmlTag>()
    val regions = mutableMapOf<String, String>()

    fun open(tag: HtmlTag) {
        for (key in listOf("title", "content")) {
            if ("data-arara-document-$key" in tag.attributes) {
                require(key !in active && key !in regions && !tag.empty) {
                    "Duplicate or invalid document $key"
                }
                active[key] = tag
            }
        }
        if (!tag.empty) stack.add(tag)
    }

    fun close(tag: HtmlTag, source: String) {
        val index = stack.indexOfLast { it.name == tag.name }
        if (index < 0) return
        for ((key, opening) in active.toMap()) {
            if (opening == stack[index]) {
                val value = source.substring(opening.end, tag.start).trim()
                require(value.isNotEmpty()) { "Empty document $key" }
                regions[key] = value
                active.remove(key)
            }
        }
        stack.subList(index, stack.size).clear()
    }
}

data class WebsiteContent(val title: String, val body: String) {
    companion object {
        private const val ASSET_HEADER_BYTES = 128
        private const val LFS_POINTER = "version https://git-lfs.github.com/spec/v1"
        const val SITE_URL = "https://islandoftex.gitlab.io/arara"
        val chapters = listOf("introduction", "concepts", "cli", "configuration", "logging", "projects",
            "database", "methods", "rules", "building", "deploying", "yaml", "mvel", "lua")

        fun read(source: String): WebsiteContent {
            val document = DocumentRegions()
            for (tag in htmlTags(source)) {
                if (tag.closing) document.close(tag, source) else document.open(tag)
            }
            require(document.active.isEmpty() && document.regions.keys == setOf("title", "content")) {
                "Expected one complete document title and content region"
            }
            return WebsiteContent(document.regions.getValue("title"), document.regions.getValue("content"))
        }

        fun localizeAssets(content: String, root: File, siteUrl: String = SITE_URL): String {
            val prefix = siteUrl.trimEnd('/') + "/"
            return Regex("""src=(["'])(.*?)\1""").replace(content) { match ->
                val url = match.groupValues[2]
                if (!url.startsWith(prefix)) match.value else {
                    val relative = URI(url.substring(prefix.length)).path
                    val asset = root.resolve(relative).canonicalFile
                    require(asset.toPath().startsWith(root.canonicalFile.toPath()) && asset.isFile) {
                        "Missing website asset: $asset"
                    }
                    asset.inputStream().use { stream ->
                        val head = ByteArray(ASSET_HEADER_BYTES)
                        val size = stream.read(head)
                        require(size < 0 || !String(head, 0, size, Charsets.UTF_8).startsWith(LFS_POINTER)) {
                            "Unresolved LFS pointer: $asset"
                        }
                    }
                    "src=${match.groupValues[1]}${asset.toURI().toASCIIString()}${match.groupValues[1]}"
                }
            }
        }

        fun checkSite(root: File, siteUrl: String = SITE_URL) {
            val pages = chapters.map { "manual/$it/index.html" } + "quickstart/index.html"
            pages.forEach { page ->
                val document = read(root.resolve(page).readText())
                localizeAssets(document.body, root, siteUrl)
                require(LFS_POINTER !in document.body) { "Unresolved LFS pointer: $page" }
            }
            println("Verified ${chapters.size} manual chapters and the quickstart")
        }
    }
}

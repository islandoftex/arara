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

object WebsiteContent {
    private const val ASSET_HEADER_BYTES = 128
    private const val LFS_POINTER = "version https://git-lfs.github.com/spec/v1"
    const val SITE_URL = "https://islandoftex.gitlab.io/arara"
    val books = listOf("manual", "quickstart")

    fun localizeAssets(content: String, root: File, siteUrl: String = SITE_URL): String {
        val prefix = siteUrl.trimEnd('/') + "/"
        return Regex("""(?<attribute>src|href)=(?<quote>["'])(?<url>.*?)\k<quote>""").replace(content) { match ->
            val url = match.groups["url"]!!.value
            val attribute = match.groups["attribute"]!!.value
            if (!url.startsWith(prefix) || (attribute == "href" && !URI(url).path.endsWith(".css"))) {
                match.value
            } else {
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
                val quote = match.groups["quote"]!!.value
                "$attribute=$quote${asset.toURI().toASCIIString()}$quote"
            }
        }
    }

    fun checkSite(root: File, siteUrl: String = SITE_URL) {
        for (book in books) {
            val source = root.resolve("books/$book/index.html").readText()
            val tags = htmlTags(source).toList()
            require(tags.count { it.attributes["data-arara-book"] == book } == 1) { "Missing book layout: $book" }
            val chapters = tags.mapNotNull { it.attributes["data-arara-book-chapter"] }
            require(chapters.isNotEmpty() && chapters.distinct() == chapters) { "Missing or duplicate chapters: $book" }
            chapters.forEach { path ->
                require(root.resolve(path.trim('/')).resolve("index.html").isFile) { "Missing website chapter: $path" }
            }
            val ids = tags.mapNotNull { it.attributes["id"] }
            require(ids.distinct() == ids) { "Duplicate book anchors: $book" }
            tags.mapNotNull { it.attributes["href"] }.filter { it.startsWith('#') && it.length > 1 }.forEach {
                require(URI(it).fragment in ids) { "Missing book anchor: $it in $book" }
            }
            localizeAssets(source, root, siteUrl)
            require(LFS_POINTER !in source) { "Unresolved LFS pointer: $book" }
        }
        println("Verified manual and quickstart book layouts and their website chapters")
    }
}

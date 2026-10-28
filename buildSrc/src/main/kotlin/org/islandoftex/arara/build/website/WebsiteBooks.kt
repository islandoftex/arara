// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build.website

import java.io.File

class WebsiteBooks(private val site: File, private val resources: File,
                   private val siteUrl: String = WebsiteContent.SITE_URL) {
    private fun demoteHeadings(body: String): String = Regex("(<\\/?h)([1-5])(?=[\\s>])").replace(body) {
        it.groupValues[1] + (it.groupValues[2].toInt() + 1)
    }

    private data class Heading(val level: Int, val id: String, val children: MutableList<Heading> = mutableListOf())

    private fun contents(body: String): String {
        val roots = mutableListOf<Heading>()
        val parents = mutableListOf<Heading>()
        htmlTags(body).filter { !it.closing && it.name.matches(Regex("h[1-6]")) && "id" in it.attributes }.forEach {
            val heading = Heading(it.name.last().digitToInt(), it.attributes.getValue("id"))
            while (parents.isNotEmpty() && parents.last().level >= heading.level) parents.removeAt(parents.lastIndex)
            (parents.lastOrNull()?.children ?: roots).add(heading)
            parents.add(heading)
        }
        fun render(headings: List<Heading>): String = headings.joinToString("", "<ul>", "</ul>") {
            "<li><a href=\"#${it.id}\"></a>" + (if (it.children.isEmpty()) "" else render(it.children)) + "</li>"
        }
        return "<article id=\"contents\"><h1>Contents</h1>${render(roots)}</article>"
    }

    fun assemble(output: File, manual: Boolean, version: String) {
        val sourcePages = if (manual) WebsiteContent.chapters.map { "manual/$it/index.html" }
        else listOf("quickstart/index.html")
        val body = sourcePages.joinToString("\n") { page ->
            val document = WebsiteContent.read(site.resolve(page).readText())
            val chapter = page.substringAfter("manual/").substringBefore('/')
            val heading = if (manual) "<h1 id=\"chapter-$chapter\">${document.title}</h1>"
            else "<h1>A quickstart guide to arara</h1>"
            val content = demoteHeadings(WebsiteContent.localizeAssets(document.body, site, siteUrl))
            "<article>$heading\n$content</article>"
        }
        val siteRoot = site.toURI().toASCIIString().trimEnd('/')
        val resourceRoot = resources.toURI().toASCIIString().trimEnd('/')
        val header = if (manual) resources.resolve("manual-cover.html").readText()
            .replace("@RESOURCE_ROOT@", resourceRoot)
            .replace("@SITE_ROOT@", siteRoot).replace("@VERSION@", version)
        else """<!DOCTYPE html>
<html lang="en-US"><head><meta charset="UTF-8"><title>arara</title>
<link href="$siteRoot/fonts/identity.css" rel="stylesheet">
<link href="$resourceRoot/manual.css" rel="stylesheet"></head><body>
"""
        var html = header + (if (manual) contents(body) else "") + body + "\n</body></html>\n"
        if (manual) {
            val prefix = siteUrl.trimEnd('/') + "/manual/"
            html = Regex("""href=(["'])${Regex.escape(prefix)}([^/"']+)/\1""").replace(html) {
                "href=${it.groupValues[1]}#chapter-${it.groupValues[2]}${it.groupValues[1]}"
            }
        }
        output.parentFile.mkdirs()
        output.writeText(html)
    }
}

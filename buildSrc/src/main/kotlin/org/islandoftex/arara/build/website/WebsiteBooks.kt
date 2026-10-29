// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build.website

import java.io.File

/** Resolves local assets and release metadata in Zola's standalone book output. */
class WebsiteBooks(private val site: File, private val siteUrl: String = WebsiteContent.SITE_URL) {
    fun prepare(output: File, manual: Boolean, version: String) {
        val book = if (manual) "manual" else "quickstart"
        val source = site.resolve("books/$book/index.html").readText()
        val escapedVersion = version.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val html = WebsiteContent.localizeAssets(source, site, siteUrl)
            .replace("<!-- arara-book-version -->", "Version $escapedVersion")
        output.parentFile.mkdirs()
        output.writeText(html)
    }
}

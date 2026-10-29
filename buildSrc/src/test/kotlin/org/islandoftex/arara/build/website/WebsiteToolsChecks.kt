// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build.website

import java.io.File
import java.nio.file.Files

/** Shared regression cases run by buildSrc:check and the offline Nix website build. */
fun main(args: Array<String>) {
    val repo = File(args[0])
    val theme = File(args.getOrElse(1) { repo.resolve("website/themes/devlab-theme").path })
    var count = 0
    fun test(name: String, body: () -> Unit) {
        try { body(); count++; println("PASS $name") }
        catch (failure: Exception) { throw IllegalStateException("Failed: $name", failure) }
    }
    fun adapt(body: String) = WebsiteSources.adapt("+++\ntitle = \"Existing\"\nweight = 3\n+++\n$body")
    fun fails(body: () -> Unit) {
        var failed = false
        try { body() } catch (_: Exception) { failed = true }
        check(failed) { "Expected failure" }
    }
    fun temporary(body: (File) -> Unit) {
        val root = Files.createTempDirectory("arara website checks ").toFile()
        try { body(root) } finally { root.deleteRecursively() }
    }

    test("front matter stays unchanged") {
        check(adapt("Hello").startsWith("+++\ntitle = \"Existing\"\nweight = 3\n+++\n"))
    }
    test("literal template examples stay raw") {
        check("{% raw %}\n```lua\n{{ value }}\n{% something %}\n```{% endraw %}" in adapt("```lua\n{{ value }}\n{% something %}\n```"))
    }
    test("logos and URL calls retain their meaning") {
        val result = adapt("{{ tex() }} {{ latex() }} {{ get_url(url=\"/images/a.svg\") }}")
        check("<span class=\"tex\">T<sub>e</sub>X</span>" in result)
        check("<span class=\"latex\">L<sup>a</sup>T<sub>e</sub>X</span>" in result)
        check("{{< arara.url url={\"/images/a.svg\"} />}}" in result)
    }
    test("nested callouts close in order") {
        val result = adapt("{% hint() %}outer {% messagebox(title=\"Inside\") %}inner{% end %}{% end %}")
        check("{% </arara.messagebox> %}{% </arara.hint> %}" in result)
    }
    test("callout bodies retain code indentation inside lists") {
        val result = adapt("    {% hint() %}\n A note\n\n ```yaml\n value: >\n     command\n ```\n    {% end %}")
        check("indent={\"    \"} dedent={\" \"}" in result)
        check("     command" in result)
    }
    test("invalid callouts and arguments fail") {
        listOf("{% end %}", "{% hint() %}unfinished", "{% hint(unknown='x') %}body{% end %}",
            "{% hint(title=call()) %}body{% end %}", "{{ get_url() }}").forEach { source -> fails { adapt(source) } }
    }
    test("quoted arguments support commas, escapes, and Unicode") {
        val result = adapt("{% messagebox(title='Hello, \\\'é\\\'') %}body{% end %}")
        check("title={\"Hello, 'é'\"}" in result)
    }
    test("staging preserves sources and front matter") {
        temporary { root ->
            val originals = repo.resolve("website/content").walkTopDown().filter { it.isFile }.associateWith { it.readBytes() }
            val output = root.resolve("site")
            WebsiteSources.prepare(repo, output, theme)
            for ((file, bytes) in originals) {
                check(file.readBytes().contentEquals(bytes))
                check(file.readText().split("+++", limit = 3)[1] ==
                    output.resolve("content/${file.relativeTo(repo.resolve("website/content"))}").readText().split("+++", limit = 3)[1])
            }
            fails { WebsiteSources.prepare(repo, output, theme) }
            check(output.resolve("themes/devlab-theme/theme.toml").isFile)
            check(output.resolve("content/_index.md").readText().contains("arara"))
        }
    }
    test("PDF images are local and percent-encoded paths work") {
        temporary { root ->
            val asset = root.resolve("image with spaces.svg").apply { writeText("<svg/>") }
            val body = "<img src='https://example.org/arara/image%20with%20spaces.svg'>"
            check(asset.toURI().toASCIIString() in WebsiteContent.localizeAssets(body, root, "https://example.org/arara"))
            fails { WebsiteContent.localizeAssets(body.replace("image%20with%20spaces", "missing"), root, "https://example.org/arara") }
        }
    }
    test("book stylesheets are local but ordinary website links stay public") {
        temporary { root ->
            val style = root.resolve("print.css").apply { writeText("body { color: black; }") }
            val html = "<link href='https://example.org/arara/print.css' rel='stylesheet'>" +
                "<a href='https://example.org/arara/manual/rules/'>Rules</a>"
            val result = WebsiteContent.localizeAssets(html, root, "https://example.org/arara")
            check(style.toURI().toASCIIString() in result)
            check("href='https://example.org/arara/manual/rules/'" in result)
            style.delete()
            fails { WebsiteContent.localizeAssets(html, root, "https://example.org/arara") }
        }
    }
    test("missing and unresolved LFS assets fail") {
        temporary { root ->
            val body = "<img src=\"${WebsiteContent.SITE_URL}/image.svg\">"
            fails { WebsiteContent.localizeAssets(body, root) }
            root.resolve("image.svg").writeText("version https://git-lfs.github.com/spec/v1\noid sha256:fake\n")
            fails { WebsiteContent.localizeAssets(body, root) }
        }
    }
    test("book preparation uses immutable Zola books without reading website pages") {
        temporary { root ->
            val site = root.resolve("site")
            site.resolve("fonts").mkdirs()
            site.resolve("fonts/identity.css").writeText("/* shared fonts */")
            for (book in WebsiteContent.books) {
                val page = site.resolve("books/$book/index.html").apply { parentFile.mkdirs() }
                val source = "<html><head><link href=\"${WebsiteContent.SITE_URL}/fonts/identity.css\" " +
                    "rel=\"stylesheet\"></head><body data-arara-book=\"$book\">" +
                    "<!-- <h1 id='chapter'> -->" +
                    "<script>const text = \"<h1 id='chapter'>\";</script>" +
                    "<!-- arara-book-version --><article data-arara-book-chapter=\"/source/\">" +
                    "<h1 id=\"chapter\">Title</h1><a href=\"#chapter\">Section</a>" +
                    "<pre>&lt;rule&gt; &amp; value\n  indented</pre></article></body></html>"
                page.writeText(source)
                page.setWritable(false)
                val output = root.resolve("$book.html")
                WebsiteBooks(site).prepare(output, book == "manual", "test<&version")
                check("Version test&lt;&amp;version" in output.readText())
                check("<pre>&lt;rule&gt; &amp; value\n  indented</pre>" in output.readText())
                check(site.resolve("fonts/identity.css").toURI().toASCIIString() in output.readText())
                check(page.readText() == source)
            }
            fails { WebsiteContent.checkSite(site) }
            site.resolve("source/index.html").apply { parentFile.mkdirs(); writeText("Website layout is independent") }
            WebsiteContent.checkSite(site)
            val manual = site.resolve("books/manual/index.html").apply { setWritable(true) }
            val valid = manual.readText()
            manual.writeText(valid.replace("href=\"#chapter\"", "href=\"#missing\""))
            fails { WebsiteContent.checkSite(site) }
            manual.writeText(valid.replace("</article>", "<h2 id=\"chapter\">Duplicate</h2></article>"))
            fails { WebsiteContent.checkSite(site) }
            manual.delete()
            fails { WebsiteBooks(site).prepare(root.resolve("missing.html"), true, "version") }
        }
    }
    test("shared identity fonts are local resolved WOFF2 assets") {
        val fonts = repo.resolve("website/static/fonts")
        val css = fonts.resolve("identity.css").readText()
        val references = Regex("""url\("([^"]+)"\)""").findAll(css)
            .map { it.groupValues[1] }.toList()
        check(references.isNotEmpty())
        references.forEach { name ->
            check('/' !in name) { "Font must be beside the shared stylesheet: $name" }
            val signature = fonts.resolve(name).inputStream().use { it.readNBytes(4).toString(Charsets.US_ASCII) }
            check(signature == "wOF2") { "Missing or unresolved LFS font: $name" }
        }
        check("fonts/identity.css" in repo.resolve("website/static/arara.css").readText())
        check("@font-face" !in repo.resolve("website/static/books/manual.css").readText())
    }
    println("Website build checks: $count passed")
}

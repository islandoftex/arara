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
    fun document(body: String) = WebsiteContent.read(
        "<nav>Not in the book</nav><h1 data-arara-document-title>Chapter</h1>" +
            "<div data-arara-document-content>$body</div><footer>Footer</footer>"
    )
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
    test("extraction keeps only article regions") {
        val body = "<h1 id=\"example\">Example</h1><p>Keep <em>this</em>.</p>"
        check(document(body) == WebsiteContent("Chapter", body))
    }
    test("code, comments, entities, void elements, and quoted brackets stay intact") {
        val body = "<!--example--><pre>&lt;rule&gt; &amp; value\n  indented</pre><br><img alt=\"a > b\" src=\"example.svg\"/>"
        check(document(body).body == body)
    }
    test("markers in scripts do not count as regions") {
        val body = "<script>var example = '<div data-arara-document-content>';</script><p>Body</p>"
        check(document(body).body == body)
    }
    test("missing, duplicate, empty, and incomplete regions fail") {
        listOf("<div>Old theme</div>", "<h1 data-arara-document-title>Title</h1><div data-arara-document-content>Missing end",
            "<h1 data-arara-document-title>Title</h1><h1 data-arara-document-title>Again</h1><div data-arara-document-content>Body</div>")
            .forEach { source -> fails { WebsiteContent.read(source) } }
        fails { document("  \n") }
    }
    test("PDF images are local and percent-encoded paths work") {
        temporary { root ->
            val asset = root.resolve("image with spaces.svg").apply { writeText("<svg/>") }
            val body = "<img src='https://example.org/arara/image%20with%20spaces.svg'>"
            check(asset.toURI().toASCIIString() in WebsiteContent.localizeAssets(body, root, "https://example.org/arara"))
            fails { WebsiteContent.localizeAssets(body.replace("image%20with%20spaces", "missing"), root, "https://example.org/arara") }
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
    test("book assembly reads immutable pages without navigation") {
        temporary { root ->
            val site = root.resolve("site")
            val resources = root.resolve("resources").apply { mkdirs() }
            resources.resolve("manual-cover.html").writeText("<html><body><span>@VERSION@</span>")
            val source = "<nav>Not in the book</nav><h1 data-arara-document-title>Chapter</h1>" +
                "<div data-arara-document-content><h1 id=\"section\">Section</h1><p>Keep this example.</p>" +
                "<a href=\"${WebsiteContent.SITE_URL}/manual/rules/\">Rules</a></div>"
            val paths = WebsiteContent.chapters.map { "manual/$it/index.html" } + "quickstart/index.html"
            val pages = paths.map { path -> site.resolve(path).apply { parentFile.mkdirs(); writeText(source); setWritable(false) } }
            WebsiteContent.checkSite(site)
            for (manual in listOf(true, false)) {
                val html = root.resolve("$manual.html")
                WebsiteBooks(site, resources).assemble(html, manual, "test-version")
                check("Keep this example." in html.readText())
                check("Not in the book" !in html.readText())
                check("<h2 id=\"section\">" in html.readText())
                if (manual) {
                    check("test-version" in html.readText())
                    check("href=\"#chapter-rules\"" in html.readText())
                    check("id=\"contents\"" in html.readText())
                }
            }
            check(pages.all { it.readText() == source })
            pages.first().setWritable(true)
            pages.first().delete()
            fails { WebsiteContent.checkSite(site) }
            fails { WebsiteBooks(site, resources).assemble(root.resolve("missing.html"), true, "version") }
        }
    }
    println("Website build checks: $count passed")
}

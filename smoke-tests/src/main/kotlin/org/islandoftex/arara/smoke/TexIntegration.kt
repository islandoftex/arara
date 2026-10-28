// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath

private const val TEX_TIMEOUT_SECONDS = 120L
private const val PDF_HEADER = "%PDF-"
private const val BIBLIOGRAPHY_LATEX_PASSES = 3

private class TexIntegration(private val options: Map<String, String>, private val engine: String) {
    private val jar = MPPPath(options.getValue("--jar")).normalize()
    private val jna = MPPPath(options["--jna"] ?: jar.resolveSibling("jna.jar").toString()).normalize()
    private val launcher = listOf(options.getValue("--java"), "--module-path", jna.toString(),
        "--add-modules=com.sun.jna", "--enable-native-access=com.sun.jna")

    private fun execute(
        root: MPPPath, label: String = "run", expected: Int = 0, reference: String = "run.tex",
        flags: List<String> = emptyList()
    ): String {
        val result = run(launcher + listOf("-Duser.home=$root", "-jar", jar.toString(), "-L", "en",
            "--preferred-engine", engine, "--verbose", "-d", root.toString()) + flags + reference, root,
            timeoutSeconds = TEX_TIMEOUT_SECONDS)
        root.resolve("$label.output.txt").writeText(result.output)
        check(result.status == expected) { "Expected exit $expected, got ${result.status}\n${result.output}" }
        return result.output
    }

    private fun document(root: MPPPath, directives: String, body: String = "Hello, arara!") {
        root.resolve("run.tex").writeText("""
            $directives
            \documentclass{article}
            \begin{document}
            $body
            \end{document}
        """.trimIndent() + "\n")
    }

    private fun pdf(root: MPPPath) {
        check(root.resolve("run.pdf").read { it.readUtf8(PDF_HEADER.length.toLong()) }
            == PDF_HEADER) { "Missing PDF header" }
        check(root.resolve("run.log").exists) { "Missing TeX log" }
    }

    private fun typesettings(output: String): Int = Regex("This is pdfTeX").findAll(output).count()

    private fun prepare(root: MPPPath) {
        root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        for (name in listOf("pdflatex", "bibtex", "upmendex", "mptopdf")) {
            val file = "arara-rule-$name.$engine"
            val text = checkNotNull(javaClass.getResourceAsStream("/rules/$file")) { "Missing $file" }
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            root.resolve(file).writeText(text)
        }
    }

    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf<String, (MPPPath) -> Unit>(
        "real_tex_warnings_are_analyzed_without_failing_the_build" to { root ->
            document(root, "% arara: pdflatex: { interaction: nonstopmode }", """
                \PackageWarning{araraprobe}{Native warning marker}
                Hello, arara!
            """.trimIndent())
            val output = execute(root, flags =
                listOf("--console", "styled", "--log-format", "json", "--log-level", "warn"))
            check("Tool diagnostics" in output) { output }
            val records = root.resolve("arara.log").readLines()
            check(records.any { "tool.diagnostic" in it && "WARN" in it && "Native warning marker" in it }) {
                "Real TeX warning was not classified"
            }
            check(records.none { "tool.diagnostic" in it && "TRACE" in it }) {
                "Trace output escaped severity filtering"
            }
            pdf(root)
        },
        "official_pdflatex_produces_pdf" to { root ->
            document(root, "% arara: pdflatex: { interaction: nonstopmode }")
            check(typesettings(execute(root)) == 1)
            pdf(root)
        },
        "project_workspace_preserves_assets_and_conditional_database" to { root ->
            root.resolve("chapters").createDirectories()
            root.resolve("chapters/intro.tex").writeText("A staged chapter.")
            document(root, "% arara: pdflatex: { interaction: nonstopmode } if changed('tex')",
                "\\input{chapters/intro}")
            root.resolve("project.proj.lua").writeText(
                "return {files={['run.tex']={}},build={directory='build/document'}}")
            val workspace = root.resolve("build/document")
            check(typesettings(execute(root, "first", reference = "project.proj.lua")) == 1)
            pdf(workspace)
            check(!root.resolve("run.pdf").exists) { "Output escaped the build directory" }
            check(typesettings(execute(root, "unchanged", reference = "project.proj.lua")) == 0)
            val source = root.resolve("run.tex")
            source.writeText(source.readText().replace("chapters/intro}", "chapters/intro} Updated."))
            check(typesettings(execute(root, "changed", reference = "project.proj.lua")) == 1)
            pdf(workspace)
        },
        "repeated_directives_enable_synctex_on_the_final_pass" to { root ->
            document(root, "% arara: pdflatex: { synctex: false, interaction: nonstopmode }\n" +
                "% arara: pdflatex: { synctex: true, interaction: nonstopmode }")
            check(typesettings(execute(root)) == 2) { "Both directives must execute for the same input file" }
            pdf(root)
            check(root.resolve("run.synctex.gz").exists) { "Final pass did not produce SyncTeX data" }
        },
        "changed_database_skips_then_rebuilds_modified_input" to { root ->
            document(root, "% arara: pdflatex: { interaction: nonstopmode } if changed('tex')")
            check(typesettings(execute(root, "first")) == 1)
            pdf(root)
            val originalPdf = root.resolve("run.pdf").read { it.readByteArray().toList() }
            check(typesettings(execute(root, "unchanged")) == 0) { "Unchanged input was typeset again" }
            check(root.resolve("run.pdf").read { it.readByteArray().toList() } == originalPdf)
            val source = root.resolve("run.tex")
            source.writeText(source.readText().replace("Hello, arara!", "Changed document."))
            check(typesettings(execute(root, "changed")) == 1) { "Changed input was not rebuilt" }
            pdf(root)
        },
        "official_bibtex_build_resolves_citations" to { root ->
            root.resolve("references.bib").writeText("""
                @book{duck, author={Duck, Ada}, title={Arara Integration}, year={2026}, publisher={Island of TeX}}
            """.trimIndent())
            val latex = "% arara: pdflatex: { interaction: nonstopmode }"
            document(root, "$latex\n% arara: bibtex\n$latex\n$latex", """
                A citation~\cite{duck}.
                \bibliographystyle{plain}
                \bibliography{references}
            """.trimIndent())
            val output = execute(root)
            check(typesettings(output) == BIBLIOGRAPHY_LATEX_PASSES)
            check("This is BibTeX" in output)
            pdf(root)
            check("\\bibitem{duck}" in root.resolve("run.bbl").readText())
            check("undefined" !in root.resolve("run.log").readText()) { "Final pass has unresolved references" }
        },
        "official_upmendex_generates_unicode_indexes" to { root ->
            root.resolve("readings file.dic").writeText("漢字 かんじ\n")
            root.resolve("style file.ist").writeText("headings_flag 1\n")
            val entries = "\\indexentry{éclair}{1}\n\\indexentry{zèbre}{2}\n\\indexentry{漢字}{3}\n"
            for (custom in listOf(false, true)) {
                val input = if (custom) "custom idx" else "idx"
                val output = if (custom) "custom ind" else "ind"
                val log = if (custom) "custom ilg" else "ilg"
                root.resolve("run.$input").writeText(entries)
                document(root, "% arara: upmendex: { compress: true, order: letter, " +
                    "style: 'style file.ist', dictionary: 'readings file.dic', options: ['-q'], " +
                    "input: '$input', output: '$output', log: '$log' }")
                execute(root, "index-$custom")
                val index = root.resolve("run.$output").readText()
                check("\\begin{theindex}" in index) { index }
                for (entry in listOf("éclair", "zèbre", "漢字")) check(entry in index) { index }
                check(root.resolve("run.$log").isRegularFile) { "Missing index transcript" }
            }
        },
        "official_mptopdf_converts_metapost_sources" to { root ->
            root.resolve("figure.mp").writeText("""
                beginfig(1);
                draw (0,0)--(20,20);
                endfig;
                end.
            """.trimIndent() + "\n")
            document(root, "% arara: mptopdf: { files: ['figure.mp'] }")
            execute(root)
            val pdf = root.resolve("figure-1.pdf")
            check(pdf.read { it.readUtf8(PDF_HEADER.length.toLong()) } == PDF_HEADER) { "Missing converted figure" }
        },
        "tex_failure_preserves_exit_status_and_stops_later_directives" to { root ->
            document(root, "% arara: pdflatex: { interaction: nonstopmode, options: ['-halt-on-error'] }\n" +
                "% arara: pdflatex: { interaction: nonstopmode, options: ['-jobname=after-failure'] }",
                "\\araraUndefinedControlSequence")
            val output = execute(root, expected = 1)
            check("Undefined control sequence" in output)
            check(!root.resolve("run.pdf").exists)
            check(!root.resolve("after-failure.log").exists) { "A directive ran after the failed TeX command" }
        }
    ).mapValues { (_, test) -> { root: MPPPath -> prepare(root); test(root) } }
}

internal fun runTexIntegration(options: Map<String, String>) {
    val workspace = MPPPath(options["--work-dir"] ?: "build/texIntegration").normalize().apply { createDirectories() }
    val destination = MPPPath(options["--report"] ?: "build/test-results/texIntegration/results.xml").normalize()
    val cases = mutableListOf<CaseResult>()
    val preflight = runCatching {
        for (tool in listOf("pdflatex", "bibtex", "upmendex", "mptopdf")) {
            val query = if (tool in listOf("upmendex", "mptopdf")) "--help" else "--version"
            val result = run(listOf(tool, query), workspace)
            workspace.resolve("$tool.version.txt").writeText(result.output)
            check(result.status == 0) { "$tool is unavailable: ${result.output}" }
        }
    }.exceptionOrNull()
    if (preflight != null) cases.add(CaseResult("tex_toolchain", preflight)) else {
        for (engine in listOf("lua", "yaml")) {
            TexIntegration(options, engine).cases.forEach { (name, test) ->
                val root = Files.createTempDirectory(workspace.toJVMPath(), "$engine-$name ").toMPPPath()
                val failure = runCatching { test(root) }.exceptionOrNull()
                cases.add(CaseResult("${engine}_$name", failure))
                println("${if (failure == null) "PASS" else "FAIL"} ${engine}_$name ($root)")
                failure?.printStackTrace()
            }
        }
    }
    report(destination, cases, "arara.tex-integration")
    check(cases.none { it.failure != null }) { "TeX integration failed; see $destination and $workspace" }
}

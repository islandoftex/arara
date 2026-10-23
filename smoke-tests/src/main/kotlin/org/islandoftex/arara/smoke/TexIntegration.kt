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

    private fun execute(root: MPPPath, label: String = "run", expected: Int = 0): String {
        val result = run(launcher + listOf("-Duser.home=$root", "-jar", jar.toString(), "-L", "en",
            "--engine", engine, "--verbose", "-d", root.toString(), "run.tex"), root,
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
        check(root.resolve("run.pdf").read { it.readUtf8(PDF_HEADER.length.toLong()) } == PDF_HEADER) { "Missing PDF header" }
        check(root.resolve("run.log").exists) { "Missing TeX log" }
    }

    private fun typesettings(output: String): Int = Regex("This is pdfTeX").findAll(output).count()

    private fun prepare(root: MPPPath) {
        root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
        for (name in listOf("pdflatex", "bibtex")) {
            val file = "arara-rule-$name.$engine"
            val text = checkNotNull(javaClass.getResourceAsStream("/rules/$file")) { "Missing $file" }
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            root.resolve(file).writeText(text)
        }
    }

    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf<String, (MPPPath) -> Unit>(
        "official_pdflatex_produces_pdf" to { root ->
            document(root, "% arara: pdflatex: { interaction: nonstopmode }")
            check(typesettings(execute(root)) == 1)
            pdf(root)
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
        for (tool in listOf("pdflatex", "bibtex")) {
            val result = run(listOf(tool, "--version"), workspace)
            workspace.resolve("$tool.version.txt").writeText(result.output)
            check(result.status == 0) { "$tool is unavailable: ${result.output}" }
        }
    }.exceptionOrNull()
    if (preflight != null) cases.add(CaseResult("tex_toolchain", preflight)) else {
        for (engine in listOf("yaml", "lua")) {
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

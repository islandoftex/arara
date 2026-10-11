// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.File
import kotlinx.coroutines.runBlocking
import org.islandoftex.coralmark.CoralMark
import org.islandoftex.coralmark.Preset
import org.islandoftex.coralmark.TagHandler
import org.islandoftex.crocplot.KrokiClient
import org.islandoftex.crocplot.OutputFormat
import org.islandoftex.crocplot.mermaid
import org.islandoftex.svgecko.Svg
import org.islandoftex.svgecko.SvgNormalizers

/**
 * The conversion of the manual's Markdown into the LaTeX the skeleton
 * compiles.
 *
 * Every chapter opens with Zola front matter naming it: the title
 * becomes the book's `\chapter` heading, the body rides CoralMark's tag
 * rules, code fences ride the listings environments the skeleton
 * configures, mermaid fences are extracted for the diagram pass, the
 * devlab callout components render through CoralMark's native component
 * registry into the skeleton's tinted boxes, tables ride the GFM element
 * tree with their mixed content intact, and every prose character the
 * LaTeX engine would misread is escaped on arrival.
 *
 * The two books — the reference guide and the standalone quickstart —
 * share this one conversion: the skeletons decide what each book
 * contains, and the same generated chapters feed both.
 *
 * The pipeline is deliberately bridge-free: every construct the manual
 * writes is native to the conversion library, so no preprocessing shim
 * or post-hoc patch lives here. The one final pass, the TeX-family
 * logos, rewrites prose mentions into their true letterforms after the
 * components restore, leaving every verbatim region untouched; the
 * text layer it delegates to carries that story.
 */
object DocsConversion {

    /**
     * The Zola front matter every chapter carries: the block between the
     * leading `+++` fences, captured so the title can name the chapter.
     */
    private val frontMatterRegex =
        Regex("""\A\+\+\+\n(.*?)\n\+\+\+\n?""", setOf(RegexOption.DOT_MATCHES_ALL))

    /** The title entry inside the front matter. */
    private val titleRegex = Regex("""title\s*=\s*"([^"]*)"""")

    /**
     * The chapter's contribution to the book: one listings environment
     * per fenced block, reading the style the fence's language asked for.
     */
    private fun lstlistingFor(language: String, text: String): String {
        val style =
            when (language) {
                "sh", "bash" -> "sh"
                "tex", "latex" -> "tex"
                "lua" -> "lua"
                "yaml", "yml" -> "yaml"
                "bibtex" -> "bibtex"
                "java" -> "java"
                "kotlin" -> "kotlin"
                else -> "plain"
            }
        return "\\begin{lstlisting}[style=$style]\n$text\n\\end{lstlisting}"
    }

    /**
     * The extracted diagrams of one chapter: each mermaid fence takes the
     * next name, and recursive conversions share the same counter, so no
     * two diagrams ever overwrite each other.
     */
    private class DiagramNames(
        private val chapter: String,
        private val dir: File,
    ) {
        private var index = 0

        /**
         * Writes one diagram's source under the next name and returns the
         * name the include reference uses.
         */
        fun write(text: String): String {
            val name = if (index == 0) chapter else "$chapter-$index"
            index++
            dir.resolve("$name.mmd").apply { parentFile.mkdirs() }.writeText(text)
            return name
        }
    }

    /**
     * Builds the CoralMark preset that converts the manual into the LaTeX
     * the skeleton compiles.
     *
     * Two text passes run at two depths: the text processor escapes
     * every prose character while the tree walk renders, and the logo
     * pass rewrites whole words after the components restore, when
     * the verbatim regions are exactly the shapes its exclusions can
     * find.
     *
     * @param diagrams the chapter's diagram name factory, shared with the
     *   recursive conversions so extracted files stay unique
     * @param chapterNames the manual's chapter set, so references to
     *   sibling chapters resolve as live cross-references
     */
    private fun latexPreset(
        diagrams: DiagramNames,
        chapterNames: Set<String>,
    ): Preset = {
        tags {
            // headings: the chapter's internal structure
            "h1" ("\\section{" to "}")
            "h2" ("\\subsection{" to "}")
            "h3" ("\\subsubsection{" to "}")
            // inline emphasis
            "strong" ("\\textbf{" to "}")
            "em" ("\\emph{" to "}")
            "code" ("\\texttt{" to "}")
            // lists
            "ul" ("\\begin{itemize}\n" to "\n\\end{itemize}\n")
            "ol" ("\\begin{enumerate}\n" to "\n\\end{enumerate}\n")
            "li" ("\\item " to "\n")
            // paragraphs: the blank line LaTeX needs between them
            "p" ("" to "\n\n")
            // quotes and rules
            "blockquote" ("\\begin{quote}\n" to "\n\\end{quote}\n")
            "hr" ("\\par\\bigskip\\hrule\\bigskip\n" to "")
            // the element handlers below own their subtrees whole
            handler("a", linkHandler(chapterNames))
            handler("table", tableHandler(chapterNames))
            handler("pre", preHandler(diagrams))
        }
        // the callout components: the type drives the tint, the title
        // rides the box (escaped here, since params never pass through
        // the text processor), and the raw body is converted again so
        // its own prose, inline code and emphasis render properly
        components {
            "devlab.callout" { params, content ->
                val type = params["type"] ?: "info"
                val title = LatexText.escapeLatex(params["title"].orEmpty())
                val body =
                    CoralMark.convert(content.trim(), latexPreset(diagrams, chapterNames)).getOrThrow()
                "\\begin{araracallout}[callout $type]{$title}\n$body\n\\end{araracallout}"
            }
        }
        // every prose character that would be misread is escaped on
        // arrival, so the LaTeX the tags emit is safe
        textProcessor { text -> LatexText.escapeLatex(text) }
        // the TeX-family logos: prose mentions render as their true
        // letterforms, the verbatim regions untouched
        postProcess { output -> LatexText.renderLogos(output) }
    }

    /**
     * The hyperlink handler: external targets ride href, and references
     * to sibling chapters become live cross-references through the label
     * stamped on every chapter heading.
     */
    private fun linkHandler(chapterNames: Set<String>): TagHandler =
        { element ->
            LatexText.renderLink(
                element.attr("href").orEmpty(),
                element.fragments.joinToString("") { LatexText.renderFragment(it, chapterNames) },
                chapterNames,
            )
        }

    /**
     * The table handler: the tree is owned whole, the column count read
     * from the first row, and the cells rendered through their mixed
     * content as equal-width ragged columns — the cells carry long code
     * spans that must wrap somewhere, and no column knows its share of
     * the width in advance.
     */
    private fun tableHandler(chapterNames: Set<String>): TagHandler =
        { table ->
            val rows =
                table.children.flatMap { child ->
                    when (child.tagName) {
                        "thead", "tbody" -> child.children
                        "tr" -> listOf(child)
                        else -> emptyList()
                    }
                }
            val columns = rows.firstOrNull()?.children?.size ?: 0
            if (columns == 0) {
                ""
            } else {
                val columnWidth = "(\\linewidth+2\\tabcolsep)/$columns-2\\tabcolsep"
                buildString {
                    append("\\begin{longtable}{@{}")
                    repeat(columns) {
                        append(">{\\raggedright\\arraybackslash}p{\\dimexpr$columnWidth\\relax}")
                    }
                    append("@{}}\n")
                    append("\\toprule\n")
                    rows.forEachIndexed { index, row ->
                        append(row.children.joinToString(" & ") { cell -> LatexText.renderCell(cell, chapterNames) })
                        append(" \\\\\n")
                        if (index == 0) {
                            append("\\midrule\n")
                        }
                    }
                    append("\\bottomrule\n")
                    append("\\end{longtable}\n")
                }
            }
        }

    /**
     * The fenced-code handler: code fences ride the listings
     * environments, and mermaid fences are extracted into their own
     * source files for the diagram pass.
     */
    private fun preHandler(diagrams: DiagramNames): TagHandler =
        { element ->
            val code = element.firstChild
            val language = code?.attr("class")?.removePrefix("language-").orEmpty()
            val text = code?.text ?: element.text
            if (language == "mermaid") {
                val name = diagrams.write(text)
                // both limits with the ratio kept: whichever dimension
                // binds, the figure fits the page
                "\\begin{center}\\includegraphics[" +
                    "width=0.85\\textwidth, height=0.85\\textheight, keepaspectratio]" +
                    "{assets/$name}\\end{center}"
            } else {
                lstlistingFor(language, text)
            }
        }

    /**
     * Strips the front matter from a chapter and returns the body with
     * its title.
     *
     * @return The chapter body and its title, or `null` when the chapter
     *   carries no front matter or no title inside it.
     */
    private fun stripFrontMatter(markdown: String): Pair<String, String>? {
        val match = frontMatterRegex.find(markdown) ?: return null
        val title = titleRegex.find(match.groupValues[1])?.groupValues?.get(1)
        return title?.let { markdown.substring(match.range.last + 1) to it }
    }

    /**
     * Renders the extracted mermaid sources into the vector figures the
     * generated chapters reference, and refreshes the committed
     * checkpoint: the extracted sources and the rendered figures are
     * synced back into the assets directory, so the books build from a
     * fresh clone even without a reachable diagram service, and the
     * diff of a diagram change shows up in review. The fence in the
     * chapter remains the single source of truth — the checkpoint is
     * pipeline-maintained and never hand-edited.
     *
     * Kroki draws mermaid as SVG — it offers no PDF for it — and the
     * vector stream is transcoded straight into the crisp PDF the
     * books include, with no raster in sight.
     *
     * @param mermaidDir the directory holding the extracted .mmd files
     * @param checkpointDir the committed assets directory the pair syncs back to
     * @param endpoint the Kroki server to talk to
     * @return the rendered figure files
     */
    fun renderDiagrams(
        mermaidDir: File,
        checkpointDir: File,
        endpoint: String,
    ): List<File> {
        val sources = mermaidDir.listFiles { f -> f.extension == "mmd" }.orEmpty().sortedBy { it.name }
        if (sources.isEmpty()) {
            return emptyList()
        }
        return runBlocking {
            KrokiClient(endpoint).use { client ->
                sources.map { source ->
                    val request = mermaid(OutputFormat.SVG) { source.readText() }
                    val svg = client.render(request).toString(Charsets.UTF_8)
                    val figure = source.resolveSibling("${source.nameWithoutExtension}.pdf")
                    // the mermaid step rides the conversion: Kroki's
                    // diagrams carry their labels as HTML inside
                    // foreignObject elements and placeholder shapes
                    // the engine rejects, and the normalizer turns
                    // both into what a vector PDF can hold
                    figure.writeBytes(
                        Svg.parse(svg).normalizer(SvgNormalizers.mermaid).toPdfBytes(),
                    )
                    // the checkpoint refresh: the committed pair mirrors
                    // the extraction and the render, overwriting whatever
                    // was there before
                    val checkpoint = checkpointDir.resolve(source.name)
                    checkpoint.parentFile.mkdirs()
                    source.copyTo(checkpoint, overwrite = true)
                    figure.copyTo(checkpointDir.resolve(figure.name), overwrite = true)
                    figure
                }
            }
        }
    }

    /**
     * Converts one chapter of the manual from Markdown to LaTeX.
     *
     * The front matter's title opens the chapter in the book; the body
     * follows through the conversion preset.
     *
     * @param source the Markdown source file
     * @param outputDir the directory the .tex file lands in
     * @param mermaidDir the directory the extracted mermaid files land in
     * @return the output .tex file
     * @throws IllegalStateException if the chapter carries no front matter
     *   or the conversion fails
     */
    fun convertChapter(
        source: File,
        outputDir: File,
        mermaidDir: File,
    ): File {
        val chapterName = source.nameWithoutExtension
        val (body, title) =
            stripFrontMatter(source.readText())
                ?: error("The chapter $chapterName carries no front matter")
        // the manual's chapter set, so references to sibling chapters
        // resolve as live cross-references in the books
        val chapterNames =
            source.parentFile.listFiles { f -> f.extension == "md" }!!
                .mapTo(mutableSetOf()) { it.nameWithoutExtension }
        val result =
            CoralMark.convert(body, latexPreset(DiagramNames(chapterName, mermaidDir), chapterNames))
        val tex =
            result.getOrElse { failure ->
                error("The conversion of ${source.name} failed: $failure")
            }
        val outputFile = outputDir.resolve("$chapterName.tex")
        outputFile.parentFile.mkdirs()
        // the chapter heading carries its label: the name is the file
        // name, which the manual's cross-references spell verbatim
        outputFile.writeText("\\chapter{${LatexText.escapeLatex(title)}}\\label{chap:$chapterName}\n\n$tex")
        return outputFile
    }
}

"use strict";
/**
 * knuthify 0.1.0 (typescript)
 *
 * Set TeX words the way TeX sets them: a zero-dependency, client-side
 * walker that decorates TeX-family words in rendered prose with the
 * classic logo treatment. The Markdown sources are never modified;
 * without JavaScript, the prose stays exactly as written.
 *
 * Provenance of the metrics (see ../SPEC.md):
 *
 *   \TeX   = T\kern-.1667em\lower.5ex\hbox{E}\kern-.125emX   (D. E. Knuth)
 *   \LaTeX = L\kern-.36em{script-size A}\kern-.15em\TeX      (latex.ltx)
 *
 * The lowered capital E carries Knuth's exact numbers. The LaTeX branch
 * follows the LaTeX2e kernel: a script-size capital A, not the web's
 * folk lowercase — so the rendered text of a LaTeX-family word reads
 * LATEX, and prose TeX reads TEX, exactly as copying \TeX or \LaTeX
 * out of a typeset PDF does.
 *
 * Copyright (c) 2026, Island of TeX. BSD 3-Clause; see the LICENSE file.
 */
(() => {
    "use strict";
    /** Corpus-attested (arara manual) + future-proof (shipped rules). */
    const WORDS = [
        "TeX", "PDFLaTeX", "pdfLaTeX", "LaTeX", "BibTeX", "pdfTeX",
        "LuaTeX", "LuaLaTeX", "XeTeX", "upLaTeX", "pLaTeX",
        "PythonTeX", "PerlTeX", "ConTeXt",
        "PDFTeX", "LuaHBTeX", "upTeX", "eTeX", "XeLaTeX",
        "BibTeX8", "BibTeXu", "pBibTeX", "upBibTeX", "SliTeX", "PiCTeX",
    ];
    /** Longest-first, so BibTeX8 can never be shadowed by BibTeX. */
    const alternation = [...WORDS].sort((a, b) => b.length - a.length).join("|");
    const RE = new RegExp(`\\b(${alternation})(?![A-Za-z])`, "g");
    const TEST = new RegExp(RE.source); // stateless twin for hit-testing
    const SKIP = new Set([
        "CODE", "PRE", "KBD", "SCRIPT", "STYLE", "TEXTAREA", "NOSCRIPT",
    ]);
    const SVG_NS = "http://www.w3.org/2000/svg";
    const isSkipped = (node) => {
        let p = node.parentNode;
        while (p !== null && p !== document.body) {
            if (SKIP.has(p.nodeName))
                return true;
            if (p.nodeType === Node.ELEMENT_NODE && p.namespaceURI === SVG_NS) {
                return true;
            }
            p = p.parentNode;
        }
        return false;
    };
    /**
     * Builds one logo span from a matched allowlist word. Every node is
     * DOM-built: the only strings entering the DOM are the word's own
     * substrings, so no foreign content can ever be injected.
     */
    const decorate = (word) => {
        const logo = document.createElement("span");
        logo.className = "tex-logo";
        const at = word.lastIndexOf("TeX");
        const add = (text) => {
            if (text !== "")
                logo.appendChild(document.createTextNode(text));
        };
        const marked = (cls, letter) => {
            const s = document.createElement("span");
            s.className = cls;
            s.textContent = letter;
            logo.appendChild(s);
        };
        let pre = word.slice(0, at);
        if (pre.endsWith("La")) {
            add(pre.slice(0, -2));
            add("L");
            marked("tex-logo-a", "A");
        }
        else {
            add(pre);
        }
        add("T");
        marked("tex-logo-e", "E");
        add("X");
        add(word.slice(at + 3));
        return logo;
    };
    const rebuild = (node) => {
        const value = node.nodeValue ?? "";
        RE.lastIndex = 0;
        let m = RE.exec(value);
        if (m === null)
            return;
        const fragment = document.createDocumentFragment();
        let last = 0;
        while (m !== null) {
            if (m.index > last) {
                fragment.appendChild(document.createTextNode(value.slice(last, m.index)));
            }
            fragment.appendChild(decorate(m[1]));
            last = m.index + m[0].length;
            m = RE.exec(value);
        }
        if (last < value.length) {
            fragment.appendChild(document.createTextNode(value.slice(last)));
        }
        node.parentNode?.replaceChild(fragment, node);
    };
    const run = () => {
        if (document.body === null)
            return;
        const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, {
            acceptNode: (node) => isSkipped(node) || !TEST.test(node.nodeValue ?? "")
                ? NodeFilter.FILTER_REJECT
                : NodeFilter.FILTER_ACCEPT,
        });
        const hits = [];
        while (walker.nextNode())
            hits.push(walker.currentNode);
        hits.forEach(rebuild);
    };
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", run);
    }
    else {
        run();
    }
})();

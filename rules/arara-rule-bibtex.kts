fun generate(name: String, description: String) {
    rule(name.toLowerCase(),
            label = name.replace("bib", "Bib").replace("tex", "TeX"),
            description = description,
            authors = listOf("Island of TeX")) {
        argument<List<String>>("options") {
            defaultValue = listOf()
            processor { it }
        }

        command(name.toLowerCase(), options, reference.nameWithoutExtension)
    }
}

generate("bibtex", "The BibTeX reference management software")
generate("bibtex8", "An 8-bit implementation of BibTeX 0.99 with a very large capacity")
generate("upbibtex", "The upBibTeX reference management software")
generate("bibtexu", "A UTF-8 Big BibTeX version 0.99d")
generate("pbibtex", "The pBibTeX reference management software")

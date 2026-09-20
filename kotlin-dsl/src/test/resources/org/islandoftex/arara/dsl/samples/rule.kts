rule("id",
        label = "Long label",
        description = "Optional description",
        authors = listOf("Quack")) {
    argument("identifier") {
        required = true
        // we also need a way for a choices value etc.
    }
    argument<Int>("second identifier") {
        required = true
        defaultValue = 0
    }

    command("pdflatex", arguments["identifier"]!!, arguments["second identifier"]!!)
    execute {
        println("Test")
        exit(1)
    }
}

rule("id",
        label = "Long label",
        description = "Optional description",
        authors = listOf("Quack")) {
    argument("identifier") {
        required = true
        type = Int::class // this should be something for advanced user, but we should allow it
        // we also need a way for a choices value etc.
    }
    // command("pdflatex", arguments["identifier"], arguments["quack"])
}

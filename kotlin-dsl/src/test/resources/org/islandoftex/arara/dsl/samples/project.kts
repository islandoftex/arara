project("name") {
    workingDirectory(".")
    file("file to add", 2)
    file(File("another file"), 1)
    dependsOn("another project")
}

project("quack") {
    file("quack")
}

project("quack") {
    file("quack") {
        directives = listOf("pdflatex: {shell: 1}")
    }
}

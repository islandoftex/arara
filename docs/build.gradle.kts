dependencies {
    project(":application")
}

tasks.create<JavaExec>("buildManual") {
    group = "documentation"
    description = "Compile the manual's TeX file to PDF."

    classpath = files(project(":application").tasks.findByPath("shadowJar"))
    args = listOf("-l", "-v", "arara-manual.tex")
    inputs.dir("chapters")
    inputs.dir("figures")
    inputs.dir("logos")
    inputs.file("rules/manual.yaml")
    inputs.file("arara.sty")
    inputs.file("arararc.yaml")
    inputs.file("arara-manual.tex")
    outputs.files("arara-manual.pdf")
    outputs.upToDateWhen { false }
}

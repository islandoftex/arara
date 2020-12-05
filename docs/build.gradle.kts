dependencies {
    project(":cli")
}

tasks.create("writeVersionFile") {
    outputs.file("version.tex")
    outputs.upToDateWhen { false }

    file("version.tex").writeText(project.version.toString())
}

tasks.create<JavaExec>("buildManual") {
    group = "documentation"
    description = "Compile the manual's TeX file to PDF."

    dependsOn("writeVersionFile")

    classpath = files(project(":cli").tasks.findByPath("shadowJar"))
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

dependencies {
    project(":cli")
}

// TODO: update version in shell scripts?

tasks.create("buildDocs") {
    group = "documentation"
    description = "Compile all arara documentation files."

    dependsOn("buildManual", "buildQuickstartGuide")

    outputs.upToDateWhen { false }
}

tasks.create<Exec>("buildManual") {
    group = "documentation"
    description = "Compile the manual's TeX file to PDF."

    commandLine("sh", "htmlmanualtopdf.sh")

    inputs.file("htmlmanualtopdf.sh")
    inputs.file("resources")
    outputs.files("arara-manual.pdf")
    outputs.upToDateWhen { false }
}

tasks.create<Exec>("buildQuickstartGuide") {
    group = "documentation"
    description = "Compile the quickstart guide's TeX file to PDF."

    commandLine("sh", "htmlquickstarttopdf.sh")

    inputs.file("htmlquickstarttopdf.sh")
    inputs.file("resources")
    outputs.files("arara-quickstart.pdf")
    outputs.upToDateWhen { false }
}

dependencies {
    project(":cli")
}

tasks.create("writeVersionFile") {
    outputs.file("resources/version.txt")
    outputs.upToDateWhen { false }

    file("resources/version.txt").writeText(project.version.toString())
}

tasks.create("buildDocs") {
    group = "documentation"
    description = "Compile all arara documentation files."

    dependsOn("buildManual", "buildQuickstartGuide")

    outputs.upToDateWhen { false }
}

tasks.create<Exec>("buildManual") {
    group = "documentation"
    description = "Compile the manual's TeX file to PDF."

    dependsOn("writeVersionFile")
    commandLine("sh", "htmlmanualtopdf.sh")

    inputs.file("htmlmanualtopdf.sh")
    inputs.dir("resources")
    outputs.files("arara-manual.pdf")
    outputs.upToDateWhen { false }
}

tasks.create<Exec>("buildQuickstartGuide") {
    group = "documentation"
    description = "Compile the quickstart guide's TeX file to PDF."

    dependsOn("writeVersionFile")
    commandLine("sh", "htmlquickstarttopdf.sh")

    inputs.file("htmlquickstarttopdf.sh")
    inputs.dir("resources")
    outputs.files("arara-quickstart.pdf")
    outputs.upToDateWhen { false }
}

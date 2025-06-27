dependencies {
    project(":cli")
}

tasks {
    
    register("writeVersionFile") {
        outputs.file("resources/version.txt")
        outputs.upToDateWhen { false }

        file("resources/version.txt")
                .writeText(project.version.toString())
    }

    register("buildDocs") {
        group = "documentation"
        description = "Compile all arara documentation files."

        dependsOn("buildManual", "buildQuickstartGuide")

        outputs.upToDateWhen { false }
    }

    register<Exec>("buildManual") {
        group = "documentation"
        description = "Compile the manual's TeX file to PDF."

        dependsOn("writeVersionFile")
        commandLine("sh", "htmlmanualtopdf.sh")

        inputs.file("htmlmanualtopdf.sh")
        inputs.dir("resources")
        outputs.files("arara-manual.pdf")
        outputs.upToDateWhen { false }
    }

    register<Exec>("buildQuickstartGuide") {
        group = "documentation"
        description = "Compile the quickstart guide's TeX file to PDF."

        dependsOn("writeVersionFile")
        commandLine("sh", "htmlquickstarttopdf.sh")

        inputs.file("htmlquickstarttopdf.sh")
        inputs.dir("resources")
        outputs.files("arara-quickstart.pdf")
        outputs.upToDateWhen { false }
    }
    
}

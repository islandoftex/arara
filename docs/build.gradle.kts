// SPDX-License-Identifier: BSD-3-Clause
import java.nio.file.Files
import org.islandoftex.arara.build.BuildWebsiteBookTask
import org.islandoftex.arara.build.CheckWebsiteTask
import org.islandoftex.arara.build.PrepareWebsiteTask
import org.islandoftex.arara.build.website.WebsiteContent

val website = rootProject.layout.projectDirectory.dir("website")
val websiteBaseUrl = providers.environmentVariable("PAGES_URL").filter { it.isNotBlank() }.orElse(WebsiteContent.SITE_URL)

val prepareWebsite by tasks.registering(PrepareWebsiteTask::class) {
    group = "documentation"
    websiteDirectory.set(website)
    themeDirectory.set(website.dir("themes/devlab-theme"))
    readme.set(rootProject.layout.projectDirectory.file("README.md"))
    changelog.set(rootProject.layout.projectDirectory.file("CHANGELOG.md"))
    outputDirectory.set(layout.buildDirectory.dir("website/source"))
}

val buildWebsite by tasks.registering(Exec::class) {
    group = "documentation"
    dependsOn(prepareWebsite)
    inputs.dir(prepareWebsite.flatMap { it.outputDirectory })
    outputs.dir(website.dir("public"))
    doFirst {
        val output = website.dir("public").asFile
        check(!Files.isSymbolicLink(output.toPath())) {
            "website/public must be writable; remove its Nix-store symlink before building with Gradle."
        }
        // CI artifacts copied from the Nix store retain read-only permissions.
        output.walkTopDown().filter { !Files.isSymbolicLink(it.toPath()) }.forEach {
            check(it.setWritable(true)) { "Cannot make generated website writable: $it" }
        }
        commandLine("zola", "--root", prepareWebsite.get().outputDirectory.get().asFile,
            "build", "--force", "--output-dir", output)
    }
}

val checkWebsite by tasks.registering(CheckWebsiteTask::class) {
    group = "verification"
    dependsOn(buildWebsite)
    siteDirectory.set(website.dir("public"))
    siteUrl.set(websiteBaseUrl)
}

val writeVersionFile by tasks.registering {
    val output = layout.projectDirectory.file("resources/version.txt")
    inputs.property("version", project.version.toString())
    outputs.file(output)
    doLast { output.asFile.writeText(project.version.toString()) }
}

fun registerBook(name: String, isManual: Boolean, baseName: String) = tasks.register<BuildWebsiteBookTask>(name) {
    group = "documentation"
    dependsOn(checkWebsite, writeVersionFile)
    siteDirectory.set(website.dir("public"))
    resourcesDirectory.set(layout.projectDirectory.dir("resources"))
    manual.set(isManual)
    bookVersion.set(project.version.toString())
    siteUrl.set(websiteBaseUrl)
    htmlFile.set(layout.buildDirectory.file("books/$baseName.html"))
    pdfFile.set(layout.projectDirectory.file("$baseName.pdf"))
}

val buildManual = registerBook("buildManual", true, "arara-manual")
val buildQuickstartGuide = registerBook("buildQuickstartGuide", false, "arara-quickstart")

tasks.register("buildDocs") {
    group = "documentation"
    description = "Build the website, manual, and quickstart PDF."
    dependsOn(buildManual, buildQuickstartGuide)
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.tasks.bundling.Zip

open class SourceZipBuilderTask : Zip() {
    init {
        group = "distribution"
        description = "Create a source ZIP as required by CTAN."

        inputs.dir(project.projectDir.resolve("api/src"))
        inputs.file(project.projectDir.resolve("api/build.gradle.kts"))
        inputs.dir(project.projectDir.resolve("core/src"))
        inputs.file(project.projectDir.resolve("core/build.gradle.kts"))
        inputs.dir(project.projectDir.resolve("cli/src"))
        inputs.file(project.projectDir.resolve("cli/build.gradle.kts"))
        outputs.files(project.buildDir.resolve("arara-${project.version}-src.zip"))
        outputs.upToDateWhen { false }

        archiveFileName.set(project.buildDir.resolve("arara-${project.version}-src.zip").absolutePath)
        from(project.projectDir.resolve("api"))
        from(project.projectDir.resolve("core"))
        from(project.projectDir.resolve("cli"))
        exclude("build")
    }
}

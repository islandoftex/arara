// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.tasks.bundling.Zip

open class CTANZipBuilderTask : Zip() {
    init {
        group = "distribution"
        description = "Create a CTAN-ready ZIP file."

        inputs.dir(project.buildDir.resolve("ctan"))
        outputs.file(project.buildDir.resolve("arara-ctan.zip"))
        outputs.upToDateWhen { false }

        archiveFileName.set(project.buildDir.resolve("arara-ctan.zip").absolutePath)
        from(project.buildDir.resolve("ctan"))
    }
}

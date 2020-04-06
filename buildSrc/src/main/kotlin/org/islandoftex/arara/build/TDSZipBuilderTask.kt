// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.tasks.bundling.Zip

open class TDSZipBuilderTask : Zip() {
    init {
        group = "distribution"
        description = "Create a TDS compliant ZIP file."

        inputs.dir(project.buildDir.resolve("tds"))
        outputs.file(project.buildDir.resolve("arara.tds.zip"))
        outputs.upToDateWhen { false }

        archiveFileName.set(project.buildDir.resolve("arara.tds.zip").absolutePath)
        from(project.buildDir.resolve("tds"))
    }
}

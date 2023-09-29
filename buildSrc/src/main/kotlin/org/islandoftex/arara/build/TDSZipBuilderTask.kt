// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.tasks.bundling.Zip

/**
 * Zip the result of [TDSTreeBuilderTask] to create a valid
 * TDS zip.
 */
open class TDSZipBuilderTask : Zip() {
    init {
        group = "distribution"
        description = "Create a TDS compliant ZIP file."

        inputs.dir(project.layout.buildDirectory.dir("tds").get().asFile)
        outputs.file(project.layout.buildDirectory.file("arara.tds.zip").get().asFile)
        outputs.upToDateWhen { false }

        archiveFileName.set(project.layout.buildDirectory.file("arara.tds.zip").get().asFile.absolutePath)
        from(project.layout.buildDirectory.dir("tds").get().asFile)
    }
}

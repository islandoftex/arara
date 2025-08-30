// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.tasks.bundling.Zip
import org.gradle.work.DisableCachingByDefault

/**
 * Zip the result of [TDSTreeBuilderTask] to create a valid
 * TDS zip.
 */
@DisableCachingByDefault(because = "simple wrapper around zip, not worth caching")
abstract class TDSZipBuilderTask : Zip() {
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

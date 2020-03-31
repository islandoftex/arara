// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.project

import java.io.File

data class Project(
    val name: String,
    val workingDirectory: File,
    val files: List<File>,
    val dependencies: Set<String> = setOf()
) {
    internal val absoluteFiles = files.map {
        if (it.isAbsolute)
            it
        else
            workingDirectory.resolve(it)
    }
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.project

import java.nio.file.Path
import org.islandoftex.arara.model.FileType

data class ProjectFile(
    val path: Path,
    val fileType: FileType
) : Path by path

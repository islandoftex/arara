// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.LinearExecutor

/** Current-file and extension references for directive conditionals. */
object ConditionalReferenceHelpers {
    /**
     * Returns a file object based on the provided name.
     *
     * @param name The file name.
     * @return A file object.
     */
    fun toFile(name: String): MPPPath = MPPPath(name)

    /**
     * Gets the file reference for the current directive. It is important to
     * observe that version 4.0 of arara replicates the directive when 'files'
     * is detected amongst the parameters, so each instance will have a
     * different reference.
     *
     * @return A file reference for the current directive.
     */
    fun currentFile(): MPPPath = LinearExecutor.currentFile!!.path
}

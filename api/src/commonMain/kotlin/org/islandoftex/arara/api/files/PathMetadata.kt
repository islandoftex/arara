// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import org.islandoftex.arara.api.time.DateTime

/**
 * Strictly retrieved metadata. A null field means the backend did not provide that value.
 */
public data class PathMetadata(
    /**
     * Whether the target is a regular file.
     */
    public val isRegularFile: Boolean,

    /**
     * Whether the target is a directory.
     */
    public val isDirectory: Boolean,

    /**
     * Byte size, or null when unknown.
     */
    public val size: Long?,

    /**
     * Modification time, or null when unknown.
     */
    public val lastModified: DateTime?
)

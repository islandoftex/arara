// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import okio.FileSystem

/**
 * Holds the file system the path model operates against. Production
 * code always runs against the system file system; tests may
 * temporarily swap in an in-memory file system to exercise spellings
 * the host platform cannot represent, restoring the previous instance
 * in a `finally` block.
 */
internal object PathModelFileSystem {
    /**
     * The file system the path model currently operates against.
     */
    internal var current: FileSystem = FileSystem.SYSTEM
}

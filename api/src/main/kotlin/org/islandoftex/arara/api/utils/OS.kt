// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.utils

/**
 * Implements a simple OS check.
 */
public object OS {

    // it's unlikely that getting the OS name property would throw an
    // exception (but it's possible), so let's make sure it doesn't fail
        /**
     * The full name of the operating system, resolved once
     * from the os.name property.
     */
private val fullName: String by lazy {
        System.getProperties().runCatching { getProperty("os.name", "Linux") }
                .getOrDefault("Linux").lowercase()
    }

    /**
     * Whether the underlying operating system is Windows. The check is
     * based on the `os.name` system property.
     */
    public val isWindows: Boolean by lazy { fullName.contains("win") }

    /**
     * Whether the underlying operating system is Linux (also covering the
     * `nix`, `nux` and `aix` spellings). The check is based on the
     * `os.name` system property.
     */
    public val isLinux: Boolean by lazy { setOf("nix", "nux", "aix").any { fullName.contains(it) } }

    /**
     * Whether the underlying operating system is macOS. The check is
     * based on the `os.name` system property.
     */
    public val isMac: Boolean by lazy { fullName.contains("mac") }
}

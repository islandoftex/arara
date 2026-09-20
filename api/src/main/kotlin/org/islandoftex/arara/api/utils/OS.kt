// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.utils

/**
 * Implements a simple OS check.
 */
public object OS {

    // it's unlikely that getting the OS name property would throw an
    // exception (but it's possible), so let's make sure it doesn't fail
    private val fullName: String by lazy {
        System.getProperties().runCatching { getProperty("os.name", "Linux") }
                .getOrDefault("Linux").lowercase()
    }

    public val isWindows: Boolean by lazy { fullName.contains("win") }
    public val isLinux: Boolean by lazy { setOf("nix", "nux", "aix").any { fullName.contains(it) } }
    public val isMac: Boolean by lazy { fullName.contains("mac") }
}

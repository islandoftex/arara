// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.diagnostics

internal actual fun toolProfileResource(name: String): String =
    checkNotNull(ToolProfiles::class.java.getResourceAsStream(name)) { "Missing diagnostic profiles: $name" }
        .bufferedReader(Charsets.UTF_8).use { it.readText() }

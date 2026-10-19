// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.core.session.Environment

internal actual fun scriptUserProperties(): Map<String, String> = mapOf(
    "home" to (Environment.getSystemPropertyOrNull("user.home") ?: ""),
    "name" to (Environment.getSystemPropertyOrNull("user.name") ?: "")
)

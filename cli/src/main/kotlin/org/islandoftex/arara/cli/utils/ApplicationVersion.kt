// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.AraraAPI

internal actual fun applicationVersion(): String = AraraAPI.version

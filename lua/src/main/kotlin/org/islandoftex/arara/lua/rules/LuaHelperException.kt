// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException

/**
 * An arara-generated error raised at the Lua helper boundary (e.g. a
 * badly typed helper argument), as opposed to the author-authored
 * errors of `throwError`: the rule evaluation wraps these behind the
 * localized runtime errors of the engine while author text keeps
 * flowing raw, mirroring the MVEL dialect.
 */
class LuaHelperException(
    message: String,
    exception: Exception? = null
) : AraraException(message, exception ?: RuntimeException(message))

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.AraraException

/**
 * Indicates that a Lua project specification could not be parsed, e.g.
 * because it is not valid Lua or does not return a table.
 */
class ProjectParseException(
    s: String,
    exception: Exception? = null
) : AraraException(s, exception ?: RuntimeException(s))

/**
 * Indicates that a Lua project specification violates the documented
 * project invariants, e.g. unresolvable dependencies.
 */
class ProjectValidationException(
    s: String,
    exception: Exception? = null
) : AraraException(s, exception ?: RuntimeException(s))

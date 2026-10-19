// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException

/** A helper argument or conversion error, distinct from an authored rule error. */
internal class LuaHelperException(message: String) : AraraException(message)

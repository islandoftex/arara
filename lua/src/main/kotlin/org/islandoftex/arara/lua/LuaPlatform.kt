// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import io.github.taoguan.luaj.Globals
import io.github.taoguan.luaj.LuaValue
import io.github.taoguan.luaj.lib.jse.JsePlatform
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.session.Environment

/**
 * The Lua platform arara's script formats run on: the standard
 * globals with arara's own values in scope, shared by the
 * configuration and the project specification formats.
 *
 * @author Island of TeX
 */
internal object LuaPlatform {
    /**
     * Builds the standard globals with arara's values bound: the
     * table `user` carries the `home` and `name` of the user
     * running arara, and `cwd` is the working directory the
     * script is loaded for. Missing system properties fall back
     * to empty strings so concatenation never sees nil.
     *
     * @param workingDirectory The working directory bound as
     *   `cwd`.
     * @return The globals every arara Lua script runs on.
     */
    fun globalsFor(workingDirectory: MPPPath): Globals =
        JsePlatform.standardGlobals().also { globals ->
            globals.set(
                "user",
                LuaValue.tableOf().also { user ->
                    user.set(
                        "home",
                        LuaValue.valueOf(
                            Environment.getSystemPropertyOrNull("user.home")
                                ?: ""
                        )
                    )
                    user.set(
                        "name",
                        LuaValue.valueOf(
                            Environment.getSystemPropertyOrNull("user.name")
                                ?: ""
                        )
                    )
                }
            )
            globals.set(
                "cwd",
                LuaValue.valueOf(workingDirectory.normalize().toString())
            )
        }
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

internal object LanguageController {
    @Suppress("SpreadOperator")
    fun <E : Enum<*>> getMessage(key: E, vararg parameters: Any): String = ""

    fun <E : Enum<*>> getMessage(key: E): String = ""
}

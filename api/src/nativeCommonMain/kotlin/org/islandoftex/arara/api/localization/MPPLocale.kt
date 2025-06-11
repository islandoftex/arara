// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

public actual class MPPLocale actual constructor(
    tag: String,
) {
    // TODO: proper implementation
    public actual val displayLanguage: String = tag
    public actual val decimalSeparator: Char = '.'
}

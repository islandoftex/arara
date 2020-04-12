// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.session.UserInterfaceOptions

data class UserInterfaceOptions(
    override val languageCode: String = "en",
    override val swingLookAndFeel: String = "none"
) : UserInterfaceOptions

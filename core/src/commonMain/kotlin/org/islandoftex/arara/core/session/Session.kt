// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.session.Session
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.utils.formatString

/**
 * Implements the session, i.e. one single run of the whole arara tool.
 * It is a superset of the session exposed to the user.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
expect object Session : Session {

    /**
     * Update the environment variables stored in the session.
     *
     * @param additionFilter Which environment variables to include. You can
     *   filter their names (the string parameter) but not their values. By
     *   default all values will be added.
     * @param removalFilter Which environment variables to remove beforehand.
     *   By default all values will be removed.
     */
    fun updateEnvironmentVariables(
        additionFilter: (String) -> Boolean = { true },
        removalFilter: (String) -> Boolean = { true }
    )
}

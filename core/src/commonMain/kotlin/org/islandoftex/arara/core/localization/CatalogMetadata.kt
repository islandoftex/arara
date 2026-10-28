// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** Typed messages for catalog metadata. */
interface CatalogMetadata {
    /** The locale of this catalog. */
    val providedLocale: MPPLocale

    /** The translators credited for this catalog. */
    val translators: List<String>
}

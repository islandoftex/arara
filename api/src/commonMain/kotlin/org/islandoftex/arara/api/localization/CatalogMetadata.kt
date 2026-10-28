// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

import org.islandoftex.arara.api.localization.MPPLocale

/** Typed messages for catalog metadata. */
public interface CatalogMetadata {
    /** The locale of this catalog. */
    public val providedLocale: MPPLocale

    /** The translators credited for this catalog. */
    public val translators: List<String>
}

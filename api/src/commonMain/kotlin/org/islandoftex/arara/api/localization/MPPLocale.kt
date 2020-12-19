// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/**
 * Represent a locale independent of the platform in use.
 *
 * Constructed from a locale tag, it is only used to hold th state of the
 * locale and provide a display language.
 */
public expect class MPPLocale(tag: String) {
    /**
     * Display language of the language in use. If not possible to provide,
     * falls back to language tag.
     */
    public val displayLanguage: String
}

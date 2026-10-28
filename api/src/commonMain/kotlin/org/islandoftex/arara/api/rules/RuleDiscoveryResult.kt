// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.rules

import org.islandoftex.arara.api.files.MPPPath

/** One candidate, including shadowed files, in execution lookup order. */
public data class RuleLocation(
    /** Directive identifier, without a distribution prefix or suffix. */
    public val identifier: String,
    /** Absolute normalized rule location. */
    public val path: MPPPath,
    /** Whether execution would select this candidate. */
    public val effective: Boolean
) {
    /** Filename suffix identifying the resolved rule format. */
    public val format: String get() = path.fileName.substringAfterLast('.')
}

/** A recoverable directory or rule inspection error. */
public data class RuleDiagnostic(
    /** Location associated with the error, when known. */
    public val path: String?,
    /** Diagnostic supplied by lookup or the resolved interpreter. */
    public val message: String
)

/** Rule inventory with optional metadata from explicit inspection. Listing never evaluates rule scripts. */
public data class RuleDiscoveryResult(
    /** Candidates in deterministic directory and format priority. */
    public val rules: List<RuleLocation>,
    /** Recoverable lookup and inspection errors. */
    public val diagnostics: List<RuleDiagnostic>,
    /** Parsed metadata for an explicitly inspected rule, or null for a filename-only listing. */
    public val metadata: Rule? = null
)

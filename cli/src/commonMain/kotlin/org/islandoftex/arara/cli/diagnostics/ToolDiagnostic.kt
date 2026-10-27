// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.diagnostics

import org.islandoftex.arara.api.configuration.LogLevel

/**
 * A diagnostic parsed from captured process output; it does not determine command success.
 * @property tool Executable name, without its directory or platform suffix.
 * @property level Recognized severity, or TRACE for otherwise unclassified output.
 * @property message Original diagnostic text, with terminal escape sequences removed.
 * @property outputLine One-based line in the captured output.
 * @property file Source filename explicitly reported by the tool, when available.
 * @property line Source line explicitly reported by the tool, when available.
 */
data class ToolDiagnostic(
    val tool: String,
    val level: LogLevel,
    val message: String,
    val outputLine: Int,
    val file: String? = null,
    val line: Int? = null
)

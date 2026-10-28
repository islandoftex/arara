// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.io.FileNotFoundException
import java.nio.file.NoSuchFileException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.RuleDiagnostic
import org.islandoftex.arara.api.rules.RuleDiscoveryResult
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.rules.RuleLocation

/** Rule metadata discoverable from filenames without running or parsing scripts. */
object RuleInventory {
    /** Scan configured rule directories, retaining shadowed locations and recoverable lookup errors. */
    fun discover(options: ExecutionOptions, directory: MPPPath, engine: RuleEngine): RuleDiscoveryResult {
        val diagnostics = mutableListOf<RuleDiagnostic>()
        val identifiers = sortedSetOf<String>()
        options.rulePaths.map { directory.resolve(it).normalize() }.distinct().forEach { path ->
            identifiers.addAll(identifiers(path, diagnostics))
        }
        val locations = identifiers.flatMap { identifier ->
            RuleLookup.candidates(identifier, options.rulePaths, directory, engine).filter { it.exists }
                .mapIndexed { index, path -> RuleLocation(identifier, path, index == 0) }
        }
        return RuleDiscoveryResult(locations, diagnostics)
    }

    private fun identifiers(path: MPPPath, diagnostics: MutableList<RuleDiagnostic>): Set<String> {
        var inspected = false
        return try {
            val metadata = path.metadata()
            inspected = true
            if (!metadata.isDirectory) throw AraraIOException("Not a rule directory: $path")
            path.listEntries().mapNotNull { entry ->
                if (entry.fileName.substringAfterLast('.') in setOf("lua", "yaml"))
                    entry.fileName.substringBeforeLast('.').removePrefix("arara-rule-").takeIf { it.isNotEmpty() }
                else null
            }.toSet()
        } catch (exception: AraraIOException) {
            if (inspected || (exception.cause !is FileNotFoundException && exception.cause !is NoSuchFileException)) {
                diagnostics.add(RuleDiagnostic(path.toString(), exception.message.orEmpty()))
            }
            emptySet()
        } catch (exception: SecurityException) {
            diagnostics.add(RuleDiagnostic(path.toString(), exception.message.orEmpty()))
            emptySet()
        }
    }
}

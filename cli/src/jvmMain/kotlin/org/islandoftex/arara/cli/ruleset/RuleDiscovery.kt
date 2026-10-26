// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.io.FileNotFoundException
import java.nio.file.NoSuchFileException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule

internal data class RuleLocation(val identifier: String, val path: MPPPath, val effective: Boolean) {
    val format: String get() = path.fileName.substringAfterLast('.')
}

internal data class RuleDiagnostic(val path: String?, val message: String)

internal data class RuleDiscoveryResult(
    val rules: List<RuleLocation>,
    val diagnostics: List<RuleDiagnostic>,
    val metadata: Rule? = null
) {
    fun json(): String = Json.encodeToString(JsonObject.serializer(), buildJsonObject {
        put("rules", JsonArray(rules.map { location -> buildJsonObject {
            put("identifier", location.identifier)
            put("format", location.format)
            put("path", location.path.toString())
            put("effective", location.effective)
            metadata?.let { rule ->
                put("name", rule.displayName?.let(::JsonPrimitive) ?: JsonNull)
                put("authors", jsonValue(rule.authors))
                put("commands", jsonValue(rule.commands.map { it.name }))
                put("arguments", JsonArray(rule.arguments.map { argument -> buildJsonObject {
                    val value = argument.defaultValue
                    val dynamic = location.format == "yaml" && value is String && "@{" in value
                    put("identifier", argument.identifier)
                    put("required", argument.isRequired)
                    put("defaultKind", if (dynamic) "dynamic" else if (value == null) "none" else "literal")
                    put("default", if (dynamic) JsonNull else jsonValue(value))
                } }))
            }
        } }))
        put("diagnostics", JsonArray(diagnostics.map { diagnostic -> buildJsonObject {
            put("path", diagnostic.path?.let(::JsonPrimitive) ?: JsonNull)
            put("message", diagnostic.message)
        } }))
    })
}

internal object RuleDiscovery {
    fun discover(options: ExecutionOptions, directory: MPPPath, engine: RuleEngine): RuleDiscoveryResult {
        val diagnostics = mutableListOf<RuleDiagnostic>()
        val identifiers = sortedSetOf<String>()
        options.rulePaths.map { directory.resolve(it).normalize() }.distinct().forEach { path ->
            identifiers.addAll(identifiers(path, diagnostics))
        }
        val locations = identifiers.flatMap { identifier ->
            RuleFiles.candidates(identifier, options.rulePaths, directory, engine).filter { it.exists }
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

    fun inspect(identifier: String, options: ExecutionOptions, directory: MPPPath,
                engine: RuleEngine): RuleDiscoveryResult {
        // Selection and parse errors must agree with directive execution, including malformed overrides.
        var location: RuleLocation? = null
        return try {
            val path = RuleFiles.resolve(identifier, options, directory, engine)
            location = RuleLocation(identifier, path, true)
            val rule = RuleFiles.parseRule(path, identifier, scriptOutputToStderr = true)
            RuleDiscoveryResult(listOf(location), emptyList(), rule)
        } catch (exception: AraraException) {
            RuleDiscoveryResult(listOfNotNull(location),
                listOf(RuleDiagnostic(location?.path?.toString(), exception.message.orEmpty())))
        }
    }
}

private fun jsonValue(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is Boolean -> JsonPrimitive(value)
    is Number -> if (value.toDouble().isFinite()) JsonPrimitive(value) else JsonPrimitive(value.toString())
    is String -> JsonPrimitive(value)
    is List<*> -> JsonArray(value.map(::jsonValue))
    is Map<*, *> -> JsonObject(value.entries.associate { it.key.toString() to jsonValue(it.value) })
    else -> JsonPrimitive(value.toString())
}

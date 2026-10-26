// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.parse
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.CLI
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor

class RuleDiscoveryTest {
    private fun fixture(test: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara-rule-discovery").toMPPPath()
        val snapshot = ConfigurationSnapshot.capture()
        try {
            LinearExecutor.executionOptions = ExecutionOptions(rulePaths = linkedSetOf(root, root / "missing"))
            test(root)
        } finally {
            snapshot.restore()
            root.removeDirectory()
        }
    }

    private fun lua(root: MPPPath, name: String = "probe.lua") {
        root.resolve(name).writeText("""
            print("RULE_METADATA_OUTPUT")
            return { identifier='probe', name='Probe', authors={'Island of TeX'},
              commands={{name='First', command=function() error('must not execute') end},
                {command=function() error('must not execute') end}},
              arguments={{identifier='literal', default={'one', 2, true}},
                {identifier='mapping', default={enabled=true, count=3}},
                {identifier='nonfinite', default=0/0},
                {identifier='required', required=true, flag=function() error('must not execute') end}} }
        """.trimIndent())
    }

    private fun yaml(root: MPPPath, name: String = "probe.yaml") {
        root.resolve(name).writeText("""
            !config
            identifier: probe
            name: Legacy
            commands:
              - name: Legacy command
                command: '@{ throw new RuntimeException("must not execute"); }'
            arguments:
              - identifier: dynamic
                default: '@{ throw new RuntimeException("must not execute"); }'
              - identifier: literal
                default: plain
        """.trimIndent())
    }

    private fun command(vararg args: String): Pair<Int, String> {
        val previous = System.out
        val output = ByteArrayOutputStream()
        var status = 0
        try {
            System.setOut(PrintStream(output, true, Charsets.UTF_8))
            try {
                CLI().parse(args.toList())
            } catch (result: ProgramResult) {
                status = result.statusCode
            }
        } finally {
            System.setOut(previous)
        }
        return status to output.toString(Charsets.UTF_8)
    }

    @Test
    fun listingDoesNotExecuteScriptsAndUsesExecutionPrecedence() = fixture { root ->
        lua(root)
        lua(root, "arara-rule-probe.lua")
        yaml(root)
        root.resolve("zebra.lua").writeText("error('must not execute')")
        root.resolve("ignored.txt").writeText("ignored")
        root.resolve("nested").createDirectories()
        lua(root / "nested")
        val result = RuleDiscovery.discover(LinearExecutor.executionOptions, root, RuleEngine.LUA)
        assertTrue(result.diagnostics.isEmpty())
        assertEquals(listOf("probe", "probe", "probe", "zebra"), result.rules.map { it.identifier })
        assertEquals(listOf(true, false, false, true), result.rules.map { it.effective })
        assertEquals(root / "probe.lua", RuleFiles.resolve("probe", LinearExecutor.executionOptions, root, RuleEngine.LUA))
        assertEquals("yaml", RuleDiscovery.discover(LinearExecutor.executionOptions, root, RuleEngine.YAML)
            .rules.first().format)
        val later = root / "later"
        later.createDirectories()
        lua(later)
        val options = ExecutionOptions(rulePaths = linkedSetOf(root, later))
        assertEquals(root / "probe.yaml", RuleFiles.resolve("probe", options, root, RuleEngine.YAML))
        assertFalse(RuleDiscovery.discover(options, root, RuleEngine.LUA).rules.last { it.identifier == "probe" }.effective)
    }

    @Test
    fun missingDirectoriesAreSkippedButOtherIoErrorsRetainPartialResults() = fixture { root ->
        lua(root)
        val file = root / "not-a-directory"
        file.writeText("file")
        val options = ExecutionOptions(rulePaths = linkedSetOf(root / "missing", file, root))
        val result = RuleDiscovery.discover(options, root, RuleEngine.LUA)
        assertEquals(1, result.rules.size)
        assertEquals(file.toString(), result.diagnostics.single().path)
        assertEquals(1, Json.parseToJsonElement(result.json()).jsonObject.getValue("diagnostics").jsonArray.size)
    }

    @Test
    fun inspectionUsesExistingMetadataWithoutEvaluatingFunctionsOrTemplates() = fixture { root ->
        lua(root)
        yaml(root)
        val options = LinearExecutor.executionOptions
        val modern = RuleDiscovery.inspect("probe", options, root, RuleEngine.LUA)
        assertTrue(modern.diagnostics.isEmpty())
        val json = Json.parseToJsonElement(modern.json()).jsonObject.getValue("rules").jsonArray.single().jsonObject
        assertEquals("Probe", json.getValue("name").jsonPrimitive.content)
        assertEquals(2, json.getValue("commands").jsonArray.size)
        assertEquals("literal", json.getValue("arguments").jsonArray.first().jsonObject
            .getValue("defaultKind").jsonPrimitive.content)
        val legacy = RuleDiscovery.inspect("probe", options, root, RuleEngine.YAML)
        val arguments = Json.parseToJsonElement(legacy.json()).jsonObject.getValue("rules")
            .jsonArray.single().jsonObject.getValue("arguments").jsonArray
        assertEquals("dynamic", arguments.first().jsonObject.getValue("defaultKind").jsonPrimitive.content)
        assertEquals("plain", arguments.last().jsonObject.getValue("default").jsonPrimitive.content)
    }

    @Test
    fun malformedEffectiveRulesFailWithoutInspectingShadowedRules() = fixture { root ->
        root.resolve("probe.lua").writeText("return {}")
        yaml(root)
        val options = LinearExecutor.executionOptions
        val result = RuleDiscovery.inspect("probe", options, root, RuleEngine.LUA)
        assertEquals(root.resolve("probe.lua").toString(), result.diagnostics.single().path)
        assertEquals(null, result.metadata)
        assertFailsWith<AraraException> { RuleFiles.parseRule(RuleFiles.resolve("probe", options, root, RuleEngine.LUA), "probe") }
        assertTrue(RuleDiscovery.inspect("probe", options, root, RuleEngine.YAML).diagnostics.isEmpty())
        assertTrue(RuleDiscovery.inspect("unknown", options, root, RuleEngine.LUA).diagnostics.isNotEmpty())
    }

    @Test
    fun subcommandLoadsLocalConfigurationWithoutAFileOrProjectAndRestoresState() = fixture { root ->
        val custom = root / "custom"
        custom.createDirectories()
        lua(custom)
        yaml(custom)
        root.resolve("arararc.lua").writeText("print('CONFIGURATION_OUTPUT'); return { paths={cwd .. '/custom'} }")
        LinearExecutor.executionOptions = ExecutionOptions(rulePaths = emptySet())
        root.resolve("broken.proj.lua").writeText("error('must not execute')")
        val previous = LinearExecutor.executionOptions
        val (status, output) = command("rules", "-d", root.toString(), "--json")
        assertEquals(0, status, output)
        val result = Json.parseToJsonElement(output).jsonObject
        assertEquals("lua", result.getValue("rules").jsonArray.single().jsonObject.getValue("format").jsonPrimitive.content)
        assertEquals(previous, LinearExecutor.executionOptions)
        val all = command("-d", root.toString(), "-e", "yaml", "rules", "--all", "--json").second
        assertEquals(2, Json.parseToJsonElement(all).jsonObject.getValue("rules").jsonArray.size)
        assertEquals("yaml", Json.parseToJsonElement(all).jsonObject.getValue("rules").jsonArray.first()
            .jsonObject.getValue("format").jsonPrimitive.content)
    }

    @Test
    fun filteringIsCaseInsensitiveAndEmptyResultsSucceed() = fixture { root ->
        lua(root)
        val (_, output) = command("rules", "-d", root.toString(), "--filter", "RoB", "--list")
        assertTrue("probe" in output && "lua" in output, output)
        assertEquals(0 to "", command("rules", "-d", root.toString(), "--filter", "absent"))
        assertFailsWith<UsageError> { command("rules", "--info", "probe", "--all") }
        assertFailsWith<UsageError> { command("rules", "--info", "probe", "--filter", "p") }
        assertFailsWith<UsageError> { command("rules", "--info", "probe", "--list") }
        assertFailsWith<UsageError> { command("rules", "-d", (root / "missing").toString()) }
        assertFailsWith<UsageError> { command("rules", "-d", (root / "probe.lua").toString()) }
    }

    @Test
    fun humanAndJsonInspectionReportMetadataAndErrors() = fixture { root ->
        lua(root)
        yaml(root)
        val (status, output) = command("rules", "-d", root.toString(), "--info", "probe")
        assertEquals(0, status)
        assertTrue("Name: Probe" in output)
        assertTrue("Command 2: [unnamed]" in output)
        assertTrue("required=true; default=none" in output)
        assertTrue("default=dynamic" in command("rules", "-d", root.toString(), "-e", "yaml", "--info", "probe").second)
        assertTrue("(shadowed)" in command("rules", "-d", root.toString(), "--all").second)
        val missing = command("rules", "-d", root.toString(), "--info", "absent", "--json")
        assertEquals(2, missing.first)
        assertEquals(1, Json.parseToJsonElement(missing.second).jsonObject.getValue("diagnostics").jsonArray.size)
        assertEquals(2, command("rules", "-d", root.toString(), "--info", "absent").first)
        val unreadable = root / "not-a-directory"
        unreadable.writeText("file")
        LinearExecutor.executionOptions = ExecutionOptions(rulePaths = linkedSetOf(root, unreadable))
        val partial = command("rules", "-d", root.toString(), "--json")
        assertEquals(2, partial.first)
        assertTrue(Json.parseToJsonElement(partial.second).jsonObject.getValue("rules").jsonArray.isNotEmpty())
        root.resolve("arararc.lua").writeText("return { loops='invalid' }")
        val invalid = command("rules", "-d", root.toString(), "--json")
        assertEquals(2, invalid.first)
        assertEquals(1, Json.parseToJsonElement(invalid.second).jsonObject.getValue("diagnostics").jsonArray.size)
    }
}
